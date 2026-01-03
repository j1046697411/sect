package cn.jzl.ecs.query

import cn.jzl.ecs.relation.Relation
import kotlin.reflect.KProperty
import kotlin.reflect.KType

class RelationAccessor<V>(
    type: KType,
    relation: Relation,
    optionalGroup: OptionalGroup,
) : AbstractCachedAccessor(type, relation, optionalGroup), ReadWriteAccessor<V> {

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: EntityQueryContext, property: KProperty<*>): V = with(thisRef) {
        val componentIndex = componentIndex
        if (isMarkedNullable && componentIndex == null) return@with null as V
        requireNotNull(componentIndex)
        val archetype = requireNotNull(archetype) {}
        world.relationService.getRelation(archetype, relation, entityIndex, componentIndex) as V
    }

    override fun setValue(
        thisRef: EntityQueryContext,
        property: KProperty<*>,
        value: V
    ): Unit = with(thisRef) {
        if (value != null) {
            entityEditor.addRelation(entity, relation, value)
        } else {
            entityEditor.addRelation(entity, relation)
        }
    }
}

