package cn.jzl.ecs.serialization.addon

import cn.jzl.ecs.component.Component
import cn.jzl.ecs.serialization.core.ComponentSerializers
import cn.jzl.ecs.serialization.core.ComponentSerializers.Companion.hasNamespace
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.capturedKClass
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

class ComponentSerializersImpl(
    override val module: SerializersModule,
    private val serialNameToClass: Map<String, KClass<out Component>> = emptyMap()
) : ComponentSerializers {
    private val classToSerialName: Map<KClass<out Component>, String> =
        serialNameToClass.entries.associate { it.value to it.key }

    override fun getClassFor(serialName: String, namespaces: List<String>): KClass<out Component> {
        return serialNameToClass[serialName]
            ?.let { serialNameToClass["geary:$serialName"] }
            ?.let {
                namespaces.firstNotNullOfOrNull { serialNameToClass["$it:$serialName"] }
            } ?: error("No class found for serial name: $serialName")
    }

    @Suppress("OPT_IN_USAGE")
    override fun <T : Component> getSerializerFor(
        key: String,
        baseClass: KClass<in T>
    ): DeserializationStrategy<T>? = module.getPolymorphic(baseClass, key)

    override fun <T : Component> getSerializerFor(kClass: KClass<in T>): DeserializationStrategy<T>? {
        val serialName = getSerialNameFor(kClass) ?: return null
        return getSerializerFor(serialName, kClass)
    }

    override fun getSerialNameFor(kClass: KClass<out Component>): String? {
        return classToSerialName[kClass]
    }

    @Suppress("UNCHECKED_CAST", "OPT_IN_USAGE")
    override fun <T : Any> getKClassFor(serializer: KSerializer<T>): KClass<T>? {
        serializer.descriptor.capturedKClass?.let { return it as KClass<T> }
        return serialNameToClass[serializer.descriptor.serialName] as? KClass<T>
    }
}