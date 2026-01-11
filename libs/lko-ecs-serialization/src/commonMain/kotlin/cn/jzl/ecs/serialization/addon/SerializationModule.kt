package cn.jzl.ecs.serialization.addon

import cn.jzl.ecs.World
import cn.jzl.ecs.serialization.core.ComponentSerializers
import cn.jzl.ecs.serialization.core.SerializationConfig
import cn.jzl.ecs.serialization.core.SerializationContext
import cn.jzl.ecs.serialization.core.VersionManager
import cn.jzl.ecs.serialization.format.Format
import cn.jzl.ecs.serialization.format.Formats
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

data class SerializationModule(
    val world: World,
    val serializers: ComponentSerializers,
    val formats: Formats,
    val config: SerializationConfig,
    val versionManager: VersionManager
) {
    val context: SerializationContext
        get() = SerializationContext(world, serializers, config)

    fun <T> serialize(serializer: SerializationStrategy<T>, value: T, format: Format): ByteArray {
        return format.encode(serializer, value, serializers.module)
    }

    fun <T> deserialize(deserializer: DeserializationStrategy<T>, data: ByteArray, format: Format): T {
        return format.decode(deserializer, data, serializers.module)
    }
}