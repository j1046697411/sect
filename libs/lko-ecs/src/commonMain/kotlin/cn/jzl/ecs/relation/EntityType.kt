package cn.jzl.ecs.relation

import kotlin.jvm.JvmInline

/**
 * 实体类型，表示实体的组件组合
 *
 * EntityType 使用 [LongArray] 值类实现，存储实体拥有的所有关系（包括组件）。
 * 数组保持有序，支持高效的二分查找。
 *
 * ## 特性
 * - 不可变：创建后不能修改，修改操作返回新实例
 * - 有序：内部数组保持排序，支持二分查找
 * - 轻量：值类实现，避免装箱开销
 *
 * ## 使用示例
 * ```kotlin
 * // 创建实体类型
 * val entityType = EntityType(sequenceOf(relation1, relation2))
 *
 * // 添加组件
 * val newType = entityType + relation
 *
 * // 移除组件
 * val removedType = entityType - relation
 *
 * // 检查包含
 * if (relation in entityType) {
 *     // 实体有此组件
 * }
 * ```
 *
 * @param data 内部存储的关系数据数组（已排序）
 */
@JvmInline
value class EntityType(private val data: LongArray) : Sequence<Relation> {

    init {
        data.sort()
    }

    /**
     * 关系数量
     */
    val size: Int get() = data.size

    /**
     * 是否非空
     */
    fun isNotEmpty(): Boolean = size > 0

    /**
     * 是否为空
     */
    fun isEmpty(): Boolean = size == 0

    /**
     * 获取指定索引处的关系
     *
     * @param index 索引
     * @return 关系对象
     */
    operator fun get(index: Int): Relation = Relation(data[index])

    /**
     * 获取指定索引处的关系（安全）
     *
     * @param index 索引
     * @return 关系对象，如果索引越界返回 null
     */
    fun getOrNull(index: Int): Relation? = data.getOrNull(index)?.let { Relation(it) }

    /**
     * 查找关系的索引
     *
     * @param relation 要查找的关系
     * @return 索引，如果不存在返回 -1
     */
    fun indexOf(relation: Relation): Int = binarySearch(relation)

    /**
     * 检查是否包含指定关系
     *
     * @param relation 关系对象
     * @return 如果包含返回 true
     */
    operator fun contains(relation: Relation): Boolean = binarySearch(relation) >= 0

    /**
     * 添加关系，返回新的实体类型
     *
     * @param relation 要添加的关系
     * @return 新的实体类型实例
     */
    operator fun plus(relation: Relation): EntityType = EntityType(data + relation.data)

    /**
     * 移除关系，返回新的实体类型
     *
     * @param relation 要移除的关系
     * @return 新的实体类型实例
     */
    operator fun minus(relation: Relation): EntityType = EntityType(data.filter { it != relation.data }.toLongArray())

    /**
     * 二分查找关系
     *
     * @param relation 要查找的关系
     * @return 索引，如果不存在返回 -1
     */
    @PublishedApi
    internal fun binarySearch(relation: Relation): Int {
        var left = 0
        var right = data.size - 1
        while (left <= right) {
            val mid = (left + right) ushr 1
            val midVal = data[mid]
            when {
                midVal < relation.data -> left = mid + 1
                midVal > relation.data -> right = mid - 1
                else -> return mid // 找到元素
            }
        }
        return -1
    }

    /**
     * 返回关系迭代器
     */
    override fun iterator(): Iterator<Relation> = iterator {
        for (i in data.indices) {
            yield(Relation(data[i]))
        }
    }

    companion object {
        /**
         * 空实体类型
         */
        val empty: EntityType = EntityType(longArrayOf())

        /**
         * 从关系序列创建实体类型
         *
         * @param relations 关系序列
         * @return 实体类型实例
         */
        operator fun invoke(relations: Sequence<Relation>): EntityType =
            EntityType(relations.map { it.data }.toSet().toLongArray())
    }
}
