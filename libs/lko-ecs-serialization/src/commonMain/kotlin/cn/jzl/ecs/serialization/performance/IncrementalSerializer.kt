package cn.jzl.ecs.serialization.performance

import cn.jzl.ecs.World
import cn.jzl.ecs.component.Component
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.serialization.core.SerializationContext
import cn.jzl.ecs.serialization.entity.Persistable
import kotlinx.serialization.KSerializer

class IncrementalSerializer(
    private val context: SerializationContext
) {
    private val lastSerializedState = mutableMapOf<Entity, EntitySnapshot>()
    private val lastDeserializedState = mutableMapOf<String, EntitySnapshot>()

    fun serializeIncremental(entity: Entity): ByteArray? {
        val currentSnapshot = createSnapshot(entity)
        val lastSnapshot = lastSerializedState[entity]

        if (lastSnapshot == null || hasChanged(lastSnapshot, currentSnapshot)) {
            lastSerializedState[entity] = currentSnapshot
            return serializeEntity(entity, currentSnapshot)
        }

        return null
    }

    fun deserializeIncremental(entityId: String, data: ByteArray): Entity {
        val snapshot = deserializeSnapshot(data)
        val lastSnapshot = lastDeserializedState[entityId]

        if (lastSnapshot != null && !hasChanged(lastSnapshot, snapshot)) {
            return lastSnapshot.entity
        }

        val entity = restoreEntity(snapshot)
        lastDeserializedState[entityId] = snapshot
        return entity
    }

    fun serializeBatch(entities: List<Entity>): Map<Entity, ByteArray> {
        val results = mutableMapOf<Entity, ByteArray>()

        entities.forEach { entity ->
            val data = serializeIncremental(entity)
            if (data != null) {
                results[entity] = data
            }
        }

        return results
    }

    fun clearCache() {
        lastSerializedState.clear()
        lastDeserializedState.clear()
    }

    fun getCacheSize(): Int {
        return lastSerializedState.size + lastDeserializedState.size
    }

    private fun createSnapshot(entity: Entity): EntitySnapshot {
        val components = mutableMapOf<String, ComponentSnapshot>()

        context.world.entityService.runOn(entity) { entityIndex ->
            val archetype = context.world.archetypeService.getArchetype(entityIndex)
            archetype.archetypeType.forEach { relation ->
                val component = context.world.relationService.getRelation(entity, relation)
                if (component != null && component is Component) {
                    val persistable = context.world.relationService.getRelation(entity, relation) as? Persistable
                    components[relation.kind.data.toString()] = ComponentSnapshot(
                        component = component,
                        hash = persistable?.hash ?: 0
                    )
                }
            }
        }

        return EntitySnapshot(entity, components)
    }

    private fun hasChanged(oldSnapshot: EntitySnapshot, newSnapshot: EntitySnapshot): Boolean {
        if (oldSnapshot.components.size != newSnapshot.components.size) return true

        newSnapshot.components.forEach { (key, newComponent) ->
            val oldComponent = oldSnapshot.components[key]
            if (oldComponent == null || oldComponent.hash != newComponent.hash) return true
        }

        return false
    }

    private fun serializeEntity(entity: Entity, snapshot: EntitySnapshot): ByteArray {
        val components = snapshot.components.values.map { it.component }
        return kotlinx.serialization.json.Json.encodeToString(
            kotlinx.serialization.serializer<List<Component>>(),
            components
        ).encodeToByteArray()
    }

    private fun deserializeSnapshot(data: ByteArray): EntitySnapshot {
        val jsonString = data.decodeToString()
        val components = kotlinx.serialization.json.Json.decodeFromString<List<ComponentSnapshot>>(
            kotlinx.serialization.serializer(),
            jsonString
        )

        val componentMap = mutableMapOf<String, ComponentSnapshot>()
        components.forEach { componentMap[it.component::class.simpleName ?: "unknown"] = it }

        return EntitySnapshot(null, componentMap)
    }

    private fun restoreEntity(snapshot: EntitySnapshot): Entity {
        val entity = context.world.entity {
            snapshot.components.values.forEach { componentSnapshot ->
                set(componentSnapshot.component)
                setPersisting(componentSnapshot.component)
            }
        }

        return entity
    }

    data class EntitySnapshot(
        val entity: Entity?,
        val components: Map<String, ComponentSnapshot>
    )

    @kotlinx.serialization.Serializable
    data class ComponentSnapshot(
        val component: Component,
        val hash: Int
    )
}