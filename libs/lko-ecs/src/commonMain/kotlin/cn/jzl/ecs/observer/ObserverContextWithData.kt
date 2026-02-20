package cn.jzl.ecs.observer

/**
 * 带事件数据的观察者上下文接口
 *
 * ObserverContextWithData 继承自 [ObserverContext]，增加了对事件数据的访问能力。
 * 用于需要获取事件具体信息的观察者场景。
 *
 * ## 使用示例
 * ```kotlin
 * data class DamageEvent(val amount: Int, val source: Entity)
 *
 * world.observeWithData<DamageEvent>()
 *     .exec {
 *         val damage = event.amount
 *         val attacker = event.source
 *         println("Took $damage damage from $attacker")
 *     }
 * ```
 *
 * @param E 事件数据类型
 * @property event 事件数据实例
 * @see ObserverContext 基础上下文接口
 */
interface ObserverContextWithData<E> : ObserverContext {
    /**
     * 事件数据
     *
     * 触发此观察者的事件的具体数据
     */
    val event: E
}
