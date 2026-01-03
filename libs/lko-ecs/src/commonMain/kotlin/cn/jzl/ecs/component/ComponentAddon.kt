package cn.jzl.ecs.component

import cn.jzl.di.new
import cn.jzl.di.singleton
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon

val componentAddon = createAddon<Unit>("componentAddon") {
    injects {
        this bind singleton { new(::ComponentService) }
        this bind singleton { new(::ShadedComponentService) }
        this bind singleton { new(::Components) }
    }
    components {
        world.componentId<Any> { it.tag() }
        world.componentId<ComponentOf> { it.tag() }
        world.componentId<SharedOf> { it.tag() }
        world.componentId<ChildOf> {
            it.tag()
            it.singleRelation()
        }
        world.componentId<InstanceOf> {
            it.tag()
            it.singleRelation()
        }
        world.componentId<EventOf> { it.tag() }
        world.componentId<Prefab> { it.tag() }
        world.componentId<NoInherit> { it.tag() }
        world.componentId<OnInserted> { it.tag() }
        world.componentId<OnRemoved> { it.tag() }
        world.componentId<OnUpdated> { it.tag() }
        world.componentId<OnEntityCreated> { it.tag() }
        world.componentId<OnEntityUpdated> { it.tag() }
        world.componentId<OnEntityDestroyed> { it.tag() }
    }
}