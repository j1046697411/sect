package cn.jzl.ecs.observer

import cn.jzl.ecs.World
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.Query
import cn.jzl.ecs.relation.EntityType
import cn.jzl.ecs.relation.Relation

/**
 * 观察者事件构建器抽象类
 *
 * ObserverEventsBuilder 是创建观察者的入口类，负责：
 * - 定义要监听的事件类型序列
 * - 指定是否需要事件数据
 * - 提供上下文创建能力
 * - 支持链式配置过滤条件和执行
 *
 * ## 使用示例
 * ```kotlin
 * // 基础用法
 * world.observe<OnUpdated>(Health::class)
 *     .exec { /* 处理 */ }
 *
 * // 带过滤
 * world.observe<OnUpdated>(Health::class)
 *     .filter(world.query { PlayerContext(this) })
 *     .involving(world.relations.component<Health>())
 *     .exec { /* 处理 */ }
 * ```
 *
 * @param Context 观察者上下文类型
 * @property world 关联的 ECS 世界
 * @property listenToEvents 要监听的事件类型序列
 * @property mustHoldData 是否必须持有事件数据
 * @property onBuild 观察者构建完成回调
 */
abstract class ObserverEventsBuilder<Context> : ExecutableObserver<Context> {
    abstract val world: World
    abstract val listenToEvents: Sequence<ComponentId>
    abstract val mustHoldData: Boolean
    abstract val onBuild: (Observer) -> Entity

    /**
     * 创建观察者上下文
     *
     * @param entity 触发事件的实体
     * @param event 事件数据
     * @param involvedRelation 涉及的关系
     * @return 上下文实例
     */
    abstract fun provideContext(entity: Entity, event: Any?, involvedRelation: Relation): Context

    /**
     * 添加过滤查询
     *
     * @param query 查询条件
     * @return 观察者构建器
     */
    override fun filter(vararg query: Query<out EntityQueryContext>): ObserverBuilder<Context> {
        return ObserverBuilder(this, EntityType.empty, query.toList())
    }

    /**
     * 直接执行创建观察者
     *
     * @param handle 事件处理函数
     * @return 观察者实例
     */
    override fun exec(handle: Context.() -> Unit): Observer = filter().exec(handle)

    /**
     * 指定涉及的关系组件类型
     *
     * @param entityType 关系组件类型集合
     * @return 观察者构建器
     */
    fun involving(entityType: EntityType): ObserverBuilder<Context> {
        return ObserverBuilder(this, entityType, emptyList())
    }

    /**
     * 指定涉及的关系
     *
     * @param relations 关系序列
     * @return 观察者构建器
     */
    fun involving(relations: Sequence<Relation>): ObserverBuilder<Context> {
        return involving(EntityType(relations.map { it.data }.toSet().toLongArray()))
    }
}
