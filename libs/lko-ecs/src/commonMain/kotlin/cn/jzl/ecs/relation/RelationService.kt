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

class RelationService(override val world: World) : WorldOwner {
    private val entityService by world.instance<EntityService>()

    fun hasRelation(entity: Entity, relation: Relation): Boolean = entityService.runOn(entity) {
        entityType.indexOf(relation) != -1
    }

    fun getRelation(entity: Entity, relation: Relation): Any? = entityService.runOn(entity) {
        val componentIndex = getComponentIndex(entity, relation) ?: return@runOn null
        if (entity == componentIndex.entity) return@runOn table[it, componentIndex.index]
        entityService.runOn(componentIndex.entity) { entityIndex -> table[entityIndex, componentIndex.index] }
    }

    fun getComponentIndex(entity: Entity, relation: Relation): ComponentIndex? = entityService.runOn(entity) {
        getComponentIndex(entity, relation)
    }

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

    fun updateRelations(entity: Entity, operates: SortSet<EntityRelationOperate>, event: Boolean) {
        if (operates.isEmpty()) return
        entityService.runOn(entity) { entityIndex ->
            val newArchetype = calculateNewArchetype(operates)
            val newEntityIndex = migrateEntityData(entity, entityIndex, newArchetype, operates)
            updateEntityRecords(entity, entityIndex, newArchetype, newEntityIndex, entityService)
            if (event) emitComponentModifyEvent(entity, this, newArchetype, operates)
        }
    }


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

    private fun Archetype.calculateNewArchetype(operates: SortSet<EntityRelationOperate>): Archetype {
        return operates.fold(this) { acc, operate ->
            when (operate) {
                is BatchEntityEditor.RemoveEntityRelationOperate -> acc - operate.relation
                else -> acc + operate.relation
            }
        }
    }

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