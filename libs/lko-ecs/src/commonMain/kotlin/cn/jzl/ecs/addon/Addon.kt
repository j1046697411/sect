package cn.jzl.ecs.addon

import cn.jzl.ecs.ECSDsl
import cn.jzl.ecs.WorldOwner

/**
 * 创建插件
 *
 * 用于创建带有配置类的插件
 *
 * ## 使用示例
 * ```kotlin
 * data class MyConfig(val name: String)
 * class MyPlugin
 *
 * val myAddon = createAddon<MyConfig, MyPlugin>("myAddon", { MyConfig("default") }) {
 *     // 插件初始化逻辑
 *     MyPlugin()
 * }
 * ```
 *
 * @param Configuration 插件配置类型
 * @param Instance 插件实例类型
 * @param name 插件名称
 * @param configurationFactory 配置工厂函数
 * @param init 插件初始化闭包
 * @return 插件实例
 */
@ECSDsl
inline fun <reified Configuration, reified Instance> createAddon(
    name: String,
    noinline configurationFactory: WorldSetup.() -> Configuration,
    crossinline init: AddonSetup<Configuration>.() -> Instance
): Addon<Configuration, Instance> = Addon(name, configurationFactory) {
    AddonSetup(name, it, this).init()
}

/**
 * 创建无配置插件
 *
 * 用于创建简单的、不需要配置的插件
 *
 * ## 使用示例
 * ```kotlin
 * val simpleAddon = createAddon<MyPlugin>("simpleAddon") {
 *     // 插件初始化逻辑
 *     MyPlugin()
 * }
 * ```
 *
 * @param Instance 插件实例类型
 * @param name 插件名称
 * @param init 插件初始化闭包
 * @return 插件实例
 */
@ECSDsl
inline fun <reified Instance> createAddon(
    name: String,
    crossinline init: AddonSetup<Unit>.() -> Instance
): Addon<Unit, Instance> = createAddon(name, {}, init)

/**
 * 配置插件
 *
 * 在插件配置阶段执行的操作
 *
 * @param Configuration 插件配置类型
 * @param configuration 配置闭包
 */
fun <Configuration> AddonSetup<Configuration>.configure(
    configuration: WorldOwner.() -> Unit
): Unit = on(Phase.ADDONS_CONFIGURED, configuration)

/**
 * 注册组件
 *
 * 在组件初始化阶段注册组件类型
 *
 * @param Configuration 插件配置类型
 * @param configuration 组件注册闭包
 */
@ECSDsl
fun <Configuration> AddonSetup<Configuration>.components(
    configuration: WorldOwner.() -> Unit
): Unit = on(Phase.INIT_COMPONENTS, configuration)

/**
 * 注册系统
 *
 * 在系统初始化阶段注册系统
 *
 * @param Configuration 插件配置类型
 * @param configuration 系统注册闭包
 */
@ECSDsl
fun <Configuration> AddonSetup<Configuration>.systems(
    configuration: WorldOwner.() -> Unit
): Unit = on(Phase.INIT_SYSTEMS, configuration)

/**
 * 创建实体
 *
 * 在实体初始化阶段创建初始实体
 *
 * @param Configuration 插件配置类型
 * @param configuration 实体创建闭包
 */
@ECSDsl
fun <Configuration> AddonSetup<Configuration>.entities(
    configuration: WorldOwner.() -> Unit
): Unit = on(Phase.INIT_ENTITIES, configuration)

/**
 * 启动回调
 *
 * 在插件启用阶段执行的回调
 *
 * @param Configuration 插件配置类型
 * @param configuration 启动闭包
 */
@ECSDsl
fun <Configuration> AddonSetup<Configuration>.onStart(
    configuration: WorldOwner.() -> Unit
): Unit = on(Phase.ENABLE, configuration)

/**
 * 插件数据类
 *
 * @param Configuration 插件配置类型
 * @param Instance 插件实例类型
 * @param name 插件名称
 * @param configurationFactory 配置工厂函数
 * @param onInstall 安装回调
 */
@ECSDsl
data class Addon<Configuration, Instance>(
    val name: String,
    val configurationFactory: WorldSetup.() -> Configuration,
    val onInstall: WorldSetup.(Configuration) -> Instance,
)
