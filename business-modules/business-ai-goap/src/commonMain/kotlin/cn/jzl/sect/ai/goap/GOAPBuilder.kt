/**
 * GOAP 配置构建器
 *
 * 用于收集和延迟应用 GOAP 系统的配置。
 * 在 addon 初始化阶段收集配置，在启用阶段统一应用。
 *
 * 使用示例：
 * ```kotlin
 * val planningAddon = createAddon("planning", { GOAPBuilder() }) {
 *     on(Phase.ENABLE) {
 *         val service by world.di.instance<PlanningService>()
 *         configuration.apply(service)
 *     }
 * }
 * ```
 */
package cn.jzl.sect.ai.goap

/**
 * GOAP 配置构建器
 *
 * 内部类，用于管理配置块的延迟执行
 */
class GOAPBuilder {

    private val configs = mutableListOf<PlanningRegistry.() -> Unit>()

    /**
     * 添加配置块
     *
     * @param block 配置块，将在 [PlanningRegistry] 上下文中执行
     */
    fun config(block: PlanningRegistry.() -> Unit) {
        configs.add(block)
    }

    /**
     * 应用所有配置到规划服务
     *
     * @param service 规划服务实例
     */
    internal fun apply(service: PlanningService) {
        configs.forEach { config -> service.config() }
    }
}
