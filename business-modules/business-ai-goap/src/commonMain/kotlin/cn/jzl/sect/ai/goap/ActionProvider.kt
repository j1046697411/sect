/**
 * 动作提供者接口
 *
 * 用于向规划系统提供可用动作的接口。
 * 实现此接口可以根据智能体的当前状态动态提供不同的动作。
 *
 * 使用示例：
 * ```kotlin
 * class CombatActionProvider : ActionProvider {
 *     override fun getActions(stateReader: WorldStateReader, agent: Entity): Sequence<GOAPAction> {
 *         val hasWeapon = stateReader.getValue(agent, HasWeapon)
 *         return if (hasWeapon) {
 *             sequenceOf(attackAction, defendAction)
 *         } else {
 *             sequenceOf(fleeAction)
 *         }
 *     }
 * }
 * ```
 */
package cn.jzl.sect.ai.goap

import cn.jzl.ecs.entity.Entity

/**
 * 动作提供者接口
 *
 * 根据智能体的当前状态提供可用的动作序列
 */
interface ActionProvider {
    /**
     * 获取智能体可用的动作序列
     *
     * @param stateReader 世界状态读取器，用于查询当前状态
     * @param agent 目标智能体实体
     * @return 该智能体当前可执行的动作序列
     */
    fun getActions(stateReader: WorldStateReader, agent: Entity): Sequence<GOAPAction>
}
