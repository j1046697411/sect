package cn.jzl.ecs.entity

import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.relation.Relation

/**
 * 实体编辑器接口，用于修改实体的组件和关系
 *
 * EntityEditor 提供底层 API 来添加或移除实体的组件和关系。
 * 通常通过 [EntityCreateContext] 和 [EntityUpdateContext] 使用，
 * 而不是直接使用此接口。
 *
 * ## 使用示例
 * ```kotlin
 * // 通常通过上下文使用
 * world.entity {
 *     it.addComponent(Name("Player"))
 *     it.addRelation<OwnerBy>(ownerEntity)
 * }
 *
 * world.editor(entity) {
 *     it.removeComponent<Health>()
 * }
 * ```
 *
 * @see EntityCreateContext 实体创建上下文
 * @see EntityUpdateContext 实体更新上下文
 */
interface EntityEditor : WorldOwner {
    /**
     * 添加带数据的关系
     *
     * @param entity 目标实体
     * @param relation 关系对象
     * @param data 关系数据
     */
    fun addRelation(entity: Entity, relation: Relation, data: Any)

    /**
     * 添加关系（无数据）
     *
     * @param entity 目标实体
     * @param relation 关系对象
     */
    fun addRelation(entity: Entity, relation: Relation)

    /**
     * 移除关系
     *
     * @param entity 目标实体
     * @param relation 要移除的关系
     */
    fun removeRelation(entity: Entity, relation: Relation)
}
