package cn.jzl.ecs.serialization.entity

import cn.jzl.ecs.component.Component
import cn.jzl.ecs.entity
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.serialization.core.SerializationContext
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias SerializableEntity = @Contextual Entity

class EntitySerializer(
    private val context: SerializationContext
) : KSerializer<Entity> {
    private val componentSerializer = PolymorphicComponentSerializer(context)

    override val descriptor = SerialDescriptor("lko-ecs:entity", componentSerializer.descriptor)

    override fun deserialize(decoder: Decoder): Entity {
        val world = context.world
        val components = componentSerializer.deserialize(decoder)

        return world.entity {
            components.forEach { component ->
                set(component)
                setPersisting(component)
            }
        }
    }

    override fun serialize(encoder: Encoder, value: Entity) {
        val persistingComponents = getAllPersistingComponents(value)
        encoder.encodeSerializableValue(componentSerializer, persistingComponents)
    }

    private fun getAllPersistingComponents(entity: Entity): List<Component> {
        val persistingComponents = mutableListOf<Component>()
        val persistableComponentId = context.world.components.id<Persistable>()

        context.world.entityService.runOn(entity) { entityIndex ->
            val archetype = context.world.archetypeService.getArchetype(entityIndex)
            archetype.archetypeType.forEach { relation ->
                if (relation.kind == persistableComponentId) {
                    val component = context.world.relationService.getRelation(entity, relation)
                    if (component != null) {
                        persistingComponents.add(component as Component)
                    }
                }
            }
        }

        return persistingComponents
    }
}