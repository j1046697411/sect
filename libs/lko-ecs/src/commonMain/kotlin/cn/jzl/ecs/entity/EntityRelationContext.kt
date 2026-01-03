package cn.jzl.ecs.entity

import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.component.components
import cn.jzl.ecs.relation.*

interface EntityRelationContext : WorldOwner

context(context: EntityRelationContext)
val Entity.entityType: EntityType
    get() = with(context) {
        world.entityService.runOn(this@entityType) { archetypeType }
    }

context(context: EntityRelationContext)
inline fun <reified K> Entity.getRelation(target: Entity): K = with(context) {
    return world.relationService.getRelation(this@getRelation, relations.relation<K>(target)) as K
}

context(context: EntityRelationContext)
inline fun <reified K, reified T> Entity.getRelation(): K = with(context) { getRelation(components.id<T>()) }

context(context: EntityRelationContext)
inline fun <reified C> Entity.getComponent(): C = with(context) { getRelation(components.componentOf) }

context(context: EntityRelationContext)
inline fun <reified C> Entity.getSharedComponent(): C = with(context) { getRelation(components.sharedOf) }

context(context: EntityRelationContext)
fun Entity.hasRelation(relation: Relation): Boolean = with(context) {
    return world.relationService.hasRelation(this@hasRelation, relation)
}

context(context: EntityRelationContext)
fun Entity.hasTag(tag: ComponentId): Boolean = with(context) {
    return world.relationService.hasRelation(this@hasTag, relations.component(tag))
}

context(context: EntityRelationContext)
inline fun <reified C> Entity.hasTag(): Boolean = with(context) {
    return hasTag(components.id<C>())
}

context(context: EntityRelationContext)
fun Entity.hasComponent(kind: ComponentId): Boolean = with(context) {
    return world.relationService.hasRelation(this@hasComponent, relations.component(kind))
}

context(context: EntityRelationContext)
inline fun <reified C> Entity.hasComponent(): Boolean = with(context) {
    return hasComponent(components.id<C>())
}

context(context: EntityRelationContext)
fun Entity.hasSharedComponent(kind: ComponentId): Boolean = with(context) {
    return world.relationService.hasRelation(this@hasSharedComponent, relations.sharedComponent(kind))
}

context(context: EntityRelationContext)
inline fun <reified C> Entity.hasSharedComponent(): Boolean = with(context) {
    return hasSharedComponent(components.id<C>())
}
