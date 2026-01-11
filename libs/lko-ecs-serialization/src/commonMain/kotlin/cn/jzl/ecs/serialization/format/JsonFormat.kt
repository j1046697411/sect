package cn.jzl.ecs.serialization.format

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

class JsonFormat(
    private val prettyPrint: Boolean = false,
    private val ignoreUnknownKeys: Boolean = true
) : Format {
    override val ext: String = "json"
    override val mimeType: String = "application/json"

    private val json = Json {
        this.prettyPrint = this@JsonFormat.prettyPrint
        this.ignoreUnknownKeys = this@JsonFormat.ignoreUnknownKeys
        this.encodeDefaults = true
    }

    override fun <T> encode(
        serializer: SerializationStrategy<T>,
        value: T,
        overrideSerializersModule: SerializersModule?
    ): ByteArray {
        val module = overrideSerializersModule ?: json.serializersModule
        val jsonString = json.encodeToString(serializer, value)
        return jsonString.encodeToByteArray()
    }

    override fun <T> decode(
        deserializer: DeserializationStrategy<T>,
        data: ByteArray,
        overrideSerializersModule: SerializersModule?
    ): T {
        val module = overrideSerializersModule ?: json.serializersModule
        val jsonString = data.decodeToString()
        return json.decodeFromString(deserializer, jsonString)
    }

    companion object {
        val DEFAULT = JsonFormat()
        val PRETTY = JsonFormat(prettyPrint = true)
    }
}