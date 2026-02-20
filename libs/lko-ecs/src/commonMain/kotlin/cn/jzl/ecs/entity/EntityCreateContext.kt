package cn.jzl.ecs.entity

import cn.jzl.ecs.World
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.component.components
import cn.jzl.ecs.relation.component
import cn.jzl.ecs.relation.relation
import cn.jzl.ecs.relation.relations
import cn.jzl.ecs.relation.sharedComponent

/**
 * 实体创建上下文，用于在实体创建时配置组件和关系
 *
 * EntityCreateContext 提供了一系列扩展函数，用于在 [world.entity] 或
 * [world.childOf] 等创建实体的闭包中添加组件和关系。
 *
 * ## 使用示例
 * ```kotlin
 * val player = world.entity {
 *     it.addComponent(Name("Player"))
 *     it.addComponent(Health(100, 100))
 *     it.addTag<ActiveTag>()
 *     it.addRelation<OwnerBy>(ownerEntity)
 * }
 * ```
 *
 * @param entityEditor 实体编辑器，用于实际执行组件和关系的添加操作
 * @property world 关联的 ECS 世界
 */
open class EntityCreateContext(@PublishedApi internal val entityEditor: EntityEditor) : EntityRelationContext {
    override val world: World get() = entityEditor.world
}

/**
 * 添加关系
 *
 * @param kind 关系类型组件 ID
 * @param target 关系目标实体
 */
context(context: EntityCreateContext)
fun Entity.addRelation(kind: ComponentId, target: Entity): Unit = with(context) {
    entityEditor.addRelation(this@addRelation, relations.relation(kind, target))
}

/**
 * 添加带数据的关系
 *
 * @param K 关系类型
 * @param T 目标类型
 * @param data 关系数据
 */
context(context: EntityCreateContext)
inline fun <reified K : Any, reified T> Entity.addRelation(data: K): Unit = with(context) {
    entityEditor.addRelation(this@addRelation, relations.relation<K, T>(), data)
}

/**
 * 添加带数据的关系到指定目标
 *
 * @param K 关系类型
 * @param target 目标实体
 * @param data 关系数据
 */
context(context: EntityCreateContext)
inline fun <reified K : Any> Entity.addRelation(target: Entity, data: K): Unit = with(context) {
    entityEditor.addRelation(this@addRelation, relations.relation<K>(target), data)
}

/**
 * 添加关系到指定目标
 *
 * @param K 关系类型
 * @param target 目标实体
 */
context(context: EntityCreateContext)
inline fun <reified K : Any> Entity.addRelation(target: Entity): Unit = with(context) {
    entityEditor.addRelation(this@addRelation, relations.relation<K>(target))
}

/**
 * 添加关系（类型和目标都通过泛型指定）
 *
 * @param K 关系类型
 * @param T 目标类型
 */
context(context: EntityCreateContext)
inline fun <reified K : Any, reified T> Entity.addRelation(): Unit = with(context) {
    entityEditor.addRelation(this@addRelation, relations.relation<K, T>())
}

/**
 * 添加组件
 *
 * @param C 组件类型
 * @param component 组件实例
 */
context(context: EntityCreateContext)
inline fun <reified C : Any> Entity.addComponent(component: C): Unit = with(context) {
    entityEditor.addRelation(this@addComponent, relations.component<C>(), component)
}

/**
 * 添加共享组件
 *
 * @param C 组件类型
 * @param component 组件实例
 */
context(context: EntityCreateContext)
inline fun <reified C : Any> Entity.addSharedComponent(component: C): Unit = with(context) {
    entityEditor.addRelation(this@addSharedComponent, relations.sharedComponent<C>(), component)
}

/**
 * 添加共享组件（无数据）
 *
 * @param C 组件类型
 */
context(context: EntityCreateContext)
inline fun <reified C : Any> Entity.addSharedComponent(): Unit = with(context) {
    entityEditor.addRelation(this@addSharedComponent, relations.sharedComponent<C>())
}

/**
 * 添加共享组件（通过组件 ID）
 *
 * @param kind 组件类型 ID
 */
context(context: EntityCreateContext)
fun Entity.addSharedComponent(kind: ComponentId): Unit = with(context) {
    entityEditor.addRelation(this@addSharedComponent, relations.sharedComponent(kind))
}

/**
 * 添加标签
 *
 * 标签是一种特殊的组件，不包含数据，仅用于标记实体
 *
 * @param C 标签类型（sealed class）
 */
context(context: EntityCreateContext)
inline fun <reified C : Any> Entity.addTag(): Unit = with(context) {
    entityEditor.addRelation(this@addTag, relations.component<C>())
}

/**
 * 添加标签（通过组件 ID）
 *
 * @param kind 标签类型 ID
 */
context(context: EntityCreateContext)
fun Entity.addTag(kind: ComponentId): Unit = with(context) {
    entityEditor.addRelation(this@addTag, relations.component(kind))
}

/**
 * 设置父实体
 *
 * 建立当前实体与指定实体的父子关系
 *
 * @param parent 父实体
 */
context(context: EntityCreateContext)
fun Entity.parent(parent: Entity): Unit = with(context) {
    addRelation(components.childOf, parent)
}
