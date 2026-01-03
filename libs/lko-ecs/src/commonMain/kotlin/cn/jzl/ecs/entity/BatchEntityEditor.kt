package cn.jzl.ecs.entity

import cn.jzl.core.list.SortSet
import cn.jzl.ecs.World
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.relation.kind
import kotlin.jvm.JvmInline

data class BatchEntityEditor(override val world: World, @PublishedApi internal var entity: Entity) : EntityEditor {

    private val operates = SortSet<EntityRelationOperate> { a, b -> a.relation.compareTo(b.relation) }

    private val singleRelationOperates =
        SortSet<EntityRelationOperate> { a, b -> a.relation.kind.data.compareTo(b.relation.kind.data) }

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

    override fun removeRelation(entity: Entity, relation: Relation) {
        check(entity == this.entity) { "entity must be $entity" }
        val removeEntityRelationOperate = RemoveEntityRelationOperate(relation)
        if (world.componentService.isSingleRelation(relation)) {
            singleRelationOperates.add(removeEntityRelationOperate)
        } else {
            operates.add(RemoveEntityRelationOperate(relation))
        }
    }

    fun apply(world: World, event: Boolean,) {
        applySingleRelationOperates()
        if (operates.isEmpty()) return
        world.relationService.updateRelations(entity, operates, event)
        operates.clear()
    }

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

    sealed interface EntityRelationOperate {
        val relation: Relation
    }

    data class AddEntityRelationOperateWithData(override val relation: Relation, val data: Any) : EntityRelationOperate

    @JvmInline
    value class AddEntityRelationOperate(override val relation: Relation) : EntityRelationOperate

    @JvmInline
    value class RemoveEntityRelationOperate(override val relation: Relation) : EntityRelationOperate
}

