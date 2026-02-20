package cn.jzl.ecs.observer

import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.Query

/**
 * 可执行观察者接口
 *
 * ExecutableObserver 定义了观察者构建过程中的可执行阶段，
 * 支持添加过滤查询和最终执行创建观察者。
 *
 * ## 使用流程
 * 1. 通过 [filter] 添加额外的查询条件
 * 2. 通过 [exec] 定义事件处理函数并创建观察者
 *
 * ## 使用示例
 * ```kotlin
 * world.observe<OnUpdated>(Health::class)
 *     .filter(world.query { PlayerContext(this) })  // 只监听玩家
 *     .exec {
 *         // 处理事件
 *     }
 * ```
 *
 * @param Context 观察者上下文类型
 * @see ObserverBuilder
 */
interface ExecutableObserver<Context> {
    /**
     * 添加额外的匹配查询
     *
     * 只有同时满足所有查询条件的实体才会触发此观察者
     *
     * @param query 要添加的查询
     * @return 可执行观察者实例（支持链式调用）
     */
    fun filter(vararg query: Query<out EntityQueryContext>): ExecutableObserver<Context>

    /**
     * 执行观察者创建
     *
     * 使用提供的处理函数创建最终的观察者实例
     *
     * @param handle 事件处理函数，在上下文环境中执行
     * @return 创建的观察者实例
     */
    fun exec(handle: Context.() -> Unit): Observer
}
