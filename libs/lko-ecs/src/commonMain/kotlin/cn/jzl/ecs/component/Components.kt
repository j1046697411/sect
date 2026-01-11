package cn.jzl.ecs.component

import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.observer.Observer

typealias ComponentId = Entity
typealias Component = Any

val WorldOwner.components: Components get() = world.components

class Components(@PublishedApi internal val componentProvider: ComponentProvider) {
    inline fun <reified C> id(): ComponentId = componentProvider.getOrRegisterEntityForClass(C::class)

    val any: ComponentId = id<Any>()
    val componentOf: ComponentId = id<ComponentOf>()
    val sharedOf: ComponentId = id<SharedOf>()
    val childOf: ComponentId = id<ChildOf>()
    val eventOf: ComponentId = id<EventOf>()
    val prefab: ComponentId = id<Prefab>()
    val instanceOf: ComponentId = id<InstanceOf>()
    val noInherit: ComponentId = id<NoInherit>()

    val onInserted: ComponentId = id<OnInserted>()
    val onRemoved: ComponentId = id<OnRemoved>()
    val onUpdated: ComponentId = id<OnUpdated>()
    val onEntityCreated: ComponentId = id<OnEntityCreated>()
    val onEntityUpdated: ComponentId = id<OnEntityUpdated>()
    val onEntityDestroyed: ComponentId = id<OnEntityDestroyed>()
    val observerId: ComponentId = id<Observer>()
}

sealed class SharedOf

sealed class ComponentOf
sealed class ChildOf
sealed class EventOf

sealed class Prefab
sealed class InstanceOf

sealed class NoInherit

sealed class OnInserted
sealed class OnRemoved
sealed class OnUpdated

sealed class OnEntityCreated
sealed class OnEntityUpdated
sealed class OnEntityDestroyed