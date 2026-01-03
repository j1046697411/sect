package cn.jzl.ecs.addon

import androidx.collection.mutableScatterMapOf
import cn.jzl.di.singleton
import cn.jzl.ecs.WorldOwner

data class WorldSetup(val injector: Injector, val phaseTaskRegistry: (String, Phase, WorldOwner.() -> Unit) -> Unit) {

    @PublishedApi
    internal val addonInstallers = mutableScatterMapOf<Addon<*, *>, AddonInstaller<*, *>>()

    inline fun <reified Configuration, reified Instance> install(
        addon: Addon<Configuration, Instance>,
        configuration: Configuration.() -> Unit = {}
    ) {
        val addonInstaller = addonInstallers.getOrPut(addon) {
            val configuration = addon.run { configurationFactory() }
            val addonInstaller = AddonInstaller(addon, configuration)
            injector.inject {
                val instance = addon.run { onInstall(configuration) }
                if (instance == null || instance == Unit) return@inject
                println("addon ${addon.name} instance $instance")
                this bind singleton { instance }
            }
            addonInstaller
        }
        @Suppress("UNCHECKED_CAST")
        addonInstaller as AddonInstaller<Configuration, Instance>
        addonInstaller.config.configuration()
    }
}