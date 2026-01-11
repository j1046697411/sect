package cn.jzl.ecs.serialization.addon

import cn.jzl.ecs.World
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class WorldProviderSerializer(val world: World) : KSerializer<World> {

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = SerialDescriptor("world", ContextualSerializer(Any::class).descriptor)

    override fun deserialize(decoder: Decoder): World {
        return world
    }

    override fun serialize(encoder: Encoder, value: World) {
    }
}