package cn.jzl.ecs.addon

enum class Phase {
    ADDONS_CONFIGURED,
    INIT_COMPONENTS,
    INIT_SYSTEMS,
    INIT_ENTITIES,
    ENABLE
}