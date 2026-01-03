package cn.jzl.ecs.observer

import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.Query
import cn.jzl.ecs.relation.EntityType

data class Observer(
    val queries: List<Query<out EntityQueryContext>>,
    val involvedRelations: EntityType,
    val listenToEvents: Sequence<Entity>,
    val mustHoldData: Boolean = false,
    val handle: ObserverHandle
) : AutoCloseable {

    private val unsubscribes = mutableListOf<() -> Unit>()

    fun unsubscribe(onUnsubscribe: () -> Unit) {
        unsubscribes.add(onUnsubscribe)
    }

    override fun close() {
        unsubscribes.forEach { it() }
        unsubscribes.clear()
    }
}