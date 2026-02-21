package cn.jzl.ecs.serialization.performance

import cn.jzl.ecs.archetype.Archetype
import cn.jzl.ecs.component.Component
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.relation.kind
import cn.jzl.ecs.serialization.core.SerializationContext
import cn.jzl.ecs.serialization.entity.EntitySerializer
import cn.jzl.ecs.serialization.internal.WorldServices
import kotlinx.serialization.json.Json

/**
 * 原型感知序列化器
 *
 * 根据实体的原型类型进行分组序列化，提高性能
 */
class ArchetypeAwareSerializer(
    private val context: SerializationContext
) {
    private val archetypeCache = mutableMapOf<Int, ArchetypeSerializer>()
    private val entityCache = mutableMapOf<Entity, List<Component>>()
    private val services = WorldServices(context.world)

    fun serializeEntities(entities: List<Entity>): List<ByteArray> {
        val archetypes = entities.groupBy { entity ->
            services.entityService.runOn(entity) { entityIndex ->
                id
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
            Json.decodeFromString(serializer, data.decodeToString())
        }
    }

    private fun getArchetypeSerializer(archetypeId: Int): ArchetypeSerializer {
        return archetypeCache.getOrPut(archetypeId) {
            val archetype = services.archetypeService[archetypeId]
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

/**
 * 原型序列化器
 *
 * 针对特定原型优化的序列化器
 */
class ArchetypeSerializer(
    private val context: SerializationContext,
    private val archetype: Archetype
) {
    private val services = WorldServices(context.world)

    fun serialize(entity: Entity): ByteArray {
        val components = mutableListOf<Component>()

        services.entityService.runOn(entity) { entityIndex ->
            archetype.archetypeType.forEach { relation ->
                val component = services.relationService.getRelation(entity, relation)
                if (component != null && component is Component) {
                    components.add(component)
                }
            }
        }

        return Json.encodeToString(
            Json.serializersModule.serializer(),
            components
        ).encodeToByteArray()
    }
}
