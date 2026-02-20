package cn.jzl.ecs.entity

import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.component.components
import cn.jzl.ecs.relation.*

/**
 * 实体关系上下文接口
 *
 * EntityRelationContext 是 [EntityCreateContext] 和 [EntityUpdateContext] 的基接口，
 * 提供查询实体组件和关系的能力。实现了 [WorldOwner]，可以访问世界服务。
 *
 * ## 使用场景
 * - 查询实体的组件数据
 * - 检查实体是否有特定标签
 * - 获取实体间的关系
 *
 * ## 使用示例
 * ```kotlin
 * fun checkHealth(entity: Entity) {
 *     if (entity.hasComponent<Health>()) {
 *         val health = entity.getComponent<Health>()
 *         println("Health: ${health.current}")
 *     }
 * }
 * ```
 */
interface EntityRelationContext : WorldOwner

/**
 * 获取实体类型
 */
context(context: EntityRelationContext)
val Entity.entityType: EntityType
    get() = with(context) {
        world.entityService.runOn(this@entityType) { archetypeType }
    }

/**
 * 获取关系到指定目标的数据
 *
 * @param K 关系类型
 * @param target 目标实体
 * @return 关系数据
 */
context(context: EntityRelationContext)
inline fun <reified K> Entity.getRelation(target: Entity): K = with(context) {
    return world.relationService.getRelation(this@getRelation, relations.relation<K>(target)) as K
}

/**
 * 获取关系数据
 *
 * @param K 关系类型
 * @param T 目标类型
 * @return 关系数据
 */
context(context: EntityRelationContext)
inline fun <reified K, reified T> Entity.getRelation(): K = with(context) { getRelation(components.id<T>()) }

/**
 * 获取组件
 *
 * @param C 组件类型
 * @return 组件实例
 */
context(context: EntityRelationContext)
inline fun <reified C> Entity.getComponent(): C = with(context) { getRelation(components.componentOf) }

/**
 * 获取共享组件
 *
 * @param C 组件类型
 * @return 组件实例
 */
context(context: EntityRelationContext)
inline fun <reified C> Entity.getSharedComponent(): C = with(context) { getRelation(components.sharedOf) }

/**
 * 检查是否有指定关系
 *
 * @param relation 关系对象
 * @return 如果存在该关系返回 true
 */
context(context: EntityRelationContext)
fun Entity.hasRelation(relation: Relation): Boolean = with(context) {
    return world.relationService.hasRelation(this@hasRelation, relation)
}

/**
 * 检查是否有指定标签
 *
 * @param tag 标签类型 ID
 * @return 如果存在该标签返回 true
 */
context(context: EntityRelationContext)
fun Entity.hasTag(tag: ComponentId): Boolean = with(context) {
    return world.relationService.hasRelation(this@hasTag, relations.component(tag))
}

/**
 * 检查是否有指定标签
 *
 * @param C 标签类型
 * @return 如果存在该标签返回 true
 */
context(context: EntityRelationContext)
inline fun <reified C> Entity.hasTag(): Boolean = with(context) {
    return hasTag(components.id<C>())
}

/**
 * 检查是否有指定组件
 *
 * @param kind 组件类型 ID
 * @return 如果存在该组件返回 true
 */
context(context: EntityRelationContext)
fun Entity.hasComponent(kind: ComponentId): Boolean = with(context) {
    return world.relationService.hasRelation(this@hasComponent, relations.component(kind))
}

/**
 * 检查是否有指定组件
 *
 * @param C 组件类型
 * @return 如果存在该组件返回 true
 */
context(context: EntityRelationContext)
inline fun <reified C> Entity.hasComponent(): Boolean = with(context) {
    return hasComponent(components.id<C>())
}

/**
 * 检查是否有指定共享组件
 *
 * @param kind 组件类型 ID
 * @return 如果存在该共享组件返回 true
 */
context(context: EntityRelationContext)
fun Entity.hasSharedComponent(kind: ComponentId): Boolean = with(context) {
    return world.relationService.hasRelation(this@hasSharedComponent, relations.sharedComponent(kind))
}

/**
 * 检查是否有指定共享组件
 *
 * @param C 组件类型
 * @return 如果存在该共享组件返回 true
 */
context(context: EntityRelationContext)
inline fun <reified C> Entity.hasSharedComponent(): Boolean = with(context) {
    return hasSharedComponent(components.id<C>())
}
