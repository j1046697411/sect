package cn.jzl.ecs.relation

import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.entity.Entity

/**
 * 获取当前世界的关系提供者
 */
val WorldOwner.relations: RelationProvider get() = world.relations

/**
 * 创建关系对象
 *
 * 关系由类型（kind）和目标实体（target）组成
 *
 * @param kind 关系类型组件 ID
 * @param target 关系目标实体
 * @return 关系对象
 */
fun RelationProvider.relation(kind: ComponentId, target: Entity): Relation = Relation(kind, target)

/**
 * 创建仅指定类型的关系
 *
 * 用于查询时匹配任意目标的关系
 *
 * @param kind 关系类型组件 ID
 * @return 关系对象，目标为通配符
 */
fun RelationProvider.kind(kind: ComponentId): Relation = relation(kind, comps.any)

/**
 * 创建仅指定目标的关系
 *
 * 用于查询时匹配任意类型的关系
 *
 * @param target 关系目标实体
 * @return 关系对象，类型为通配符
 */
fun RelationProvider.target(target: Entity): Relation = relation(comps.any, target)

/**
 * 创建指定类型的关系（泛型版本）
 *
 * @param K 关系类型（sealed class）
 * @return 关系对象，目标为通配符
 */
inline fun <reified K> RelationProvider.kind(): Relation = kind(comps.id<K>())

/**
 * 创建指定目标的关系（泛型版本）
 *
 * @param T 目标实体类型标记
 * @return 关系对象，类型为通配符
 */
inline fun <reified T> RelationProvider.target(): Relation = target(comps.id<T>())

/**
 * 创建指定类型和目标的关系
 *
 * @param K 关系类型
 * @param T 目标实体类型
 * @return 关系对象
 */
inline fun <reified K, reified T> RelationProvider.relation(): Relation = relation<K>(comps.id<T>())

/**
 * 创建指定类型和目标实体的关系
 *
 * @param K 关系类型
 * @param target 目标实体
 * @return 关系对象
 */
inline fun <reified K> RelationProvider.relation(target: Entity): Relation = relation(comps.id<K>(), target)

/**
 * 创建组件关系
 *
 * 用于表示实体拥有某个组件的关系查询
 *
 * @param C 组件类型
 * @return 关系对象
 */
inline fun <reified C> RelationProvider.component(): Relation = component(comps.id<C>())

/**
 * 创建组件关系
 *
 * @param kind 组件类型 ID
 * @return 关系对象
 */
fun RelationProvider.component(kind: ComponentId): Relation = relation(kind, comps.componentOf)

/**
 * 创建共享组件关系
 *
 * 用于表示实体共享某个组件数据的关系查询
 *
 * @param C 组件类型
 * @return 关系对象
 */
inline fun <reified C> RelationProvider.sharedComponent(): Relation = sharedComponent(comps.id<C>())

/**
 * 创建共享组件关系
 *
 * @param kind 组件类型 ID
 * @return 关系对象
 */
fun RelationProvider.sharedComponent(kind: ComponentId): Relation = relation(kind, comps.sharedOf)
