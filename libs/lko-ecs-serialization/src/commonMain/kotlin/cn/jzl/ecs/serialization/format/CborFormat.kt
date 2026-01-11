package cn.jzl.ecs.serialization.format

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
sealed class CborFormat(private val ignoreUnknownKeys: Boolean = true) : Format {
    override val ext: String = "cbor"
    override val mimeType: String = "application/cbor"

    private val cbor = Cbor {
        this.ignoreUnknownKeys = this@CborFormat.ignoreUnknownKeys
        this.encodeDefaults = true
    }

    override fun <T> encode(
        serializer: SerializationStrategy<T>,
        value: T,
        overrideSerializersModule: SerializersModule?
    ): ByteArray {
        val module = overrideSerializersModule ?: cbor.serializersModule
        return cbor.encodeToByteArray(serializer, value)
    }

    override fun <T> decode(
        deserializer: DeserializationStrategy<T>,
        data: ByteArray,
        overrideSerializersModule: SerializersModule?
    ): T {
        val module = overrideSerializersModule ?: cbor.serializersModule
        return cbor.decodeFromByteArray(deserializer, data)
    }

    companion object : CborFormat()
}