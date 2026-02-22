package cn.jzl.ecs.serialization.performance

import cn.jzl.ecs.component.Component
import cn.jzl.ecs.entity
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.relation.kind
import cn.jzl.ecs.serialization.core.SerializationContext
import cn.jzl.ecs.serialization.entity.Persistable
import cn.jzl.ecs.serialization.entity.setPersisting
import cn.jzl.ecs.serialization.internal.WorldServices
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.PolymorphicSerializer

/**
 * 增量序列化器
 *
 * 只序列化发生变化的组件，提高性能
 */
class IncrementalSerializer(
    private val context: SerializationContext
) {
    private val lastSerializedState = mutableMapOf<Entity, EntitySnapshot>()
    private val lastDeserializedState = mutableMapOf<String, EntitySnapshot>()
    private val services = WorldServices(context.world)

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
            return lastSnapshot.entity ?: error("Entity not found in snapshot")
        }

        val entity = restoreEntity(snapshot)
        lastDeserializedState[entityId] = snapshot.copy(entity = entity)
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

        services.entityService.runOn(entity) { entityIndex ->
            archetypeType.forEach { relation ->
                val component = services.relationService.getRelation(entity, relation)
                if (component != null && component is Component) {
                    val persistableComponentId = services.components.id<Persistable>()
                    val hasPersistable = archetypeType.any { it.kind == persistableComponentId }
                    val hash = if (hasPersistable) component.hashCode() else 0
                    components[relation.kind.data.toString()] = ComponentSnapshot(
                        component = component,
                        hash = hash
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
        return Json.encodeToString(
            ListSerializer(PolymorphicSerializer(Component::class)),
            components
        ).encodeToByteArray()
    }

    private fun deserializeSnapshot(data: ByteArray): EntitySnapshot {
        val jsonString = data.decodeToString()
        val components: List<Component> = Json.decodeFromString(
            ListSerializer(PolymorphicSerializer(Component::class)),
            jsonString
        )

        val componentMap = mutableMapOf<String, ComponentSnapshot>()
        components.forEach { component ->
            componentMap[component::class.simpleName ?: "unknown"] = ComponentSnapshot(
                component = component,
                hash = component.hashCode()
            )
        }

        return EntitySnapshot(null, componentMap)
    }

    private fun restoreEntity(snapshot: EntitySnapshot): Entity {
        return context.world.entity { e ->
            snapshot.components.values.forEach { componentSnapshot ->
                e.setPersisting(this@IncrementalSerializer.context, componentSnapshot.component)
            }
        }
    }

    data class EntitySnapshot(
        val entity: Entity?,
        val components: Map<String, ComponentSnapshot>
    )

    data class ComponentSnapshot(
        val component: Component,
        val hash: Int
    )
}
