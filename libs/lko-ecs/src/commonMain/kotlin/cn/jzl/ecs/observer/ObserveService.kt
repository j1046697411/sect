package cn.jzl.ecs.observer

import androidx.collection.mutableLongObjectMapOf
import cn.jzl.ecs.World
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.component.components
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.family.FamilyBuilder
import cn.jzl.ecs.family.relation
import cn.jzl.ecs.isActive
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.OptionalGroup
import cn.jzl.ecs.query.Query
import cn.jzl.ecs.query.forEach
import cn.jzl.ecs.relation.*

@PublishedApi
internal class ObserveService(private val world: World) {

    private val queries = mutableLongObjectMapOf<Query<ObserveEntityQueryContext>>()

    val notInvolvedRelation: Relation by lazy { Relation(world.components.any, world.components.any) }

    private fun getQuery(target: Entity, eventId: ComponentId): Query<ObserveEntityQueryContext> {
        return queries.getOrPut(Relation(target, eventId).data) {
            world.query { ObserveEntityQueryContext(world, target, eventId) }
        }
    }

    fun dispatch(entity: Entity, eventId: ComponentId, event: Any? = null, involved: Relation = notInvolvedRelation) {
        getQuery(entity, eventId).forEach {
            it.targetObserver?.handle(entity, event, involved)
            it.globalObserver?.handle(entity, event, involved)
        }
    }

    private fun Observer.handle(entity: Entity, event: Any?, involved: Relation) {
        if (mustHoldData && event == null) return
        if (involved != notInvolvedRelation && involvedRelations.isNotEmpty()) {
            if (involved.target == world.components.any && involvedRelations.none { it.kind == involved.kind }) return
            if (involved.kind == world.components.any && involvedRelations.none { it.target == involved.target }) return
            if (involved !in involvedRelations) return
        }
        if (queries.isNotEmpty() && world.isActive(entity)) {
            world.entityService.runOn(entity) { entityIndex ->
                if (queries.all { query -> this in query }) {
                    queries.forEach {
                        it.context.updateCache(this)
                    }
                    handle.handle(entity, event, involved)
                    queries.forEach { it.context.apply(entityIndex) {} }
                }
            }
        } else {
            handle.handle(entity, event, involved)
        }
    }

    private class ObserveEntityQueryContext(world: World, target: Entity, private val eventId: ComponentId) :
        EntityQueryContext(world) {
        val targetObserver: Observer? by relation<Observer?>(target, OptionalGroup.One)
        val globalObserver: Observer? by relation<Observer?>(world.components.observerId, OptionalGroup.One)
        override fun FamilyBuilder.configure() {
            relation(relations.relation(components.eventOf, eventId))
        }
    }
}