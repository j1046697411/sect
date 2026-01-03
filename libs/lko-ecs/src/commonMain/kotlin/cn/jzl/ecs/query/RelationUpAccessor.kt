package cn.jzl.ecs.query

import cn.jzl.ecs.archetype.Archetype
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.family.FamilyBuilder
import cn.jzl.ecs.family.kind
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.relation.kind
import cn.jzl.ecs.relation.relation
import cn.jzl.ecs.relation.relations
import cn.jzl.ecs.relation.target
import kotlin.reflect.KProperty

class RelationUpAccessor(private val kind: ComponentId) : CachedAccessor, ReadWriteAccessor<Entity> {
    override val isMarkedNullable: Boolean get() = false
    override val optionalGroup: OptionalGroup get() = OptionalGroup.Ignore
    private var relation: Relation? = null
    override fun FamilyBuilder.matching(): Unit = kind(kind)

    override fun updateCache(archetype: Archetype) {
        relation = archetype.entityType.firstOrNull { it.kind == kind }
    }

    override fun getValue(
        thisRef: EntityQueryContext,
        property: KProperty<*>
    ): Entity {
        val relation = requireNotNull(relation) { "RelationUpAccessor<$kind> is not cached" }
        return relation.target
    }

    override fun setValue(
        thisRef: EntityQueryContext,
        property: KProperty<*>,
        value: Entity
    ): Unit = with(thisRef) {
        val data = relation?.let { world.relationService.getRelation(entity, it) }
        if (relation?.target != value) {
            if (data != null) {
                entityEditor.addRelation(entity, relations.relation(kind, value), data)
            } else {
                entityEditor.addRelation(entity, relations.relation(kind, value))
            }
        }
    }
}