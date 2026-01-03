package cn.jzl.ecs.observer

import cn.jzl.ecs.World
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.Query
import cn.jzl.ecs.relation.EntityType
import cn.jzl.ecs.relation.Relation

abstract class ObserverEventsBuilder<Context> : ExecutableObserver<Context> {
    abstract val world: World
    abstract val listenToEvents: Sequence<ComponentId>
    abstract val mustHoldData: Boolean
    abstract val onBuild: (Observer) -> Entity

    abstract fun provideContext(entity: Entity, event: Any?, involvedRelation: Relation): Context

    override fun filter(vararg query: Query<out EntityQueryContext>): ObserverBuilder<Context> {
        return ObserverBuilder(this, EntityType.empty, query.toList())
    }

    override fun exec(handle: Context.() -> Unit): Observer = filter().exec(handle)

    fun involving(entityType: EntityType) : ObserverBuilder<Context> {
        return ObserverBuilder(this, entityType, emptyList())
    }

    fun involving(relations: Sequence<Relation>) : ObserverBuilder<Context> {
        return involving(EntityType(relations.map { it.data }.toSet().toLongArray()))
    }
}