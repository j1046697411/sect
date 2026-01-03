package cn.jzl.ecs.query

import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner

class QueryService(override val world: World) : WorldOwner {
    private val queryCache = mutableMapOf<Any, Query<*>>()

    @Suppress("UNCHECKED_CAST")
    fun <E : EntityQueryContext> query(factory: World.() -> E): Query<E> {
        return queryCache.getOrPut(factory) { Query(world.factory()) } as Query<E>
    }
}