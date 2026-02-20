package cn.jzl.ecs.entity

import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.relation.*

/**
 * 实体更新上下文，用于在实体编辑时修改组件和关系
 *
 * EntityUpdateContext 继承自 [EntityCreateContext]，除了支持创建时的所有操作外，
 * 还提供了移除组件和关系的能力。用于 [world.editor] 闭包中。
 *
 * ## 使用示例
 * ```kotlin
 * world.editor(player) {
 *     it.addComponent(health.copy(current = 80))  // 更新组件
 *     it.removeTag<ActiveTag>()                    // 移除标签
 *     it.removeComponent<Buff>()                   // 移除组件
 * }
 * ```
 *
 * @param entityEditor 实体编辑器
 */
open class EntityUpdateContext(entityEditor: EntityEditor) : EntityCreateContext(entityEditor)

/**
 * 移除关系
 *
 * @param relation 要移除的关系
 */
context(context: EntityUpdateContext)
fun Entity.removeRelation(relation: Relation): Unit = with(context) {
    entityEditor.removeRelation(this@removeRelation, relation)
}

/**
 * 移除关系到指定目标
 *
 * @param K 关系类型
 * @param target 目标实体
 */
context(context: EntityUpdateContext)
inline fun <reified K> Entity.removeRelation(target: Entity): Unit = with(context) {
    removeRelation(relations.relation<K>(target))
}

/**
 * 移除关系（类型和目标通过泛型指定）
 *
 * @param K 关系类型
 * @param T 目标类型
 */
context(context: EntityUpdateContext)
inline fun <reified K, reified T> Entity.removeRelation(): Unit = with(context) {
    removeRelation(relations.relation<K, T>())
}

/**
 * 移除共享组件
 *
 * @param C 组件类型
 */
context(context: EntityUpdateContext)
inline fun <reified C : Any> Entity.removeSharedComponent(): Unit = with(context) {
    removeRelation(relations.sharedComponent<C>())
}

/**
 * 移除共享组件（通过组件 ID）
 *
 * @param kind 组件类型 ID
 */
context(context: EntityUpdateContext)
fun Entity.removeSharedComponent(kind: ComponentId): Unit = with(context) {
    removeRelation(relations.sharedComponent(kind))
}

/**
 * 移除组件
 *
 * @param C 组件类型
 */
context(context: EntityUpdateContext)
inline fun <reified C : Any> Entity.removeComponent(): Unit = with(context) {
    removeRelation(relations.component<C>())
}

/**
 * 移除组件（通过组件 ID）
 *
 * @param kind 组件类型 ID
 */
context(context: EntityUpdateContext)
fun Entity.removeComponent(kind: ComponentId): Unit = with(context) {
    removeRelation(relations.component(kind))
}

/**
 * 移除标签
 *
 * @param C 标签类型
 */
context(context: EntityUpdateContext)
inline fun <reified C : Any> Entity.removeTag(): Unit = with(context) {
    removeRelation(relations.component<C>())
}

/**
 * 移除标签（通过组件 ID）
 *
 * @param kind 标签类型 ID
 */
context(context: EntityUpdateContext)
fun Entity.removeTag(kind: ComponentId): Unit = with(context) {
    removeRelation(relations.component(kind))
}
