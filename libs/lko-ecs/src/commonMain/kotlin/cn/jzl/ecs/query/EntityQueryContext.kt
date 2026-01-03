package cn.jzl.ecs.query

import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.archetype.Archetype
import cn.jzl.ecs.entity.BatchEntityEditor
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.family.Family
import cn.jzl.ecs.family.FamilyBuilder
import cn.jzl.ecs.family.or
import cn.jzl.ecs.relation.EntityType

open class EntityQueryContext(override val world: World) : AccessorOperations(), WorldOwner {
    private var archetype: Archetype = world.archetypeService.rootArchetype
    val entity: Entity get() = if (entityIndex == -1) Entity.ENTITY_INVALID else archetype.table[entityIndex]
    val entityType: EntityType get() = archetype.entityType

    @PublishedApi
    internal var entityIndex: Int = -1

    @PublishedApi
    internal val entityEditor: BatchEntityEditor = BatchEntityEditor(world, Entity.ENTITY_INVALID)

    internal fun build(): Family = world.familyService.family {
        val families = mutableListOf<FamilyMatching>()
        accessors.asSequence().filterIsInstance<FamilyMatching>().forEach {
            if (!it.isMarkedNullable) {
                it.run { matching() }
                return@forEach
            }
            if (it.optionalGroup == OptionalGroup.One) families.add(it)
        }
        if (families.isNotEmpty()) {
            or { families.forEach { it.run { matching() } } }
        }
        configure()
    }

    protected open fun FamilyBuilder.configure(): Unit = Unit

    internal fun updateCache(archetype: Archetype) {
        this.archetype = archetype
        accessors.asSequence().filterIsInstance<CachedAccessor>().forEach { it.updateCache(archetype) }
        entityIndex = -1
    }

    inline fun apply(entityIndex: Int, block: () -> Unit) {
        this.entityIndex = entityIndex
        try {
            entityEditor.entity = entity
            block()
        } finally {
            entityEditor.apply(world, event = true)
        }
    }
}