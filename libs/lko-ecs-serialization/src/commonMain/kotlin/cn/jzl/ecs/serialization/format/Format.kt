package cn.jzl.ecs.serialization.format

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule

interface Format {
    val ext: String
    val mimeType: String

    fun <T> encode(
        serializer: SerializationStrategy<T>,
        value: T,
        overrideSerializersModule: SerializersModule? = null
    ): ByteArray

    fun <T> decode(
        deserializer: DeserializationStrategy<T>,
        data: ByteArray,
        overrideSerializersModule: SerializersModule? = null
    ): T

    fun <T> encodeToString(
        serializer: SerializationStrategy<T>,
        value: T,
        overrideSerializersModule: SerializersModule? = null
    ): String {
        return encode(serializer, value, overrideSerializersModule).decodeToString()
    }

    fun <T> decodeFromString(
        deserializer: DeserializationStrategy<T>,
        string: String,
        overrideSerializersModule: SerializersModule? = null
    ): T {
        return decode(deserializer, string.encodeToByteArray(), overrideSerializersModule)
    }
}