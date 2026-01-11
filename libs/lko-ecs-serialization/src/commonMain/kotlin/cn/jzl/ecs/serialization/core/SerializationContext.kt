package cn.jzl.ecs.serialization.core

import cn.jzl.ecs.World
import cn.jzl.ecs.component.Component
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

data class SerializationContext(
    val world: World,
    val serializers: ComponentSerializers,
    val config: SerializationConfig,
    val module: SerializersModule = serializers.module
) {
    fun <T : Component> getSerializer(kClass: KClass<in T>): KSerializer<T>? {
        return serializers.getSerializerFor(kClass) as? KSerializer<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Component> getSerializer(serialName: String): KSerializer<T>? {
        val kClass = serializers.getClassFor(serialName) as? KClass<T>
        return if (kClass != null) getSerializer(kClass) else null
    }
}