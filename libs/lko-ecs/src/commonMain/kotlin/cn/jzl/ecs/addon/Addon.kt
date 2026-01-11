package cn.jzl.ecs.addon

import cn.jzl.ecs.ECSDsl
import cn.jzl.ecs.WorldOwner

@ECSDsl
inline fun <reified Configuration, reified Instance> createAddon(
    name: String,
    noinline configurationFactory: WorldSetup.() -> Configuration,
    crossinline init: AddonSetup<Configuration>.() -> Instance
): Addon<Configuration, Instance> = Addon(name, configurationFactory) {
    AddonSetup(name, it, this).init()
}

@ECSDsl
inline fun <reified Instance> createAddon(
    name: String,
    crossinline init: AddonSetup<Unit>.() -> Instance
): Addon<Unit, Instance> = createAddon(name, {}, init)

fun <Configuration> AddonSetup<Configuration>.configure(
    configuration: WorldOwner.() -> Unit
): Unit = on(Phase.ADDONS_CONFIGURED, configuration)

@ECSDsl
fun <Configuration> AddonSetup<Configuration>.components(
    configuration: WorldOwner.() -> Unit
): Unit = on(Phase.INIT_COMPONENTS, configuration)

@ECSDsl
fun <Configuration> AddonSetup<Configuration>.systems(
    configuration: WorldOwner.() -> Unit
): Unit = on(Phase.INIT_SYSTEMS, configuration)

@ECSDsl
fun <Configuration> AddonSetup<Configuration>.entities(
    configuration: WorldOwner.() -> Unit
): Unit = on(Phase.INIT_ENTITIES, configuration)

@ECSDsl
fun <Configuration> AddonSetup<Configuration>.onStart(
    configuration: WorldOwner.() -> Unit
): Unit = on(Phase.ENABLE, configuration)

@ECSDsl
data class Addon<Configuration, Instance>(
    val name: String,
    val configurationFactory: WorldSetup.() -> Configuration,
    val onInstall: WorldSetup.(Configuration) -> Instance,
)