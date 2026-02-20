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

/**
 * 观察者系统插件
 *
 * 提供 ECS 事件观察功能，包括：
 * - 观察者服务注册
 * - 观察者组件类型注册
 */
val observeAddon = createAddon("observeAddon") {
    injects {
        this bind singleton { new(::ObserveService) }
    }
    components {
        world.componentId<Observer>()
    }
}

/**
 * 在可执行观察者上绑定查询（单查询）
 *
 * @param Context 观察者上下文类型
 * @param E 查询上下文类型
 * @param query 要绑定的查询
 * @param handle 处理函数
 * @return 观察者实例
 */
inline fun <reified Context, reified E : EntityQueryContext> ExecutableObserver<Context>.exec(
    query: Query<E>,
    noinline handle: Context.(E) -> Unit
): Observer = filter(query).exec { handle(query.context) }

/**
 * 在可执行观察者上绑定查询（双查询）
 *
 * @param Context 观察者上下文类型
 * @param E1 第一个查询上下文类型
 * @param E2 第二个查询上下文类型
 * @param query1 第一个查询
 * @param query2 第二个查询
 * @param handle 处理函数
 * @return 观察者实例
 */
inline fun <reified Context, reified E1 : EntityQueryContext, reified E2 : EntityQueryContext> ExecutableObserver<Context>.exec(
    query1: Query<E1>,
    query2: Query<E2>,
    noinline handle: Context.(E1, E2) -> Unit
): Observer = filter(query1, query2).exec { handle(query1.context, query2.context) }

/**
 * 创建全局事件观察者（无数据）
 *
 * @param E 事件类型
 * @return 事件构建器
 */
inline fun <reified E> World.observe(): ObserverEventsBuilder<ObserverContext> = observe<E>(components.observerId)

/**
 * 创建全局事件观察者（自定义配置）
 *
 * @param configure 事件配置函数
 * @return 事件构建器
 */
inline fun World.observe(
    crossinline configure: suspend SequenceScope<ComponentId>.(RelationProvider) -> Unit
): ObserverEventsBuilder<ObserverContext> = observe(components.observerId, configure)

/**
 * 创建全局事件观察者（带数据）
 *
 * @param E 事件数据类型
 * @return 事件构建器
 */
inline fun <reified E> World.observeWithData(): ObserverEventsBuilder<ObserverContextWithData<E>> =
    observeWithData(components.observerId)

/**
 * 创建全局事件观察者（带数据，自定义配置）
 *
 * @param E 事件数据类型
 * @param configure 事件配置函数
 * @return 事件构建器
 */
inline fun <reified E> World.observeWithData(
    noinline configure: suspend SequenceScope<ComponentId>.(RelationProvider) -> Unit
): ObserverEventsBuilder<ObserverContextWithData<E>> = observeWithData<E>(components.observerId, configure)

/**
 * 为指定实体创建事件观察者
 *
 * @param E 事件类型
 * @param entity 目标实体
 * @return 事件构建器
 */
inline fun <reified E> World.observe(entity: Entity): ObserverEventsBuilder<ObserverContext> = observe(entity) {
    yield(components.id<E>())
}

/**
 * 为指定实体创建事件观察者（带数据）
 *
 * @param E 事件数据类型
 * @param entity 目标实体
 * @return 事件构建器
 */
inline fun <reified E> World.observeWithData(entity: Entity): ObserverEventsBuilder<ObserverContextWithData<E>> =
    observeWithData(entity) {
        yield(components.id<E>())
    }

/**
 * 为指定实体创建事件观察者（自定义配置）
 *
 * @param entity 目标实体
 * @param configure 事件配置函数
 * @return 事件构建器
 */
inline fun World.observe(
    entity: Entity,
    crossinline configure: suspend SequenceScope<ComponentId>.(RelationProvider) -> Unit
): ObserverEventsBuilder<ObserverContext> {
    val listenToEvents = sequence { configure(relations) }
    return ObserverWithoutData(this, listenToEvents) {
        attachObserver(entity, it)
    }
}

/**
 * 为指定实体创建事件观察者（带数据，自定义配置）
 *
 * @param E 事件数据类型
 * @param entity 目标实体
 * @param configure 事件配置函数
 * @return 事件构建器
 */
