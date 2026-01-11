package cn.jzl.ecs.serialization.entity

import cn.jzl.ecs.component.Component
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.serialization.core.SerializationContext
import kotlin.reflect.KClass

inline fun <reified T : Component> Entity.setPersisting(
    context: SerializationContext,
    component: T,
    kClass: KClass<out T> = T::class,
): T {
    set(component, kClass)
    val persistableRelation = Relation(
        kind = context.world.components.id<Persistable>(),
        target = this
    )
    context.world.relationService.addRelation(this, persistableRelation)
    Persistable().updateHash(component)
    return component
}

fun Entity.setAllPersisting(
    context: SerializationContext,
    components: Collection<Component>,
    override: Boolean = true,
) {
    components.forEach {
        if (override || !has(it::class)) {
            setPersisting(context, it, it::class)
        }
    }
}

inline fun <reified T : Component> Entity.getOrSetPersisting(
    context: SerializationContext,
    kClass: KClass<out T> = T::class,
    default: () -> T,
): T {
    return get(kClass) ?: default().also { setPersisting(context, it, kClass) }
}

fun Entity.getAllPersisting(context: SerializationContext): Set<Component> {
    val persistingComponents = mutableSetOf<Component>()
    val persistableComponentId = context.world.components.id<Persistable>()

    context.world.entityService.runOn(this) { entityIndex ->
        val archetype = context.world.archetypeService.getArchetype(entityIndex)
        archetype.archetypeType.forEach { relation ->
            if (relation.kind == persistableComponentId) {
                val component = context.world.relationService.getRelation(this, relation)
                if (component != null && component is Component) {
                    persistingComponents.add(component)
                }
            }
        }
    }

        return persistingComponents
    }
}

fun Entity.getAllNotPersisting(context: SerializationContext): Set<Component> {
    val allComponents = getAll()
    val persistingComponents = getAllPersisting(context)
    return allComponents - persistingComponents
}

fun Entity.markAsPersisted(context: SerializationContext) {
    val persistingComponents = getAllPersisting(context)
    persistingComponents.forEach { component ->
        val persistableRelation = Relation(
            kind = context.world.components.id<Persistable>(),
            target = this
        )
        context.world.relationService.addRelation(this, persistableRelation, Persistable().updateHash(component))
    }
}