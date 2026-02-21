package cn.jzl.ecs.serialization.entity

import cn.jzl.ecs.component.Component
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.entity.EntityCreateContext
import cn.jzl.ecs.relation.kind
import cn.jzl.ecs.serialization.core.SerializationContext
import cn.jzl.ecs.serialization.internal.WorldServices
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

        return world.entity { entity ->
            components.forEach { component ->
                entity.addComponent(component)
                entity.setPersisting(this@EntitySerializer.context, component)
            }
        }
    }

    override fun serialize(encoder: Encoder, value: Entity) {
        val persistingComponents = getAllPersistingComponents(value)
        encoder.encodeSerializableValue(componentSerializer, persistingComponents)
    }

    private fun getAllPersistingComponents(entity: Entity): List<Component> {
        val persistingComponents = mutableListOf<Component>()
        val services = WorldServices(context.world)
        val persistableComponentId = services.components.id<Persistable>()

        services.entityService.runOn(entity) { entityIndex ->
            archetypeType.forEach { relation ->
                if (relation.kind == persistableComponentId) {
                    val component = services.relationService.getRelation(entity, relation)
                    if (component != null) {
                        persistingComponents.add(component as Component)
                    }
                }
            }
        }

        return persistingComponents
    }
}

/**
 * 在实体创建上下文中设置持久化组件（简化版本，用于反序列化）
 */
context(context: EntityCreateContext)
fun Entity.setPersisting(serializationContext: SerializationContext, component: Component): Component {
    addComponent(component)
    Persistable().updateHash(component)
    return component
}
