package cn.jzl.ecs.relation

import cn.jzl.core.list.SortSet
import cn.jzl.di.instance
import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.archetype.Archetype
import cn.jzl.ecs.archetype.ComponentIndex
import cn.jzl.ecs.archetype.entity
import cn.jzl.ecs.archetype.index
import cn.jzl.ecs.component.components
import cn.jzl.ecs.entity.BatchEntityEditor
import cn.jzl.ecs.entity.BatchEntityEditor.EntityRelationOperate
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityService
import kotlin.text.get

/**
 * 关系服务，管理实体间的关系和组件数据
 *
 * RelationService 是 ECS 框架中处理关系和组件操作的核心服务，负责：
 * - 检查实体是否拥有特定关系/组件
 * - 获取组件数据
 * - 批量更新关系和组件（实体原型迁移）
 * - 触发组件变更事件
 *
 * ## 工作原理
 * 当实体的组件发生变化时，RelationService 负责：
 * 1. 计算新的原型（Archetype）
 * 2. 迁移组件数据到新原型
 * 3. 更新实体记录
 * 4. 触发相应的事件通知
 *
 * @param world 关联的 ECS 世界
 * @property entityService 实体服务，用于实体操作
 */
class RelationService(override val world: World) : WorldOwner {
    private val entityService by world.instance<EntityService>()

    /**
     * 检查实体是否拥有指定关系
     *
     * @param entity 目标实体
     * @param relation 关系对象
     * @return 如果实体拥有该关系返回 true
     */
    fun hasRelation(entity: Entity, relation: Relation): Boolean = entityService.runOn(entity) {
        entityType.indexOf(relation) != -1
    }

    /**
     * 获取实体的关系数据
     *
     * @param entity 目标实体
     * @param relation 关系对象
     * @return 关系数据，如果不存在返回 null
     */
    fun getRelation(entity: Entity, relation: Relation): Any? = entityService.runOn(entity) {
        val componentIndex = getComponentIndex(entity, relation) ?: return@runOn null
        if (entity == componentIndex.entity) return@runOn table[it, componentIndex.index]
        entityService.runOn(componentIndex.entity) { entityIndex -> table[entityIndex, componentIndex.index] }
    }

    /**
     * 获取组件索引
     *
     * @param entity 目标实体
     * @param relation 关系对象
     * @return 组件索引，如果不存在返回 null
     */
    fun getComponentIndex(entity: Entity, relation: Relation): ComponentIndex? = entityService.runOn(entity) {
        getComponentIndex(entity, relation)
    }

    /**
     * 获取关系数据（内部优化版本）
     *
     * @param archetype 实体所在原型
     * @param relation 关系对象
     * @param entityIndex 实体在原型表中的索引
     * @param componentIndex 组件索引
     * @return 关系数据
     */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun getRelation(
        archetype: Archetype,
        relation: Relation,
        entityIndex: Int,
        componentIndex: ComponentIndex
    ): Any? {
        if (world.componentService.isShadedComponent(relation)) return world.shadedComponentService[relation]
        if (componentIndex.entity == Entity.ENTITY_INVALID) return archetype.table[entityIndex, componentIndex.index]
        return world.entityService.runOn(componentIndex.entity) { table[it, componentIndex.index] }
    }

    /**
     * 批量更新实体的关系和组件
     *
     * 处理添加/移除组件的操作，执行原型迁移并触发事件
     *
     * @param entity 目标实体
     * @param operates 操作集合
     * @param event 是否触发事件
     */
    fun updateRelations(entity: Entity, operates: SortSet<EntityRelationOperate>, event: Boolean) {
        if (operates.isEmpty()) return
        entityService.runOn(entity) { entityIndex ->
            val newArchetype = calculateNewArchetype(operates)
            val newEntityIndex = migrateEntityData(entity, entityIndex, newArchetype, operates)
            updateEntityRecords(entity, entityIndex, newArchetype, newEntityIndex, entityService)
            if (event) emitComponentModifyEvent(entity, this, newArchetype, operates)
        }
    }

