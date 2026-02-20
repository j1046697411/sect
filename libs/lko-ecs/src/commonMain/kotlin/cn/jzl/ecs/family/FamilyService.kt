package cn.jzl.ecs.family

import androidx.collection.mutableLongObjectMapOf
import androidx.collection.mutableScatterMapOf
import cn.jzl.core.bits.BitSet
import cn.jzl.core.list.LongFastList
import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.archetype.Archetype
import cn.jzl.ecs.component.components
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.relation.kind
import cn.jzl.ecs.relation.relations
import cn.jzl.ecs.relation.target

/**
 * 家族服务，管理 Family 实例和原型注册
 *
 * FamilyService 负责创建、缓存 Family 实例，并在新原型创建时
 * 自动将其注册到匹配的 Family 中。
 *
 * ## 主要功能
 * - 创建和缓存 Family 实例
 * - 注册原型到匹配的 Family
 * - 管理组件位图索引，加速匹配判断
 *
 * ## 使用示例
 * ```kotlin
 * // 创建 Family
 * val family = world.familyService.family {
 *     component<Position>()
 *     component<Velocity>()
 * }
 *
 * // 遍历家族中的所有原型
 * family.archetypes.forEach { archetype ->
 *     // 处理原型中的实体
 * }
 * ```
 *
 * @param world 关联的 ECS 世界
 * @property allArchetypeBits 所有原型的位图集合
 */
class FamilyService(override val world: World) : FamilyMatchScope, WorldOwner {

    /**
     * 所有原型的位图集合
     *
     * 每个位代表一个原型 ID
     */
    override val allArchetypeBits: BitSet = BitSet.Companion()
    private val componentMap = mutableLongObjectMapOf<BitSet>()
    private val families = mutableScatterMapOf<String, Family>()
    private val emptyBits = BitSet()

    /**
     * 获取包含指定关系的所有原型的位图
     *
     * @param relation 关系对象
     * @return 原型位图
     */
    override fun getArchetypeBits(relation: Relation): BitSet {
        return componentMap[relation.data] ?: emptyBits
    }

    /**
     * 注册原型到家族服务
     *
     * 当新原型创建时调用，自动将其添加到匹配的 Family 中
     *
     * @param archetype 要注册的原型
     */
    @PublishedApi
    internal fun registerArchetype(archetype: Archetype) {
        allArchetypeBits.set(archetype.id)
        fun setRelation(relation: Relation): Unit =
            componentMap.getOrPut(relation.data) { BitSet.Companion() }.set(archetype.id)
        archetype.entityType.forEach { relation ->
            if (relation.isRelation()) {
                setRelation(relations.kind(relation.kind))
                setRelation(relations.target(relation.target))
            }
            setRelation(relation)
        }
        families.forEachValue {
            if (!it.familyMatcher.match(archetype)) {
                return@forEachValue
            }
            it.addArchetype(archetype)
        }
    }

    private fun Relation.isRelation(): Boolean = components.componentOf != target

    /**
     * 创建或获取 Family 实例
     *
     * 根据构建器配置创建 Family，相同配置的 Family 会被缓存
     *
     * @param block Family 配置闭包
     * @return Family 实例
     */
    fun family(block: FamilyBuilder.() -> Unit): Family {
        val keys = LongFastList()
        val familyMatcher = and(keys, block)
        val key = keys.joinToString("|") { it.toString(16) }
        return families.getOrPut(key) {
            val family = Family(world, familyMatcher)
            familyMatcher.run { getArchetypeBits() }.map { world.archetypeService[it] }.forEach(family::addArchetype)
            family
        }
    }

    /**
     * 遍历匹配的原型
     *
     * @param receiver 原型处理器
     * @param block Family 配置闭包
     */
    fun buildArchetype(receiver: (Archetype) -> Unit, block: FamilyBuilder.() -> Unit) {
        val keys = LongFastList()
        val familyMatcher = and(keys, block)
        familyMatcher.run { getArchetypeBits() }.map { world.archetypeService[it] }.forEach(receiver)
    }

    private fun and(keys: LongFastList, block: FamilyBuilder.() -> Unit): FamilyMatcher {
        var matcher: FamilyMatcher? = null
        val familyBuilder = object : FamilyBuilder, WorldOwner by this {
            override val keys: LongFastList get() = keys
            override fun matcher(familyMatcher: FamilyMatcher) {
                matcher = familyMatcher
            }
        }
        familyBuilder.and(block)
        return requireNotNull(matcher) { "" }
    }
}
