package cn.jzl.ecs.archetype

import androidx.collection.mutableObjectListOf
import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.relation.EntityType

/**
 * 原型服务，管理所有原型实例
 *
 * ArchetypeService 负责创建、缓存和提供原型实例。
 * 确保相同组件组合的原型只存在一个实例，避免数据冗余。
 *
 * ## 主要功能
 * - 根据组件类型创建或获取原型
 * - 缓存已创建的原型，避免重复创建
 * - 管理根原型（无任何组件的原型）
 * - 向家族服务注册新原型
 *
 * ## 使用示例
 * ```kotlin
 * // 获取或创建原型
 * val archetype = world.archetypeService.getArchetype(entityType)
 *
 * // 通过 ID 获取原型
 * val archetypeById = world.archetypeService[0]
 * ```
 *
 * @param world 关联的 ECS 世界
 * @property rootArchetype 根原型（无任何组件的原型）
 */
class ArchetypeService(override val world: World) : ArchetypeProvider, WorldOwner {
    private val typeToArchetypeMap = mutableMapOf<EntityType, Archetype>()
    private val archetypes = mutableObjectListOf<Archetype>()

    /**
     * 根原型（无任何组件的原型）
     *
     * 所有实体的初始原型，新创建的实体首先被放入根原型
     */
    override val rootArchetype: Archetype = getArchetype(EntityType.empty)

    /**
     * 获取或创建指定类型的原型
     *
     * 如果该类型的原型已存在，返回缓存的实例；
     * 否则创建新原型并注册到家族服务。
     *
     * @param entityType 实体类型（组件组合）
     * @return 原型实例
     */
    override fun getArchetype(entityType: EntityType): Archetype {
        return typeToArchetypeMap.getOrPut(entityType) { createArchetype(entityType) }
    }

    /**
     * 创建新原型
     *
     * @param entityType 实体类型
     * @return 创建的原型
     */
    private fun createArchetype(entityType: EntityType): Archetype {
        val archetype = Archetype(world, archetypes.size, entityType, this)
        archetypes.add(archetype)
        world.familyService.registerArchetype(archetype)
        return archetype
    }

    /**
     * 通过 ID 获取原型
     *
     * @param id 原型 ID
     * @return 原型实例
     * @throws IllegalArgumentException 如果 ID 超出范围
     */
    operator fun get(id: Int): Archetype {
        require(id in archetypes.indices) { "Archetype id $id is out of range" }
        return archetypes[id]
    }
}
