package cn.jzl.ecs.entity

import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.relation.*

open class EntityUpdateContext(entityEditor: EntityEditor) : EntityCreateContext(entityEditor)

context(context: EntityUpdateContext)
fun Entity.removeRelation(relation: Relation): Unit = with(context) {
    entityEditor.removeRelation(this@removeRelation, relation)
}

context(context: EntityUpdateContext)
inline fun <reified K> Entity.removeRelation(target: Entity): Unit = with(context) {
    removeRelation(relations.relation<K>(target))
}

context(context: EntityUpdateContext)
inline fun <reified K, reified T> Entity.removeRelation(): Unit = with(context) {
    removeRelation(relations.relation<K, T>())
}

context(context: EntityUpdateContext)
inline fun <reified C : Any> Entity.removeSharedComponent(): Unit = with(context) {
    removeRelation(relations.sharedComponent<C>())
}

context(context: EntityUpdateContext)
fun Entity.removeSharedComponent(kind: ComponentId): Unit = with(context) {
    removeRelation(relations.sharedComponent(kind))
}

context(context: EntityUpdateContext)
inline fun <reified C : Any> Entity.removeComponent(): Unit = with(context) {
    removeRelation(relations.component<C>())
}

context(context: EntityUpdateContext)
fun Entity.removeComponent(kind: ComponentId): Unit = with(context) {
    removeRelation(relations.component(kind))
}

context(context: EntityUpdateContext)
inline fun <reified C : Any> Entity.removeTag(): Unit = with(context) {
    removeRelation(relations.component<C>())
}

context(context: EntityUpdateContext)
fun Entity.removeTag(kind: ComponentId): Unit = with(context) {
    removeRelation(relations.component(kind))
}
