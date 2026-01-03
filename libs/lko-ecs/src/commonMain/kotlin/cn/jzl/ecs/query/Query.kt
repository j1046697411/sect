package cn.jzl.ecs.query

import cn.jzl.ecs.World
import cn.jzl.ecs.archetype.Archetype
import cn.jzl.ecs.family.Family

class Query<C : EntityQueryContext>(@PublishedApi internal val context: C) : QueryStream<C>, QueryStreamScope {

    override val world: World get() = context.world
    override val family: Family by lazy { context.build() }

    operator fun contains(archetype: Archetype): Boolean = family.familyMatcher.match(archetype)

    override fun collect(collector: QueryCollector<C>) = with(collector) {
        if (family.archetypes.isEmpty()) return
        runCatching {
            family.archetypes.forEach { archetype ->
                var entityIndex = 0
                context.updateCache(archetype)
                while (entityIndex < archetype.size) {
                    context.apply(entityIndex) {
                        val oldEntity = context.entity
                        emit(context)
                        if (oldEntity == context.entity) entityIndex++
                    }
                }
            }
        }.recoverCatching {
            if (it !is AbortQueryException) throw it
        }.getOrThrow()
    }

    override fun close() {
    }
}