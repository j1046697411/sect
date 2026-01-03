package cn.jzl.ecs.query

import cn.jzl.ecs.archetype.Archetype
import cn.jzl.ecs.archetype.ComponentIndex
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.family.FamilyBuilder
import cn.jzl.ecs.family.relation
import cn.jzl.ecs.relation.Relation
import kotlin.reflect.KType

abstract class AbstractCachedAccessor(
    private val type: KType,
    val relation: Relation,
    override val optionalGroup: OptionalGroup,
) : CachedAccessor {
    override val isMarkedNullable: Boolean get() = type.isMarkedNullable
    protected var componentIndex: ComponentIndex? = null
    protected var archetype: Archetype? = null
    override fun FamilyBuilder.matching(): Unit = relation(relation)
    override fun updateCache(archetype: Archetype) {
        this.archetype = archetype
        this.componentIndex = archetype.getComponentIndex(Entity.Companion.ENTITY_INVALID, relation)
    }
}