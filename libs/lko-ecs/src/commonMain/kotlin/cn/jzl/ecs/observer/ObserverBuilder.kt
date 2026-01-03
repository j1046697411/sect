package cn.jzl.ecs.observer

import cn.jzl.ecs.ECSDsl
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.Query
import cn.jzl.ecs.relation.EntityType

data class ObserverBuilder<Context>(
    val events: ObserverEventsBuilder<Context>,
    val involvedComponents: EntityType,
    val matchQueries: List<Query<out EntityQueryContext>>
) : ExecutableObserver<Context> {

    override fun filter(vararg query: Query<out EntityQueryContext>): ExecutableObserver<Context> = copy(matchQueries = matchQueries + query)

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