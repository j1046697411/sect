package cn.jzl.ecs.serialization.addon

import cn.jzl.di.instance
import cn.jzl.di.singleton
import cn.jzl.ecs.addon.WorldSetup
import cn.jzl.ecs.addon.createAddon


val SerializationAddon = createAddon<SerializationBuilder, Unit>(
    name = "Serialization",
    configurationFactory = { SerializationBuilder() }
) {
    injects { this bind singleton { configuration.build(instance()) } }
}

fun WorldSetup.serialization(configure: SerializationBuilder.() -> Unit): Unit = install(SerializationAddon, configure)


