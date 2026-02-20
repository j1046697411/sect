package cn.jzl.ecs.observer

import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.Query
import cn.jzl.ecs.relation.EntityType

/**
 * 观察者，用于监听 ECS 世界中的事件和变化
 *
 * Observer 提供一种机制来监听实体的创建、销毁、组件变更等事件。
 * 它通过关联的查询来定义监听范围，当匹配的实体发生变化时触发回调。
 *
 * ## 使用示例
 * ```kotlin
 * // 创建观察者监听健康变化
 * val observer = world.observe<OnUpdated>(Health::class) { entity, oldHealth, newHealth ->
 *     println("Entity ${entity.id} health changed: $oldHealth -> $newHealth")
 * }
 *
 * // 不再需要时取消订阅
 * observer.close()
 * ```
 *
 * ## 生命周期
 * Observer 实现了 [AutoCloseable]，使用完毕后应调用 [close] 方法
 * 取消订阅，避免内存泄漏。
 *
 * @property queries 关联的查询列表，定义监听范围
 * @property involvedRelations 涉及的关系类型
 * @property listenToEvents 监听的事件实体序列
 * @property mustHoldData 是否必须持有数据
 * @property handle 观察者处理句柄，用于实际的事件处理
 */
data class Observer(
    val queries: List<Query<out EntityQueryContext>>,
    val involvedRelations: EntityType,
    val listenToEvents: Sequence<Entity>,
    val mustHoldData: Boolean = false,
    val handle: ObserverHandle
) : AutoCloseable {

    private val unsubscribes = mutableListOf<() -> Unit>()

    /**
     * 注册取消订阅回调
     *
     * 当观察者被关闭时，这些回调会被依次执行
     *
     * @param onUnsubscribe 取消订阅时要执行的回调
     */
    fun unsubscribe(onUnsubscribe: () -> Unit) {
        unsubscribes.add(onUnsubscribe)
    }

    /**
     * 关闭观察者，取消所有订阅
     *
     * 调用此方法后，观察者将不再接收事件通知
     */
    override fun close() {
        unsubscribes.forEach { it() }
        unsubscribes.clear()
    }
}
