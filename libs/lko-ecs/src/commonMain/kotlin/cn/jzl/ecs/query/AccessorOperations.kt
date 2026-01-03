package cn.jzl.ecs.query

import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.component.components
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.relation.relation
import cn.jzl.ecs.relation.relations
import kotlin.reflect.typeOf

abstract class AccessorOperations {
    internal val accessors = mutableSetOf<Accessor>()
    private val cachedAccessors = mutableSetOf<CachedAccessor>()

    inline fun <reified K> EntityQueryContext.relation(
        target: Entity,
        optionalGroup: OptionalGroup = OptionalGroup.Ignore
    ): ReadWriteAccessor<K> = addAccessor {
        val type = typeOf<K>()
        val relation = relations.relation<K>(target)
        RelationAccessor(type, relation, optionalGroup)
    }

    inline fun <reified K, reified T> EntityQueryContext.relation(
        optionalGroup: OptionalGroup = OptionalGroup.Ignore
    ): ReadWriteAccessor<K> = relation<K>(components.id<T>(), optionalGroup)

    inline fun <reified C> EntityQueryContext.component(
        optionalGroup: OptionalGroup = OptionalGroup.Ignore
    ): ReadWriteAccessor<C> = relation<C>(components.componentOf, optionalGroup)

    fun EntityQueryContext.relationUp(kind: ComponentId): ReadWriteAccessor<Entity> = addAccessor {
        RelationUpAccessor(kind)
    }

    inline fun <reified K> EntityQueryContext.relationUp(): ReadWriteAccessor<Entity> = relationUp(components.id<K>())

    @PublishedApi
    internal fun <A : Accessor> addAccessor(create: () -> A): A {
        val accessor = create()
        accessors.add(accessor)
        if (accessor is CachedAccessor) cachedAccessors.add(accessor)
        return accessor
    }
}