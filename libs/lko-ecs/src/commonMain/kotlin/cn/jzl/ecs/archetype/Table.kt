package cn.jzl.ecs.archetype

import cn.jzl.ecs.component.ComponentStoreFactory
import cn.jzl.ecs.entity.Entities
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.relation.EntityType
import cn.jzl.ecs.relation.Relation

/**
 * 组件表，存储原型中所有实体的组件数据
 *
 * Table 是 Archetype 的内部数据结构，使用 SOA（Structure of Arrays）方式
 * 存储组件数据，确保相同类型的组件数据在内存中连续排列，提高缓存效率。
 *
 * ## 存储结构
 * - entities: 实体 ID 数组
 * - componentArrays: 组件数据数组，每个组件类型一个数组
 *
 * ## 使用示例
 * ```kotlin
 * // Table 通常不直接使用，通过 Archetype 访问
 * val archetype = world.archetypeService.getArchetype(entityType)
 * val entity = archetype.table[0]  // 获取第一个实体
 * ```
 *
 * @param holdsDataType 持有数据的组件类型集合
 * @param componentStoreFactory 组件存储工厂，用于创建组件数组
 */
class Table(
    val holdsDataType: EntityType,
    private val componentStoreFactory: ComponentStoreFactory<Any>
) {
    private val componentArrays by lazy {
        Array(holdsDataType.size) {
            componentStoreFactory.create(holdsDataType[it])
        }
    }

    internal val entities = Entities()

    /**
     * 表中实体的数量
     */
    val size: Int get() = entities.size

    /**
     * 插入实体到表中
     *
     * @param entity 要插入的实体
     * @param provider 组件数据提供者
     * @return 实体在表中的索引
     */
    fun insert(entity: Entity, provider: Relation.(Int) -> Any): Int {
        val size = entities.size
        entities.add(entity)
        if (holdsDataType.isNotEmpty()) {
            holdsDataType.forEachIndexed { index, relation ->
                componentArrays[index].add(relation.provider(index))
            }
        }
        return size
    }

    /**
     * 获取指定索引处的实体
     *
     * @param entityIndex 实体索引
     * @return 实体实例
     */
    operator fun get(entityIndex: Int): Entity = entities[entityIndex]

    /**
     * 获取指定实体的指定组件
     *
     * @param entityIndex 实体索引
     * @param componentIndex 组件索引
     * @return 组件值
     */
    operator fun get(entityIndex: Int, componentIndex: Int): Any {
        require(entityIndex in 0 until entities.size)
        require(componentIndex in 0 until holdsDataType.size)
        return componentArrays[componentIndex][entityIndex]
    }

    /**
     * 设置指定实体的指定组件
     *
     * @param entityIndex 实体索引
     * @param componentIndex 组件索引
     * @param value 组件值
     */
    operator fun set(entityIndex: Int, componentIndex: Int, value: Any) {
        require(entityIndex in 0 until entities.size)
        require(componentIndex in 0 until holdsDataType.size)
        componentArrays[componentIndex][entityIndex] = value
    }

    /**
     * 移除指定索引处的实体及其组件
     *
     * @param entityIndex 实体索引
     */
    fun removeAt(entityIndex: Int) {
        require(entityIndex in 0 until entities.size)
        entities.removeAt(entityIndex)
        if (holdsDataType.isNotEmpty()) {
            componentArrays.forEach { it.removeAt(entityIndex) }
        }
    }
}
