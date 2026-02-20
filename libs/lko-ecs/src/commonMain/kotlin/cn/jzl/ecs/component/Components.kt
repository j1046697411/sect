package cn.jzl.ecs.component

import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.observer.Observer

/**
 * 组件类型标识符，使用实体作为组件的唯一标识
 */
typealias ComponentId = Entity

/**
 * 组件类型别名，任何类都可以作为组件
 */
typealias Component = Any

/**
 * 获取当前世界的组件管理器
 */
val WorldOwner.components: Components get() = world.components

/**
 * 组件管理器，负责组件类型的注册和内置组件标识符的提供
 *
 * Components 管理所有组件类型的注册，并提供 ECS 框架内置的特殊组件标识符，
 * 包括关系类型、事件类型和观察者类型。
 *
 * ## 使用示例
 * ```kotlin
 * // 注册自定义组件
 * val healthId = world.components.id<Health>()
 *
 * // 使用内置关系类型
 * val childId = world.components.childOf
 * ```
 *
 * @property componentProvider 底层组件提供者，负责实际的组件注册
 */
class Components(@PublishedApi internal val componentProvider: ComponentProvider) {
    /**
     * 获取或注册指定类型的组件 ID
     *
     * @param C 组件类型
     * @return 组件类型对应的唯一标识符
     */
    inline fun <reified C> id(): ComponentId = componentProvider.getOrRegisterEntityForClass(C::class)

    // 内置通用组件
    val any: ComponentId = id<Any>()

    // 关系类型组件
    val componentOf: ComponentId = id<ComponentOf>()
    val sharedOf: ComponentId = id<SharedOf>()
    val childOf: ComponentId = id<ChildOf>()
    val eventOf: ComponentId = id<EventOf>()

    // 预制体相关
    val prefab: ComponentId = id<Prefab>()
    val instanceOf: ComponentId = id<InstanceOf>()
    val noInherit: ComponentId = id<NoInherit>()

    // 生命周期事件
    val onInserted: ComponentId = id<OnInserted>()
    val onRemoved: ComponentId = id<OnRemoved>()
    val onUpdated: ComponentId = id<OnUpdated>()
    val onEntityCreated: ComponentId = id<OnEntityCreated>()
    val onEntityUpdated: ComponentId = id<OnEntityUpdated>()
    val onEntityDestroyed: ComponentId = id<OnEntityDestroyed>()
    val observerId: ComponentId = id<Observer>()
}

/**
 * 共享组件关系标记
 *
 * 用于表示组件数据在多个实体间共享
 */
sealed class SharedOf

/**
 * 组件归属关系标记
 *
 * 用于表示实体拥有某个组件
 */
sealed class ComponentOf

/**
 * 父子关系标记
 *
 * 用于建立实体间的层级关系
 */
sealed class ChildOf

/**
 * 事件关系标记
 *
 * 用于事件传播机制
 */
sealed class EventOf

/**
 * 预制体标记
 *
 * 标记一个实体为预制体模板
 */
sealed class Prefab

/**
 * 实例关系标记
 *
 * 用于表示实体是某个预制体的实例
 */
sealed class InstanceOf

/**
 * 不继承标记
 *
 * 用于预制体实例化时标记不应继承的组件
 */
sealed class NoInherit

/**
 * 组件插入事件标记
 */
sealed class OnInserted

/**
 * 组件移除事件标记
 */
sealed class OnRemoved

/**
 * 组件更新事件标记
 */
sealed class OnUpdated

/**
 * 实体创建事件标记
 */
sealed class OnEntityCreated

/**
 * 实体更新事件标记
 */
sealed class OnEntityUpdated

/**
 * 实体销毁事件标记
 */
sealed class OnEntityDestroyed
