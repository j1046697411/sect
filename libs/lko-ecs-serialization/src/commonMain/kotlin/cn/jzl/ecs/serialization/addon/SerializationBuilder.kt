package cn.jzl.ecs.serialization.addon

import cn.jzl.ecs.ECSDsl
import cn.jzl.ecs.World
import cn.jzl.ecs.component.Component
import cn.jzl.ecs.serialization.core.SerializationConfig
import cn.jzl.ecs.serialization.core.VersionManager
import cn.jzl.ecs.serialization.format.Format
import cn.jzl.ecs.serialization.format.FormatsBuilder
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializerOrNull
import kotlin.collections.plusAssign
import kotlin.reflect.KClass

data class SerializationBuilder(
    val serializers: ComponentSerializersBuilder = ComponentSerializersBuilder(),
    val formats: FormatsBuilder = FormatsBuilder(),
    var config: SerializationConfig = SerializationConfig(),
    var versionManager: VersionManager = VersionManager()
) {

    @ECSDsl
    fun module(init: SerializersModuleBuilder.() -> Unit) {
        serializers.modules += SerializersModule(init)
    }

    @ECSDsl
    fun components(init: PolymorphicModuleBuilder<Component>.() -> Unit): Unit = module {
        polymorphic(Component::class, builderAction = init)
    }

    fun format(ext: String, formatFactory: (SerializersModule) -> Format) {
        formats.register(ext, formatFactory)
    }

    inline fun <reified T : Component> PolymorphicModuleBuilder<T>.component(serializer: KSerializer<T>) {
        subclass(T::class, serializer)
    }

    @OptIn(InternalSerializationApi::class)
    fun <T : Any> PolymorphicModuleBuilder<T>.component(
        kClass: KClass<T>,
        serializer: KSerializer<T> = kClass.serializerOrNull()
            ?: error("No serializer found for $kClass while registering serializable component")
    ) {
        val serialName = serializer.descriptor.serialName
        serializers.serialNameToClass[serialName] = kClass as KClass<out Component>
        subclass(kClass, serializer)
    }

    inline fun <reified T : Any> namedComponent(name: String) {
        serializers.serialNameToClass[name] = T::class
    }

    fun build(world: World): SerializationModule {
        module {
            contextual<World>(WorldProviderSerializer(world))
        }
        val serializersModule = serializers.build()
        return SerializationModule(
            world = world,
            serializers = serializersModule,
            formats = formats.build(serializersModule),
            config = config,
            versionManager = versionManager
        )
    }
}