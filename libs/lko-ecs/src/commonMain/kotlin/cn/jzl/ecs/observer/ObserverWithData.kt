package cn.jzl.ecs.observer

import cn.jzl.ecs.World
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.relation.Relation

/**
 * 带数据观察者事件构建器
 *
 * ObserverWithData 用于创建携带事件数据的观察者。
 * 适用于需要访问事件具体数据的场景。
 *
 * ## 使用示例
 * ```kotlin
 * data class DamageEvent(val amount: Int, val source: Entity)
 *
 * world.observeWithData<DamageEvent>()
 *     .exec {
 *         println("Entity ${entity.id} took ${event.amount} damage from ${event.source}")
 *     }
 * ```
 *
 * @param E 事件数据类型
 * @property world 关联的 ECS 世界
 * @property listenToEvents 要监听的事件类型序列
 * @property onBuild 观察者构建完成回调
 */
data class ObserverWithData<E>(
    override val world: World,
    override val listenToEvents: Sequence<ComponentId>,
    override val onBuild: (Observer) -> Entity
) : ObserverEventsBuilder<ObserverContextWithData<E>>() {

    /**
     * 是否必须持有事件数据
     *
     * 对于带数据观察者，始终返回 true
     */
    override val mustHoldData: Boolean get() = true

    private val context = object : ObserverContextWithData<E> {

        var data: E? = null

        override val world: World get() = this@ObserverWithData.world
        override var entity: Entity = Entity.ENTITY_INVALID
        override var involvedRelation: Relation = world.observeService.notInvolvedRelation
        override val event: E get() = requireNotNull(data) { "Event is null" }
    }

    /**
     * 提供带数据的观察者上下文
     *
     * @param entity 触发事件的实体
     * @param event 事件数据
     * @param involvedRelation 涉及的关系
     * @return 带数据的观察者上下文实例
     */
    @Suppress("UNCHECKED_CAST")
    override fun provideContext(entity: Entity, event: Any?, involvedRelation: Relation): ObserverContextWithData<E> {
        context.entity = entity
        context.data = event as? E
        context.involvedRelation = involvedRelation
        return context
    }
}
