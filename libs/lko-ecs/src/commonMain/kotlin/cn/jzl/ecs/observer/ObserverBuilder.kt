package cn.jzl.ecs.observer

import cn.jzl.ecs.ECSDsl
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.Query
import cn.jzl.ecs.relation.EntityType

/**
 * 观察者构建器，用于配置和创建观察者
 *
 * ObserverBuilder 是创建观察者的核心类，支持：
 * - 指定监听的事件类型
 * - 指定涉及的关系组件
 * - 绑定额外的查询条件
 * - 定义事件处理函数
 *
 * ## 使用示例
 * ```kotlin
 * val observer = world.observe<OnUpdated>(Health::class)
 *     .involved(Health::class)
 *     .filter(world.query { PlayerContext(this) })
 *     .exec {
 *         println("Player health updated: ${health.current}")
 *     }
 * ```
 *
 * @param Context 观察者上下文类型
 * @property events 事件构建器，提供上下文和事件配置
 * @property involvedComponents 涉及的关系组件类型
 * @property matchQueries 额外的匹配查询列表
 */
data class ObserverBuilder<Context>(
    val events: ObserverEventsBuilder<Context>,
    val involvedComponents: EntityType,
    val matchQueries: List<Query<out EntityQueryContext>>
) : ExecutableObserver<Context> {

    /**
     * 添加额外的匹配查询
     *
     * 只有同时满足所有查询条件的实体才会触发观察者
     *
     * @param query 要添加的查询
     * @return 新的观察者构建器实例
     */
    override fun filter(vararg query: Query<out EntityQueryContext>): ExecutableObserver<Context> = copy(matchQueries = matchQueries + query)

    /**
     * 执行观察者创建
     *
     * 使用提供的处理函数创建最终的观察者实例
     *
     * @param handle 事件处理函数
     * @return 观察者实例
     */
    @ECSDsl
    override fun exec(handle: Context.() -> Unit): Observer {
        val observer = Observer(
            matchQueries,
            involvedComponents,
            events.listenToEvents,
            events.mustHoldData,
        ) { entity, event, involved -> events.provideContext(entity, event, involved).handle() }
        events.onBuild(observer)
        return observer
    }
}
