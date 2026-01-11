@file:Suppress("OPT_IN_USAGE")

package cn.jzl.ecs.serialization.format

import cn.jzl.ecs.serialization.core.ComponentSerializers
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule

class FormatsBuilder {
    private val formatFactories = mutableMapOf<String, (SerializersModule) -> Format>()
    fun register(ext: String, formatFactory: (SerializersModule) -> Format) {
        formatFactories[ext] = formatFactory
    }

    fun build(componentSerializers: ComponentSerializers): Formats {
        return FormatsImpl(
            binaryFormat = Cbor {
                serializersModule = componentSerializers.module
                encodeDefaults = false
                ignoreUnknownKeys = true
            },
            formats = formatFactories.mapValues { (_, factory) -> factory(componentSerializers.module) }
        )
    }
}

interface Formats {
    operator fun get(ext: String): Format?

    @Suppress("OPT_IN_USAGE")
    val binaryFormat: Cbor
}

class FormatsImpl(
    override val binaryFormat: Cbor,
    private val formats: Map<String, Format>
) : Formats {
    override operator fun get(ext: String): Format? = formats[ext]
}