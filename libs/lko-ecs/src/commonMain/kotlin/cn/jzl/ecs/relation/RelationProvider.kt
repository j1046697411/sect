package cn.jzl.ecs.relation

import cn.jzl.ecs.component.Components
import kotlin.jvm.JvmInline

/**
 * 关系提供者内联值类
 *
 * RelationProvider 是对 [Components] 的轻量级包装，提供创建关系对象的便捷方法。
 * 使用内联值类实现，避免运行时开销。
 *
 * ## 使用方式
 * 通常通过 [relations] 扩展属性获取 RelationProvider 实例，
 * 然后使用其提供的辅助函数创建关系对象。
 *
 * ## 使用示例
 * ```kotlin
 * val relation = relations.relation<OwnerBy>(targetEntity)
 * val componentRelation = relations.component<Health>()
 * val sharedComponent = relations.sharedComponent<GlobalConfig>()
 * ```
 *
 * @param comps 组件管理器实例
 * @see Relations 提供的关系创建辅助函数
 */
@JvmInline
value class RelationProvider(@PublishedApi internal val comps: Components)
