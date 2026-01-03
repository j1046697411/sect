package cn.jzl.ecs.archetype

import androidx.collection.mutableObjectListOf
import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.relation.EntityType

class ArchetypeService(override val world: World) : ArchetypeProvider, WorldOwner {
    private val typeToArchetypeMap = mutableMapOf<EntityType, Archetype>()
    private val archetypes = mutableObjectListOf<Archetype>()
    override val rootArchetype: Archetype = getArchetype(EntityType.Companion.empty)

    override fun getArchetype(entityType: EntityType): Archetype {
        return typeToArchetypeMap.getOrPut(entityType) { createArchetype(entityType) }
    }

    private fun createArchetype(entityType: EntityType): Archetype {
        val archetype = Archetype(world, archetypes.size, entityType, this)
        archetypes.add(archetype)
        world.familyService.registerArchetype(archetype)
        return archetype
    }

    operator fun get(id: Int): Archetype {
        require(id in archetypes.indices) { "Archetype id $id is out of range" }
        return archetypes[id]
    }
}