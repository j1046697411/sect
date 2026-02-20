package cn.jzl.ecs.entity

import androidx.collection.MutableIntList
import androidx.collection.mutableIntListOf
import kotlin.jvm.JvmInline

/**
 * 实体集合内联值类
 *
 * Entities 是对 [MutableIntList] 的轻量级包装，用于高效存储实体集合。
 * 使用内联值类实现，避免运行时装箱开销，同时提供类型安全的实体操作。
 *
 * ## 特性
 * - 使用 Int 存储实体数据（打包的 ID 和版本）
 * - 支持标准的集合操作（add, remove, contains 等）
 * - 实现了 [Collection] 接口，可与其他集合互操作
 *
 * ## 使用场景
 * - 原型表中存储实体列表
 * - 批量实体操作
 * - 需要高性能实体存储的场景
 *
 * @param entities 内部存储的实体数据列表
 */
@JvmInline
value class Entities(private val entities: MutableIntList = mutableIntListOf()) : Collection<Entity> {

    /**
     * 实体数量
     */
    override val size: Int get() = entities.size

    /**
     * 检查是否为空
     */
    override fun isEmpty(): Boolean = entities.isEmpty()

    /**
     * 添加实体
     *
     * @param entity 要添加的实体
     */
    fun add(entity: Entity) {
        entities.add(entity.data)
    }

    /**
     * 获取指定索引处的实体
     *
     * @param index 索引
     * @return 实体实例
     */
    operator fun get(index: Int): Entity = Entity(entities[index])

    /**
     * 设置指定索引处的实体
     *
     * @param index 索引
     * @param entity 实体实例
     */
    operator fun set(index: Int, entity: Entity) {
        entities[index] = entity.data
    }

    /**
     * 检查是否包含指定实体
     *
     * @param element 要检查的实体
     * @return 如果包含返回 true
     */
    override fun contains(element: Entity): Boolean {
        return element.data in entities
    }

    /**
     * 检查是否包含所有指定实体
     *
     * @param elements 要检查的实体集合
     * @return 如果全部包含返回 true
     */
    override fun containsAll(elements: Collection<Entity>): Boolean = elements.all { it in this }

    /**
     * 移除指定索引处的实体
     *
     * 使用交换移除策略（将最后一个元素移到被移除位置），保证 O(1) 复杂度
     *
     * @param index 要移除的索引
     * @return 被移除的实体
     */
    fun removeAt(index: Int): Entity {
        val lastIndex = entities.lastIndex
        val removedEntity = Entity(entities[index])
        if (lastIndex != index) entities[index] = entities[lastIndex]
        entities.removeAt(lastIndex)
        return removedEntity
    }

    /**
     * 返回实体迭代器
     */
    override fun iterator(): Iterator<Entity> = iterator {
        entities.forEach { yield(Entity(it)) }
    }
}
