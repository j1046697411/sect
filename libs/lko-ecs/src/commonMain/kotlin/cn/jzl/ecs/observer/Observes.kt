package cn.jzl.ecs.observer

import cn.jzl.di.new
import cn.jzl.di.singleton
import cn.jzl.ecs.*
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.addRelation
import cn.jzl.ecs.entity.parent
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.Query
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.relation.RelationProvider

val observeAddon = createAddon("observeAddon") {
    injects {
        this bind singleton { new(::ObserveService) }
    }
    components {
        world.componentId<Observer>()
    }
}

inline fun <reified Context, reified E : EntityQueryContext> ExecutableObserver<Context>.exec(
    query: Query<E>,
    noinline handle: Context.(E) -> Unit
): Observer = filter(query).exec { handle(query.context) }

inline fun <reified Context, reified E1 : EntityQueryContext, reified E2 : EntityQueryContext> ExecutableObserver<Context>.exec(
    query1: Query<E1>,
    query2: Query<E2>,
    noinline handle: Context.(E1, E2) -> Unit
): Observer = filter(query1, query2).exec { handle(query1.context, query2.context) }

inline fun <reified E> World.observe(): ObserverEventsBuilder<ObserverContext> = observe<E>(components.observerId)

inline fun World.observe(
    crossinline configure: suspend SequenceScope<ComponentId>.(RelationProvider) -> Unit
): ObserverEventsBuilder<ObserverContext> = observe(components.observerId, configure)

inline fun <reified E> World.observeWithData(): ObserverEventsBuilder<ObserverContextWithData<E>> =
    observeWithData(components.observerId)

inline fun <reified E> World.observeWithData(
    noinline configure: suspend SequenceScope<ComponentId>.(RelationProvider) -> Unit
): ObserverEventsBuilder<ObserverContextWithData<E>> = observeWithData<E>(components.observerId, configure)

inline fun <reified E> World.observe(entity: Entity): ObserverEventsBuilder<ObserverContext> = observe(entity) {
    yield(components.id<E>())
}

inline fun <reified E> World.observeWithData(entity: Entity): ObserverEventsBuilder<ObserverContextWithData<E>> =
    observeWithData(entity) {
        yield(components.id<E>())
    }

inline fun World.observe(
    entity: Entity,
    crossinline configure: suspend SequenceScope<ComponentId>.(RelationProvider) -> Unit
): ObserverEventsBuilder<ObserverContext> {
    val listenToEvents = sequence { configure(relations) }
    return ObserverWithoutData(this, listenToEvents) {
        attachObserver(entity, it)
    }
}

inline fun <reified E> World.observeWithData(
    entity: Entity,
    noinline configure: suspend SequenceScope<ComponentId>.(RelationProvider) -> Unit
): ObserverEventsBuilder<ObserverContextWithData<E>> {
    val listenToEvents = sequence { configure(relations) }
    return ObserverWithData(this, listenToEvents) {
        attachObserver(entity, it)
    }
}

@PublishedApi
internal fun World.attachObserver(entity: Entity, observer: Observer): Entity = entityService.create(false) {
    it.addRelation(entity, observer)
    it.parent(entity)
    observer.listenToEvents.forEach { eventId ->
        it.addRelation(components.eventOf, eventId)
    }
    observer.unsubscribe { world.destroy(it) }
}

inline fun <reified E> World.emit(
    entity: Entity,
    event: E,
    involvedRelation: Relation = observeService.notInvolvedRelation
) {
    observeService.dispatch(entity, components.id<E>(), event, involvedRelation)
}

inline fun <reified E> World.emit(entity: Entity, involvedRelation: Relation = observeService.notInvolvedRelation) {
    observeService.dispatch(entity, components.id<E>(), null, involvedRelation)
}

context(worldOwner: WorldOwner)
inline fun <reified E> Entity.emit(
    event: E,
    involvedRelation: Relation = worldOwner.world.observeService.notInvolvedRelation
): Unit = with(worldOwner) {
    world.emit<E>(this@emit, event, involvedRelation)
}

context(worldOwner: WorldOwner)
inline fun <reified E> Entity.emit(
    involvedRelation: Relation = worldOwner.world.observeService.notInvolvedRelation
): Unit = with(worldOwner) {
    world.emit<E>(this@emit, involvedRelation)
}

context(worldOwner: WorldOwner)
inline fun Entity.observe(
    crossinline configure: suspend SequenceScope<ComponentId>.(RelationProvider) -> Unit
): ObserverEventsBuilder<ObserverContext> = with(worldOwner) {
    world.observe(this@observe, configure)
}

context(worldOwner: WorldOwner)
inline fun <reified E> Entity.observe(): ObserverEventsBuilder<ObserverContext> = observe {
    yield(worldOwner.world.components.id<E>())
}

context(worldOwner: WorldOwner)
inline fun <reified E> Entity.observeWithData(): ObserverEventsBuilder<ObserverContextWithData<E>> = observeWithData {
    yield(worldOwner.world.components.id<E>())
}

context(worldOwner: WorldOwner)
inline fun <reified E> Entity.observeWithData(
    noinline configure: suspend SequenceScope<ComponentId>.(RelationProvider) -> Unit
): ObserverEventsBuilder<ObserverContextWithData<E>> = with(worldOwner) {
    world.observeWithData(this@observeWithData, configure)
}
