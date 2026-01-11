package cn.jzl.ecs.serialization.performance

import cn.jzl.ecs.World
import cn.jzl.ecs.archetype.Archetype
import cn.jzl.ecs.component.Component
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.serialization.core.SerializationContext
import kotlinx.serialization.KSerializer

class ArchetypeAwareSerializer(
    private val context: SerializationContext
) {
    private val archetypeCache = mutableMapOf<Int, ArchetypeSerializer>()
    private val entityCache = mutableMapOf<Entity, List<Component>>()

    fun serializeEntities(entities: List<Entity>): List<ByteArray> {
        val archetypes = entities.groupBy { entity ->
            context.world.entityService.runOn(entity) { entityIndex ->
                context.world.archetypeService.getArchetype(entityIndex).id
            }
        }

        return archetypes.flatMap { (archetypeId, archetypeEntities) ->
            val serializer = getArchetypeSerializer(archetypeId)
            archetypeEntities.map { entity ->
                serializer.serialize(entity)
            }
        }
    }

    fun deserializeEntities(dataList: List<ByteArray>): List<Entity> {
        return dataList.map { data ->
            val serializer = EntitySerializer(context)
            kotlinx.serialization.json.Json.decodeFromString(serializer, data.decodeToString())
        }
    }

    private fun getArchetypeSerializer(archetypeId: Int): ArchetypeSerializer {
        return archetypeCache.getOrPut(archetypeId) {
            val archetype = context.world.archetypeService.getArchetypeById(archetypeId)
            ArchetypeSerializer(context, archetype)
        }
    }

    fun clearCache() {
        archetypeCache.clear()
        entityCache.clear()
    }

    fun getCacheStats(): CacheStats {
        return CacheStats(
            archetypeCacheSize = archetypeCache.size,
            entityCacheSize = entityCache.size
        )
    }

    data class CacheStats(
        val archetypeCacheSize: Int,
        val entityCacheSize: Int
    )
}

class ArchetypeSerializer(
    private val context: SerializationContext,
    private val archetype: Archetype
) {
    private val componentSerializers = mutableMapOf<Int, KSerializer<Component>>()

    init {
        archetype.archetypeType.forEach { relation ->
            val serializer = context.serializers.getSerializerFor(relation.kind.data)
            if (serializer != null) {
                componentSerializers[relation.kind.data] = serializer
            }
        }
    }

    fun serialize(entity: Entity): ByteArray {
        val components = mutableListOf<Component>()

        context.world.entityService.runOn(entity) { entityIndex ->
            archetype.archetypeType.forEach { relation ->
                val component = context.world.relationService.getRelation(entity, relation)
                if (component != null && component is Component) {
                    components.add(component)
                }
            }
        }

        return kotlinx.serialization.json.Json.encodeToString(
            kotlinx.serialization.serializer<List<Component>>(),
            components
        ).encodeToByteArray()
    }
}