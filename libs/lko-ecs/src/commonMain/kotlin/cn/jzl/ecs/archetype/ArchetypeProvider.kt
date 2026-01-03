package cn.jzl.ecs.archetype

import cn.jzl.ecs.relation.EntityType

interface ArchetypeProvider {

    val rootArchetype: Archetype

    fun getArchetype(entityType: EntityType): Archetype
}

