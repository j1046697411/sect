package cn.jzl.ecs.relation

import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.entity.Entity

val WorldOwner.relations: RelationProvider get() = world.relations

fun RelationProvider.relation(kind: ComponentId, target: Entity): Relation = Relation(kind, target)
fun RelationProvider.kind(kind: ComponentId): Relation = relation(kind, comps.any)
fun RelationProvider.target(target: Entity): Relation = relation(comps.any, target)
inline fun <reified K> RelationProvider.kind(): Relation = kind(comps.id<K>())
inline fun <reified T> RelationProvider.target(): Relation = target(comps.id<T>())
inline fun <reified K, reified T> RelationProvider.relation(): Relation = relation<K>(comps.id<T>())
inline fun <reified K> RelationProvider.relation(target: Entity): Relation = relation(comps.id<K>(), target)
inline fun <reified C> RelationProvider.component(): Relation = component(comps.id<C>())
fun RelationProvider.component(kind: ComponentId): Relation = relation(kind, comps.componentOf)
inline fun <reified C> RelationProvider.sharedComponent(): Relation = sharedComponent(comps.id<C>())
fun RelationProvider.sharedComponent(kind: ComponentId): Relation = relation(kind, comps.sharedOf)


