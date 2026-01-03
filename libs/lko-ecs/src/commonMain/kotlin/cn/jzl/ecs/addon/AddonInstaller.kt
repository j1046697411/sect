package cn.jzl.ecs.addon

data class AddonInstaller<Configuration, Instance>(
    val addon: Addon<Configuration, Instance>,
    val config: Configuration
)