    /**
     * 触发组件变更事件
     *
     * 根据组件变化类型触发 OnInserted、OnRemoved 或 OnUpdated 事件
     */
    private fun emitComponentModifyEvent(
        entity: Entity,
        oldArchetype: Archetype,
        newArchetype: Archetype,
        operates: SortSet<EntityRelationOperate>
    ) {
        var oldRelationIndex = 0
        var newRelationIndex = 0
        while (oldRelationIndex < oldArchetype.archetypeType.size || newRelationIndex < newArchetype.archetypeType.size) {
            val oldRelation = oldArchetype.archetypeType.getOrNull(oldRelationIndex)
            val newRelation = newArchetype.archetypeType.getOrNull(newRelationIndex)
            if (oldRelation == null) {
                if (newRelation != null) {
                    world.observeService.dispatch(entity, components.onInserted, null, newRelation)
                    newRelationIndex++
                }
                continue
            }
            if (newRelation == null) {
                world.observeService.dispatch(entity, components.onRemoved, null, oldRelation)
                oldRelationIndex++
                continue
            }
            if (oldRelation == newRelation) {
                if (operates.any { it.relation == oldRelation && it is BatchEntityEditor.AddEntityRelationOperateWithData }) {
                    world.observeService.dispatch(entity, components.onUpdated, null, newRelation)
                }
                oldRelationIndex++
                newRelationIndex++
            } else if (oldRelation > newRelation) {
                world.observeService.dispatch(entity, components.onInserted, null, newRelation)
                newRelationIndex++
            } else {
                world.observeService.dispatch(entity, components.onRemoved, null, oldRelation)
                oldRelationIndex++
            }
        }
    }

    /**
     * 计算新的原型
     *
     * 根据操作集合计算实体应该迁移到的新原型
     */
    private fun Archetype.calculateNewArchetype(operates: SortSet<EntityRelationOperate>): Archetype {
        return operates.fold(this) { acc, operate ->
            when (operate) {
                is BatchEntityEditor.RemoveEntityRelationOperate -> acc - operate.relation
                else -> acc + operate.relation
            }
        }
    }

    /**
     * 迁移实体数据到新原型
     *
     * 将实体的组件数据从旧原型迁移到新原型
     */
    private fun Archetype.migrateEntityData(
        entity: Entity,
        oldEntityIndex: Int,
        newArchetype: Archetype,
        operates: SortSet<EntityRelationOperate>
    ): Int {
        val holdsData =
            operates.asSequence().filterIsInstance<BatchEntityEditor.AddEntityRelationOperateWithData>().iterator()
        var nextData: BatchEntityEditor.AddEntityRelationOperateWithData? = null

        if (this.id == newArchetype.id) {
            if (holdsData.hasNext()) {
                table.holdsDataType.forEachIndexed { index, relation ->
                    if (nextData == null && holdsData.hasNext()) {
                        nextData = holdsData.next()
                    }
                    val newNextData = nextData ?: return@forEachIndexed
                    if (relation == newNextData.relation) {
                        table[oldEntityIndex, index] = newNextData.data
                        nextData = null
                    }
                }
            }
            return oldEntityIndex
        }
        
        val oldTable = table
        val oldHoldsDataType = oldTable.holdsDataType
        val oldSize = oldHoldsDataType.size
        return newArchetype.table.insert(entity) {
            if (nextData == null && holdsData.hasNext()) {
                nextData = holdsData.next()
            }
            val newNextData = nextData
            if (newNextData != null && newNextData.relation == this) {
                nextData = null
                newNextData.data
            } else {
                var currentIndex = 0
                while (currentIndex < oldSize && oldHoldsDataType[currentIndex] < this) {
                    currentIndex++
                }
                if (currentIndex < oldSize && oldHoldsDataType[currentIndex] == this) {
                    oldTable[oldEntityIndex, currentIndex]
                } else {
                    Any()
                }
            }
        }
    }

    /**
     * 更新实体记录
     *
     * 在原型迁移后更新实体的记录信息
     */
    private fun Archetype.updateEntityRecords(
        entity: Entity,
        oldEntityIndex: Int,
        newArchetype: Archetype,
        newEntityIndex: Int,
        entityService: EntityService
    ) {
        if (id == newArchetype.id) return
        // 更新实体记录
        val isNotLast = oldEntityIndex != table.size - 1
        table.removeAt(oldEntityIndex)
        val movedEntity = if (table.size > 1 && isNotLast) table[oldEntityIndex] else null
        entityService.updateEntityRecord(entity, newArchetype, newEntityIndex)
        if (movedEntity != null) {
            entityService.updateEntityRecord(movedEntity, this, oldEntityIndex)
        }
    }
}
