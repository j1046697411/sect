package cn.jzl.ecs.serialization.addon

import cn.jzl.ecs.component.Component
import cn.jzl.ecs.serialization.core.ComponentSerializers
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import kotlin.reflect.KClass

class ComponentSerializersBuilder {
    val modules = mutableListOf<SerializersModule>()
    val serialNameToClass = mutableMapOf<String, KClass<out Component>>()

    fun build(): ComponentSerializers {
        return ComponentSerializersImpl(modules.fold(EmptySerializersModule()) { acc, module ->
            acc.overwriteWith(module)
        }, serialNameToClass.toMap())
    }
}