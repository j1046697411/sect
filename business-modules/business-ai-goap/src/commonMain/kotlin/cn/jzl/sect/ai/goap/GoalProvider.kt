/**
 * 目标提供者接口
 *
 * 用于向规划系统提供可用目标的接口。
 * 实现此接口可以根据智能体的当前状态动态提供不同的目标。
 *
 * 使用示例：
 * ```kotlin
 * class SurvivalGoalProvider : GoalProvider {
 *     override fun getGoals(stateReader: WorldStateReader, agent: Entity): Sequence<GOAPGoal> {
 *         val health = stateReader.getValue(agent, Health)
 *         return if (health < 30) {
 *             sequenceOf(survivalGoal, findHealthGoal)
 *         } else {
 *             sequenceOf(exploreGoal, gatherResourceGoal)
 *         }
 *     }
 * }
 * ```
 */
package cn.jzl.sect.ai.goap

import cn.jzl.ecs.entity.Entity

/**
 * 目标提供者接口
 *
 * 根据智能体的当前状态提供可追求的目标序列
 */
interface GoalProvider {
    /**
     * 获取智能体可追求的目标序列
     *
     * @param stateReader 世界状态读取器，用于查询当前状态
     * @param agent 目标智能体实体
     * @return 该智能体当前可追求的目标序列
     */
    fun getGoals(stateReader: WorldStateReader, agent: Entity): Sequence<GOAPGoal>
}
