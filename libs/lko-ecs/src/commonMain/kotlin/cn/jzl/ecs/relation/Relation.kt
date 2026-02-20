package cn.jzl.ecs.relation

import cn.jzl.core.bits.fromLowHigh
import cn.jzl.core.bits.high
import cn.jzl.core.bits.low
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.entity.Entity
import kotlin.jvm.JvmInline

/**
 * 关系类，表示实体之间的关联
 *
 * Relation 是 ECS 框架中连接实体的桥梁，由关系类型（kind）和目标实体（target）组成。
 * 使用 [Long] 值类实现，将 kind 和 target 打包存储以提高性能。
 *
 * ## 关系结构
 * - 低 32 位：关系类型（kind），即 ComponentId
 * - 高 32 位：目标实体（target），即 Entity
 *
 * ## 使用示例
 * ```kotlin
 * // 创建关系
 * val relation = Relation(kind, targetEntity)
 *
 * // 获取关系信息
 * println(relation.kind)   // 关系类型
 * println(relation.target) // 目标实体
 * ```
 *
 * @property data 内部存储的 Long 值，包含 kind 和 target
 * @see Relations 提供关系创建辅助函数
 */
@JvmInline
value class Relation @PublishedApi internal constructor(val data: Long) : Comparable<Relation> {

    /**
     * 比较两个关系
     *
     * 按内部 data 值进行比较
     */
    override fun compareTo(other: Relation): Int = data.compareTo(other.data)

    override fun toString(): String = "Relation(kind=${kind}, target=${target})"

    companion object {
        /**
         * 创建关系实例
         *
         * @param kind 关系类型组件 ID
         * @param target 目标实体
         * @return 关系实例
         * @throws IllegalArgumentException 如果目标实体是无效实体
         */
        operator fun invoke(kind: ComponentId, target: Entity): Relation {
            require(target != Entity.ENTITY_INVALID) { "Relation target cannot be ENTITY_INVALID" }
            return Relation(Long.fromLowHigh(kind.data, target.data))
        }
    }
}

/**
 * 获取关系的类型（kind）
 */
val Relation.kind: ComponentId get() = Entity(data.low)

/**
 * 获取关系的目标实体（target）
 */
val Relation.target: Entity get() = Entity(data.high)
