package cn.jzl.ecs.observer

import cn.jzl.ecs.World
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.relation.Relation

data class ObserverWithData<E>(
    override val world: World,
    override val listenToEvents: Sequence<ComponentId>,
    override val onBuild: (Observer) -> Entity
) : ObserverEventsBuilder<ObserverContextWithData<E>>() {

    override val mustHoldData: Boolean get() = true

    private val context = object : ObserverContextWithData<E> {

        var data: E? = null

        override val world: World get() = this@ObserverWithData.world
        override var entity: Entity = Entity.ENTITY_INVALID
        override var involvedRelation: Relation = world.observeService.notInvolvedRelation
        override val event: E get() = requireNotNull(data) { "Event is null" }
    }

    @Suppress("UNCHECKED_CAST")
    override fun provideContext(entity: Entity, event: Any?, involvedRelation: Relation): ObserverContextWithData<E> {
        context.entity = entity
        context.data = event as? E
        context.involvedRelation = involvedRelation
        return context
    }
}