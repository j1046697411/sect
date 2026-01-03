package cn.jzl.ecs.entity

import cn.jzl.ecs.World
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.component.components
import cn.jzl.ecs.relation.component
import cn.jzl.ecs.relation.relation
import cn.jzl.ecs.relation.relations
import cn.jzl.ecs.relation.sharedComponent

open class EntityCreateContext(@PublishedApi internal val entityEditor: EntityEditor) : EntityRelationContext {
    override val world: World get() = entityEditor.world
}

context(context: EntityCreateContext)
fun Entity.addRelation(kind: ComponentId, target: Entity): Unit = with(context) {
    entityEditor.addRelation(this@addRelation, relations.relation(kind, target))
}

context(context: EntityCreateContext)
inline fun <reified K : Any, reified T> Entity.addRelation(data: K): Unit = with(context) {
    entityEditor.addRelation(this@addRelation, relations.relation<K, T>(), data)
}

context(context: EntityCreateContext)
inline fun <reified K : Any> Entity.addRelation(target: Entity, data: K): Unit = with(context) {
    entityEditor.addRelation(this@addRelation, relations.relation<K>(target), data)
}

context(context: EntityCreateContext)
inline fun <reified K : Any> Entity.addRelation(target: Entity): Unit = with(context) {
    entityEditor.addRelation(this@addRelation, relations.relation<K>(target))
}

context(context: EntityCreateContext)
inline fun <reified K : Any, reified T> Entity.addRelation(): Unit = with(context) {
    entityEditor.addRelation(this@addRelation, relations.relation<K, T>())
}

context(context: EntityCreateContext)
inline fun <reified C : Any> Entity.addComponent(component: C): Unit = with(context) {
    entityEditor.addRelation(this@addComponent, relations.component<C>(), component)
}

context(context: EntityCreateContext)
inline fun <reified C : Any> Entity.addSharedComponent(component: C): Unit = with(context) {
    entityEditor.addRelation(this@addSharedComponent, relations.sharedComponent<C>(), component)
}


context(context: EntityCreateContext)
inline fun <reified C : Any> Entity.addSharedComponent(): Unit = with(context) {
    entityEditor.addRelation(this@addSharedComponent, relations.sharedComponent<C>())
}

context(context: EntityCreateContext)
fun Entity.addSharedComponent(kind: ComponentId): Unit = with(context) {
    entityEditor.addRelation(this@addSharedComponent, relations.sharedComponent(kind))
}


context(context: EntityCreateContext)
inline fun <reified C : Any> Entity.addTag(): Unit = with(context) {
    entityEditor.addRelation(this@addTag, relations.component<C>())
}

context(context: EntityCreateContext)
fun Entity.addTag(kind: ComponentId): Unit = with(context) {
    entityEditor.addRelation(this@addTag, relations.component(kind))
}

context(context: EntityCreateContext)
fun Entity.parent(parent: Entity): Unit = with(context) {
    addRelation(components.childOf, parent)
}