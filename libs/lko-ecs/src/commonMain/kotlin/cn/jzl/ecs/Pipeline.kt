package cn.jzl.ecs

import cn.jzl.ecs.addon.Phase

/**
 * 管道接口，管理世界的初始化和生命周期任务
 *
 * Pipeline 提供一种机制来按阶段组织和执行世界的初始化任务。
 * 它确保任务按照正确的顺序执行，特别是在插件安装和系统初始化期间。
 *
 * ## 使用场景
 * - 插件初始化
 * - 系统注册
 * - 数据预加载
 * - 启动任务执行
 *
 * ## 使用示例
 * ```kotlin
 * val world = world {
 *     install(myAddon)
 *     // Pipeline 自动管理初始化顺序
 * }
 * ```
 *
 * @see Phase 定义了不同的执行阶段
 */
interface Pipeline {

    /**
     * 在指定阶段或之后执行任务
     *
     * 注册一个任务，使其在指定的 [Phase] 或之后的某个阶段执行
     *
     * @param phase 任务执行的阶段
     * @param task 要执行的任务闭包
     */
    fun runOnOrAfter(phase: Phase, task: WorldOwner.() -> Unit)

    /**
     * 运行所有启动任务
     *
     * 按照阶段顺序执行所有已注册的任务。
     * 此方法通常由 [world] 函数自动调用
     */
    fun runStartupTasks()
}