inline fun <reified E> World.observeWithData(
    entity: Entity,
    noinline configure: suspend SequenceScope<ComponentId>.(RelationProvider) -> Unit
): ObserverEventsBuilder<ObserverContextWithData<E>> {
    val listenToEvents = sequence { configure(relations) }
    return ObserverWithData(this, listenToEvents) {
        attachObserver(entity, it)
    }
}

/**
 * 将观察者附加到实体
 *
 * 内部方法，创建观察者实体并建立关系
 *
 * @param entity 目标实体
 * @param observer 观察者实例
 * @return 观察者实体
 */
@PublishedApi
internal fun World.attachObserver(entity: Entity, observer: Observer): Entity = entityService.create(false) {
    it.addRelation(entity, observer)
    it.parent(entity)
    observer.listenToEvents.forEach { eventId ->
        it.addRelation(components.eventOf, eventId)
    }
    observer.unsubscribe { world.destroy(it) }
}

/**
 * 触发事件（带数据）
 *
 * @param E 事件数据类型
 * @param entity 触发事件的实体
 * @param event 事件数据
 * @param involvedRelation 涉及的关系
 */
inline fun <reified E> World.emit(
    entity: Entity,
    event: E,
    involvedRelation: Relation = observeService.notInvolvedRelation
) {
    observeService.dispatch(entity, components.id<E>(), event, involvedRelation)
}

/**
 * 触发事件（无数据）
 *
 * @param E 事件类型
 * @param entity 触发事件的实体
 * @param involvedRelation 涉及的关系
 */
inline fun <reified E> World.emit(entity: Entity, involvedRelation: Relation = observeService.notInvolvedRelation) {
    observeService.dispatch(entity, components.id<E>(), null, involvedRelation)
}

/**
 * 在当前上下文中触发事件（带数据）
 *
 * @param E 事件数据类型
 * @param event 事件数据
 * @param involvedRelation 涉及的关系
 */
context(worldOwner: WorldOwner)
inline fun <reified E> Entity.emit(
    event: E,
    involvedRelation: Relation = worldOwner.world.observeService.notInvolvedRelation
): Unit = with(worldOwner) {
    world.emit<E>(this@emit, event, involvedRelation)
}

/**
 * 在当前上下文中触发事件（无数据）
 *
 * @param E 事件类型
 * @param involvedRelation 涉及的关系
 */
context(worldOwner: WorldOwner)
inline fun <reified E> Entity.emit(
    involvedRelation: Relation = worldOwner.world.observeService.notInvolvedRelation
): Unit = with(worldOwner) {
    world.emit<E>(this@emit, involvedRelation)
}

/**
 * 在当前上下文中创建事件观察者（自定义配置）
 *
 * @param configure 事件配置函数
 * @return 事件构建器
 */
context(worldOwner: WorldOwner)
inline fun Entity.observe(
    crossinline configure: suspend SequenceScope<ComponentId>.(RelationProvider) -> Unit
): ObserverEventsBuilder<ObserverContext> = with(worldOwner) {
    world.observe(this@observe, configure)
}

/**
 * 在当前上下文中创建事件观察者
 *
 * @param E 事件类型
 * @return 事件构建器
 */
context(worldOwner: WorldOwner)
inline fun <reified E> Entity.observe(): ObserverEventsBuilder<ObserverContext> = observe {
    yield(worldOwner.world.components.id<E>())
}

/**
 * 在当前上下文中创建事件观察者（带数据）
 *
 * @param E 事件数据类型
 * @return 事件构建器
 */
context(worldOwner: WorldOwner)
inline fun <reified E> Entity.observeWithData(): ObserverEventsBuilder<ObserverContextWithData<E>> = observeWithData {
    yield(worldOwner.world.components.id<E>())
}

/**
 * 在当前上下文中创建事件观察者（带数据，自定义配置）
 *
 * @param E 事件数据类型
 * @param configure 事件配置函数
 * @return 事件构建器
 */
context(worldOwner: WorldOwner)
inline fun <reified E> Entity.observeWithData(
    noinline configure: suspend SequenceScope<ComponentId>.(RelationProvider) -> Unit
): ObserverEventsBuilder<ObserverContextWithData<E>> = with(worldOwner) {
    world.observeWithData(this@observeWithData, configure)
}
