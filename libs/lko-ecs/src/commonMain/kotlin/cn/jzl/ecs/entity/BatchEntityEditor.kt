package cn.jzl.ecs.entity

import cn.jzl.core.list.SortSet
import cn.jzl.ecs.World
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.relation.kind
import kotlin.jvm.JvmInline

/**
 * 批量实体编辑器，用于批量处理实体的关系和组件操作
 *
 * BatchEntityEditor 实现了 [EntityEditor] 接口，提供批量添加/移除关系和组件的能力。
 * 它会将操作缓存到 [operates] 和 [singleRelationOperates] 中，然后在 [apply] 时统一执行，
 * 这样可以优化性能并确保操作的原子性。
 *
 * ## 工作原理
 * 1. 收集所有添加/移除操作到操作集合中
 * 2. 处理单例关系（确保同一类型只有一个关系）
 * 3. 调用 [apply] 时一次性提交所有操作
 * 4. 触发相应的事件通知
 *
 * ## 使用场景
 * - 实体创建时的批量组件添加
 * - 实体编辑时的多组件更新
 * - 需要原子性操作的场景
 *
 * @param world 关联的 ECS 世界
 * @param entity 要编辑的目标实体
 * @property operates 普通关系操作集合
 * @property singleRelationOperates 单例关系操作集合
 */
data class BatchEntityEditor(override val world: World, @PublishedApi internal var entity: Entity) : EntityEditor {

    private val operates = SortSet<EntityRelationOperate> { a, b -> a.relation.compareTo(b.relation) }

    private val singleRelationOperates =
        SortSet<EntityRelationOperate> { a, b -> a.relation.kind.data.compareTo(b.relation.kind.data) }

    /**
     * 添加带数据的关系
     *
     * @param entity 目标实体
     * @param relation 关系对象
     * @param data 关系数据
     * @throws IllegalStateException 如果实体不匹配或关系不持有数据
     */
    override fun addRelation(entity: Entity, relation: Relation, data: Any) {
        check(entity == this.entity) { "entity must be $entity" }
        check(world.componentService.holdsData(relation)) { "relation $relation must hold data" }
        if (world.componentService.isShadedComponent(relation)) {
            world.shadedComponentService[relation] = data
            addRelation(entity, relation)
            return
        }
        val addEntityRelationOperate = AddEntityRelationOperateWithData(relation, data)
        if (world.componentService.isSingleRelation(relation)) {
            singleRelationOperates.add(addEntityRelationOperate)
        } else {
            operates.add(addEntityRelationOperate)
        }
    }

    /**
     * 添加关系（无数据）
     *
     * @param entity 目标实体
     * @param relation 关系对象
     * @throws IllegalStateException 如果实体不匹配
     */
    override fun addRelation(entity: Entity, relation: Relation) {
        check(entity == this.entity) { "entity must be $entity" }
        if (!world.componentService.isShadedComponent(relation)) {
            check(!world.componentService.holdsData(relation))
        }
        val addEntityRelationOperate = AddEntityRelationOperate(relation)
        if (world.componentService.isSingleRelation(relation)) {
            singleRelationOperates.add(addEntityRelationOperate)
        } else {
            operates.add(addEntityRelationOperate)
        }
    }

    /**
     * 移除关系
     *
     * @param entity 目标实体
     * @param relation 要移除的关系
     * @throws IllegalStateException 如果实体不匹配
     */
    override fun removeRelation(entity: Entity, relation: Relation) {
        check(entity == this.entity) { "entity must be $entity" }
        val removeEntityRelationOperate = RemoveEntityRelationOperate(relation)
        if (world.componentService.isSingleRelation(relation)) {
            singleRelationOperates.add(removeEntityRelationOperate)
        } else {
            operates.add(RemoveEntityRelationOperate(relation))
        }
    }

    /**
     * 应用所有操作
     *
     * 处理单例关系操作，然后批量提交所有操作到 [RelationService]
     *
     * @param world 关联的 ECS 世界
     * @param event 是否触发事件
     */
    fun apply(world: World, event: Boolean,) {
        applySingleRelationOperates()
        if (operates.isEmpty()) return
        world.relationService.updateRelations(entity, operates, event)
        operates.clear()
    }

    /**
     * 应用单例关系操作
     *
     * 对于单例关系，需要先移除同类型的现有关系，确保只有一个实例
     */
    private fun applySingleRelationOperates() {
        if (singleRelationOperates.isEmpty()) return
        val entityService = world.entityService
        entityService.runOn(entity) {
            singleRelationOperates.forEach { operate ->
                if (operate is AddEntityRelationOperate || operate is AddEntityRelationOperateWithData) {
                    archetypeType.filter { it.kind == operate.relation.kind && it != operate.relation }.forEach {
                        operates.add(RemoveEntityRelationOperate(it))
                    }
                }
                operates.add(operate)
            }
        }
        singleRelationOperates.clear()
    }

    /**
     * 实体关系操作接口
     *
     * 标记接口，用于统一处理添加和移除操作
     */
    sealed interface EntityRelationOperate {
        /**
         * 操作涉及的关系
         */
        val relation: Relation
    }

    /**
     * 添加带数据的关系操作
     *
     * @param relation 关系对象
     * @param data 关系数据
     */
    data class AddEntityRelationOperateWithData(override val relation: Relation, val data: Any) : EntityRelationOperate

    /**
     * 添加关系操作（无数据）
     *
     * @param relation 关系对象
     */
    @JvmInline
    value class AddEntityRelationOperate(override val relation: Relation) : EntityRelationOperate

    /**
     * 移除关系操作
     *
     * @param relation 关系对象
     */
    @JvmInline
    value class RemoveEntityRelationOperate(override val relation: Relation) : EntityRelationOperate
}
