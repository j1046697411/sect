package cn.jzl.ecs.serialization.entity

import cn.jzl.ecs.component.Component
import cn.jzl.ecs.serialization.core.ComponentSerializers.Companion.fromCamelCaseToSnakeCase
import cn.jzl.ecs.serialization.core.ComponentSerializers.Companion.hasNamespace
import cn.jzl.ecs.serialization.core.OnMissingStrategy
import cn.jzl.ecs.serialization.core.SerializationContext
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

/**
 * 多态组件序列化器
 *
 * 用于序列化和反序列化组件列表
 */
class PolymorphicComponentSerializer(
    private val context: SerializationContext
) : KSerializer<List<Component>> {
    private val config = context.config
    private val polymorphicSerializer = PolymorphicSerializer(Component::class)
    private val stringMapSerializer = MapSerializer(String.serializer(), String.serializer())

    override val descriptor: SerialDescriptor = stringMapSerializer.descriptor

    override fun deserialize(decoder: Decoder): List<Component> {
        val components = mutableListOf<Component>()
        val componentMap = decoder.decodeSerializableValue(stringMapSerializer)

        componentMap.entries.forEach { (key, value) ->
            val componentSerializer = findSerializerFor(key)
            if (componentSerializer == null) {
                when (config.onMissingSerializer) {
                    OnMissingStrategy.ERROR -> error("Missing serializer for polymorphic key: $key")
                    OnMissingStrategy.WARN -> println("Warning: No serializer found for $key, ignoring")
                    OnMissingStrategy.IGNORE -> Unit
                }
                return@forEach
            }

            runCatching {
                val json = Json { ignoreUnknownKeys = config.skipMalformedComponents }
                json.decodeFromString(componentSerializer, value)
            }.onSuccess { components += it }
                .onFailure { exception ->
                    if (config.skipMalformedComponents) {
                        println("Warning: Malformed component $key, ignoring: ${exception.message}")
                    } else {
                        throw exception
                    }
                }
        }

        return components
    }

    override fun serialize(encoder: Encoder, value: List<Component>) {
        val componentMap = mutableMapOf<String, String>()

        value.forEach { component ->
            val kClass = component::class
            val serialName = context.serializers.getSerialNameFor(kClass)
                ?: error("No serial name found for ${kClass.simpleName}")

            val serializer = context.serializers.getSerializerFor(kClass)
                ?: error("No serializer found for ${kClass.simpleName}")

            @Suppress("UNCHECKED_CAST")
            val jsonValue = Json.encodeToString(serializer as KSerializer<Component>, component)
            componentMap[serialName] = jsonValue
        }

        encoder.encodeSerializableValue(stringMapSerializer, componentMap)
    }

    private fun findSerializerFor(key: String): KSerializer<out Component>? {
        val parsedKey = "${config.prefix}$key".fromCamelCaseToSnakeCase()

        return if (parsedKey.hasNamespace()) {
            context.serializers.getClassFor(parsedKey)?.let {
                context.serializers.getSerializerFor(it) as? KSerializer<out Component>
            }
        } else {
            config.namespaces.firstNotNullOfOrNull { namespace ->
                context.serializers.getClassFor("$namespace:$parsedKey")?.let {
                    context.serializers.getSerializerFor(it) as? KSerializer<out Component>
                }
            }
        }
    }
}
