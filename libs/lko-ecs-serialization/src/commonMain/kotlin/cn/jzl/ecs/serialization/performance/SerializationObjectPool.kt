package cn.jzl.ecs.serialization.performance

import cn.jzl.ecs.component.Component
import cn.jzl.ecs.serialization.core.SerializationContext
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.KClass

/**
 * 序列化对象池
 *
 * 用于复用序列化相关的对象，减少内存分配
 */
class SerializationObjectPool(
    private val maxPoolSize: Int = 1000,
    private val context: SerializationContext
) {
    private val serializerPools = mutableMapOf<KClass<*>, MutableList<Any>>()
    private val jsonPools = mutableMapOf<Boolean, MutableList<Json>>()
    private val bufferPools = mutableMapOf<Int, MutableList<ByteArray>>()

    @OptIn(InternalSerializationApi::class)
    fun <T : Component> acquireSerializer(kClass: KClass<T>): KSerializer<T> {
        val pool = serializerPools.getOrPut(kClass) { mutableListOf() }
        @Suppress("UNCHECKED_CAST")
        val serializer = if (pool.isNotEmpty()) {
            pool.removeAt(pool.size - 1) as KSerializer<T>
        } else {
            kClass.serializerOrNull() ?: error("No serializer found for $kClass")
        }

        return serializer
    }

    fun releaseSerializer(serializer: Any) {
        val kClass = serializer::class
        val pool = serializerPools[kClass] ?: return

        if (pool.size < maxPoolSize) {
            @Suppress("UNCHECKED_CAST")
            pool.add(serializer)
        }
    }

    fun acquireJson(prettyPrint: Boolean = false): Json {
        val pool = jsonPools.getOrPut(prettyPrint) { mutableListOf() }
        return if (pool.isNotEmpty()) {
            pool.removeAt(pool.size - 1)
        } else {
            Json {
                this.prettyPrint = prettyPrint
                this.ignoreUnknownKeys = true
                this.encodeDefaults = true
            }
        }
    }

    fun releaseJson(json: Json) {
        val prettyPrint = json.serializersModule.toString().contains("prettyPrint")
        val pool = jsonPools[prettyPrint] ?: return

        if (pool.size < maxPoolSize) {
            pool.add(json)
        }
    }

    fun acquireBuffer(size: Int): ByteArray {
        val sizeClass = when {
            size <= 256 -> 256
            size <= 1024 -> 1024
            size <= 4096 -> 4096
            size <= 16384 -> 16384
            else -> 65536
        }

        val pool = bufferPools.getOrPut(sizeClass) { mutableListOf() }
        return if (pool.isNotEmpty()) {
            pool.removeAt(pool.size - 1)
        } else {
            ByteArray(size)
        }
    }

    fun releaseBuffer(buffer: ByteArray) {
        val sizeClass = when {
            buffer.size <= 256 -> 256
            buffer.size <= 1024 -> 1024
            buffer.size <= 4096 -> 4096
            buffer.size <= 16384 -> 16384
            else -> 65536
        }

        val pool = bufferPools[sizeClass] ?: return

        if (pool.size < maxPoolSize) {
            pool.add(buffer)
        }
    }

    fun clear() {
        serializerPools.values.forEach { it.clear() }
        jsonPools.values.forEach { it.clear() }
        bufferPools.values.forEach { it.clear() }
    }

    fun getPoolStats(): PoolStats {
        return PoolStats(
            serializerPoolSize = serializerPools.values.sumOf { it.size },
            jsonPoolSize = jsonPools.values.sumOf { it.size },
            bufferPoolSize = bufferPools.values.sumOf { it.size },
            totalPools = serializerPools.size + jsonPools.size + bufferPools.size
        )
    }

    data class PoolStats(
        val serializerPoolSize: Int,
        val jsonPoolSize: Int,
        val bufferPoolSize: Int,
        val totalPools: Int
    )
}
