package cn.jzl.ecs.archetype

import androidx.collection.LongSparseArray
import androidx.collection.getOrElse
import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.component.components
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.getOrPut
import cn.jzl.ecs.relation.EntityType
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.relation.kind
import cn.jzl.ecs.relation.target

/**
 * 原型，表示具有相同组件组合的实体集合
 *
 * Archetype 是 ECS 框架的核心数据结构，将具有相同组件组合的实体组织在一起，
 * 实现组件数据的连续存储，提高缓存命中率和查询性能。
 *
 * ## 工作原理
 * 1. 每个独特的组件组合对应一个 Archetype
 * 2. 实体在 Archetype 之间移动时，组件数据随之迁移
 * 3. 使用边（Edge）缓存快速找到添加/移除组件后的目标 Archetype
 * 4. 支持预制体继承，子 Archetype 可以继承父 Archetype 的组件
 *
 * ## 使用示例
 * ```kotlin
 * // Archetype 通常不直接创建，由系统自动管理
 * val archetype = world.archetypeService.getArchetype(entityType)
 *
 * // 获取实体在 Archetype 中的索引
 * val index = world.entityService.runOn(entity) { entityIndex }
 * ```
 *
 * @property world 关联的 ECS 世界
 * @property id 原型唯一标识符
 * @property archetypeType 原型的实体类型（组件组合）
 * @property archetypeProvider 原型提供者，用于创建新原型
 */
data class Archetype(
    override val world: World,
    val id: Int,
    val archetypeType: EntityType,
    val archetypeProvider: ArchetypeProvider
) : WorldOwner {

    private val componentAddEdges = LongSparseArray<Archetype>()
    private val componentRemoveEdges = LongSparseArray<Archetype>()

    @PublishedApi
    internal val table = Table(archetypeType.holdsData(), world.componentService)

    /**
     * 原型中实体的数量
     */
    val size: Int get() = table.entities.size

    /**
     * 预制体实体（如果有）
     *
     * 如果此原型是基于预制体的，返回预制体实体
     */
    val prefab: Entity? by lazy { archetypeType.firstOrNull { it.kind == components.instanceOf }?.target }

    /**
     * 实体类型（包含继承的组件）
     *
     * 如果是预制体实例，会合并预制体的组件类型
     */
    val entityType: EntityType by lazy {
        val prefab = this.prefab ?: return@lazy archetypeType
        val entityType = mutableSetOf<Relation>()
        entityType.addAll(archetypeType)
        world.entityService.runOn(prefab) {
            entityType.addAll(this@runOn.entityType)
        }
        EntityType(entityType.asSequence())
    }

    /**
     * 获取组件索引
     *
     * 查找指定关系在原型的组件表中的索引位置
     *
     * @param entity 实体（用于预制体查找）
     * @param relation 关系对象
     * @return 组件索引，如果不存在返回 null
     */
    fun getComponentIndex(entity: Entity, relation: Relation): ComponentIndex? {
        val entityType = if (world.componentService.isShadedComponent(relation)) archetypeType else table.holdsDataType
        val componentIndex = entityType.indexOf(relation)
        if (componentIndex != -1) return ComponentIndex(entity, componentIndex)
        val prefab = this.prefab ?: return null
        return world.entityService.runOn(prefab) {
            getComponentIndex(prefab, relation)
        }
    }

    /**
     * 过滤出持有数据的组件类型
     *
     * 排除标记组件和共享组件，只返回实际存储数据的组件
     */
    fun EntityType.holdsData(): EntityType {
        val componentService = world.componentService
        return EntityType(filter { componentService.holdsData(it) && !componentService.isShadedComponent(it) })
    }

    /**
     * 添加组件，返回新的原型
     *
     * 使用边缓存优化，避免重复创建相同的原型
     *
     * @param relation 要添加的关系
     * @return 添加组件后的新原型
     */
    operator fun plus(relation: Relation): Archetype {
        return componentAddEdges.getOrPut(relation.data) {
            archetypeProvider.getArchetype(archetypeType + relation).also {
                it.componentRemoveEdges.put(relation.data, this)
            }
        }
    }

    /**
     * 移除组件，返回新的原型
     *
     * 使用边缓存优化，避免重复创建相同的原型
     *
     * @param relation 要移除的关系
     * @return 移除组件后的新原型
     */
    operator fun minus(relation: Relation): Archetype {
        return componentRemoveEdges.getOrPut(relation.data) {
            archetypeProvider.getArchetype(archetypeType - relation).also {
                it.componentAddEdges.put(relation.data, this)
            }
        }
    }
}
