package cn.jzl.ecs.addon

import cn.jzl.di.DIMainBuilder
import cn.jzl.ecs.WorldOwner

data class AddonSetup<Configuration>(
    val name: String,
    val configuration: Configuration,
    @PublishedApi internal val worldSetup: WorldSetup
) {

    fun injects(configuration: DIMainBuilder.() -> Unit) {
        worldSetup.injector.inject(configuration)
    }

    inline fun <reified Configuration1, reified Instance> install(
        addon: Addon<Configuration1, Instance>,
        configuration: Configuration1.() -> Unit = {}
    ): Unit = worldSetup.install(addon, configuration)

    fun on(phase: Phase, configuration: WorldOwner.() -> Unit) {
        worldSetup.phaseTaskRegistry(name, phase, configuration)
    }
}