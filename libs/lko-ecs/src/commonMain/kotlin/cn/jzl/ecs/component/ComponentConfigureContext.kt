package cn.jzl.ecs.component

import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.entity.EntityCreateContext
import cn.jzl.ecs.entity.id
import kotlin.jvm.JvmInline

@JvmInline
value class ComponentConfigureContext(private val entityCreateContext: EntityCreateContext) : WorldOwner {
    override val world: World get() = entityCreateContext.world
}

context(owner: WorldOwner)
inline fun <reified C> World.componentId(
    crossinline configuration: ComponentConfigureContext.(ComponentId) -> Unit
): ComponentId {
    val componentId = components.id<C>()
    owner.world.entityService.configure(componentId, false) {
        val context = ComponentConfigureContext(this)
        context.configuration(it)
    }
    return componentId
}

inline fun <reified C> World.componentId(): ComponentId = components.id<C>()

context(context: ComponentConfigureContext)
fun ComponentId.tag(): Unit = with(context) {
    world.componentService.entityTags.set(id)
}

context(context: ComponentConfigureContext)
fun ComponentId.singleRelation(): Unit = with(context) {
    world.componentService.singleRelationBits.set(id)
}