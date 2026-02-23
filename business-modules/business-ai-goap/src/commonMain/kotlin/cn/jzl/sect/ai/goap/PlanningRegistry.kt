/**
 * GOAP 规划注册表接口
 *
 * 用于注册 GOAP 系统所需的各种提供者和解析器。
 * 实现此接口可以配置规划系统的行为。
 *
 * 使用示例：
 * ```kotlin
 * world.planning {
 *     register(MyActionProvider())
 *     register(MyGoalProvider())
 *     register(MyStateResolverRegistry())
 * }
 * ```
 */
package cn.jzl.sect.ai.goap

import cn.jzl.ecs.WorldOwner

/**
 * 规划注册表接口
 *
 * 提供注册各种提供者和解析器的方法
 */
interface PlanningRegistry : WorldOwner {
    /**
     * 注册状态解析器注册表
     *
     * @param stateHandlerProvider 状态解析器注册表
     */
    fun register(stateHandlerProvider: StateResolverRegistry)

    /**
     * 注册动作提供者
     *
     * @param actionProvider 动作提供者
     */
    fun register(actionProvider: ActionProvider)

    /**
     * 注册目标提供者
     *
     * @param goalProvider 目标提供者
     */
    fun register(goalProvider: GoalProvider)
}
