package cn.jzl.ecs.observer

import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.relation.Relation

/**
 * 观察者处理函数式接口
 *
 * ObserverHandle 定义了当观察的事件触发时要执行的回调函数。
 * 这是一个函数式接口 (SAM)，可以使用 Lambda 表达式实现。
 *
 * ## 使用示例
 * ```kotlin
 * val observer = world.observe<OnUpdated>(Health::class).exec {
 *     println("Health updated for entity ${entity.id}")
 * }
 * ```
 *
 * ## 参数说明
 * - entity: 触发事件的实体
 * - event: 事件数据（如果有）
 * - involved: 涉及的关系（如果有）
 *
 * @see Observer
 */
fun interface ObserverHandle {
    /**
     * 处理观察事件
     *
     * @param entity 触发事件的实体
     * @param event 事件数据，如果没有数据则为 null
     * @param involved 涉及的关系，如果没有则为 [ObserveService.notInvolvedRelation]
     */
    fun handle(entity: Entity, event: Any?, involved: Relation)
}
