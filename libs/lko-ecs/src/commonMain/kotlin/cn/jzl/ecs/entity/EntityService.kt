package cn.jzl.ecs.entity

import cn.jzl.core.bits.fromLowHigh
import cn.jzl.core.bits.high
import cn.jzl.core.bits.low
import cn.jzl.core.list.LongFastList
import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.archetype.Archetype

class EntityService(override val world: World) : WorldOwner {

    private val entityRecords = BucketedLongArray()
    private val entityEditorPool = BatchEntityEditorPool(world)

    fun isActive(entity: Entity): Boolean = entity in world.entityStore

    @PublishedApi
    internal fun updateEntityRecord(entity: Entity, archetype: Archetype, entityIndex: Int) {
        entityRecords[entity.id] = Long.fromLowHigh(archetype.id, entityIndex)
    }

    fun <R> runOn(entity: Entity, block: Archetype.(Int) -> R): R {
        require(entity in world.entityStore) { "Entity $entity is not in world" }
        val record = entityRecords[entity.id]
        val archetype = world.archetypeService[record.low]
        return archetype.block(record.high)
    }

    fun create(event: Boolean = true, configuration: EntityCreateContext.(Entity) -> Unit = {}): Entity {
        return postCreate(world.entityStore.create(), event, configuration)
    }

    fun create(entityId: Int, event: Boolean, configuration: EntityCreateContext.(Entity) -> Unit = {}): Entity {
        return postCreate(world.entityStore.create(entityId), event, configuration)
    }

    fun configure(entity: Entity, event: Boolean, configuration: EntityUpdateContext.(Entity) -> Unit = {}) {
        val editor = entityEditorPool.obtain(entity)
        val context = EntityUpdateContext(editor)
        context.configuration(entity)
        editor.apply(world, event)
        entityEditorPool.release(editor)
    }

    private fun postCreate(
        entity: Entity,
        event: Boolean,
        configuration: EntityCreateContext.(Entity) -> Unit
    ): Entity {
        val rootArchetype = world.archetypeService.rootArchetype
        val entityIndex = rootArchetype.table.insert(entity) {}
        updateEntityRecord(entity, rootArchetype, entityIndex)
        val editor = entityEditorPool.obtain(entity)
        val context = EntityCreateContext(editor)
        context.configuration(entity)
        editor.apply(world, event)
        entityEditorPool.release(editor)
        return entity
    }
}

