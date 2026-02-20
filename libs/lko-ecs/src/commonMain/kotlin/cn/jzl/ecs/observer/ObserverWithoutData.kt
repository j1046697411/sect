package cn.jzl.ecs.observer

import cn.jzl.ecs.World
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.relation.Relation

/**
 * 无数据观察者事件构建器
 *
 * ObserverWithoutData 用于创建不携带事件数据的观察者。
 * 适用于只需要知道事件发生了，但不需要具体数据的场景。
 *
 * ## 使用示例
 * ```kotlin
 * world.observe<OnUpdated>(Health::class)
 *     .exec {
 *         println("Health was updated for entity ${entity.id}")
 *     }
 * ```
 *
 * @property world 关联的 ECS 世界
 * @property listenToEvents 要监听的事件类型序列
 * @property onBuild 观察者构建完成回调
 */
data class ObserverWithoutData(
    override val world: World,
    override val listenToEvents: Sequence<ComponentId>,
    override val onBuild: (Observer) -> Entity
) : ObserverEventsBuilder<ObserverContext>() {

    /**
     * 是否必须持有事件数据
     *
     * 对于无数据观察者，始终返回 false
     */
    override val mustHoldData: Boolean get() = false

    private val observerContext = object : ObserverContext {
        override val world: World get() = this@ObserverWithoutData.world
        override var entity: Entity = Entity.Companion.ENTITY_INVALID
        override var involvedRelation: Relation = world.observeService.notInvolvedRelation
    }

    /**
     * 提供观察者上下文
     *
     * @param entity 触发事件的实体
     * @param event 事件数据（对于无数据观察者忽略）
     * @param involvedRelation 涉及的关系
     * @return 观察者上下文实例
     */
    override fun provideContext(entity: Entity, event: Any?, involvedRelation: Relation): ObserverContext {
        observerContext.entity = entity
        observerContext.involvedRelation = involvedRelation
        return observerContext
    }
}
