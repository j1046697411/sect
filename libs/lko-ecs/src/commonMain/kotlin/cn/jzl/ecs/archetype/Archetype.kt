package cn.jzl.ecs.archetype

import androidx.collection.LongSparseArray
import androidx.collection.getOrElse
import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.component.components
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.getOrPut
import cn.jzl.ecs.relation.EntityType
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.relation.kind
import cn.jzl.ecs.relation.target

data class Archetype(
    override val world: World,
    val id: Int,
    val archetypeType: EntityType,
    val archetypeProvider: ArchetypeProvider
) : WorldOwner {

    private val componentAddEdges = LongSparseArray<Archetype>()
    private val componentRemoveEdges = LongSparseArray<Archetype>()

    @PublishedApi
    internal val table = Table(archetypeType.holdsData(), world.componentService)

    val size: Int get() = table.entities.size

    val prefab: Entity? by lazy { archetypeType.firstOrNull { it.kind == components.instanceOf }?.target }

    val entityType: EntityType by lazy {
        val prefab = this.prefab ?: return@lazy archetypeType
        val entityType = mutableSetOf<Relation>()
        entityType.addAll(archetypeType)
        world.entityService.runOn(prefab) {
            entityType.addAll(this@runOn.entityType)
        }
        EntityType(entityType.asSequence())
    }

    fun getComponentIndex(entity: Entity, relation: Relation): ComponentIndex? {
        val entityType = if (world.componentService.isShadedComponent(relation)) archetypeType else table.holdsDataType
        val componentIndex = entityType.indexOf(relation)
        if (componentIndex != -1) return ComponentIndex(entity, componentIndex)
        val prefab = this.prefab ?: return null
        return world.entityService.runOn(prefab) {
            getComponentIndex(prefab, relation)
        }
    }

    fun EntityType.holdsData(): EntityType {
        val componentService = world.componentService
        return EntityType(filter { componentService.holdsData(it) && !componentService.isShadedComponent(it) })
    }

    operator fun plus(relation: Relation): Archetype {
        return componentAddEdges.getOrPut(relation.data) {
            archetypeProvider.getArchetype(archetypeType + relation).also {
                it.componentRemoveEdges.put(relation.data, it)
            }
        }
    }

    operator fun minus(relation: Relation): Archetype {
        return componentRemoveEdges.getOrPut(relation.data) {
            archetypeProvider.getArchetype(archetypeType - relation).also {
                it.componentAddEdges.put(relation.data, it)
            }
        }
    }
}

