package cn.jzl.ecs.observer

import cn.jzl.ecs.World
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.relation.Relation

data class ObserverWithoutData(
    override val world: World,
    override val listenToEvents: Sequence<ComponentId>,
    override val onBuild: (Observer) -> Entity
) : ObserverEventsBuilder<ObserverContext>() {

    override val mustHoldData: Boolean get() = false

    private val observerContext = object : ObserverContext {
        override val world: World get() = this@ObserverWithoutData.world
        override var entity: Entity = Entity.Companion.ENTITY_INVALID
        override var involvedRelation: Relation = world.observeService.notInvolvedRelation
    }

    override fun provideContext(entity: Entity, event: Any?, involvedRelation: Relation): ObserverContext {
        observerContext.entity = entity
        observerContext.involvedRelation = involvedRelation
        return observerContext
    }
}