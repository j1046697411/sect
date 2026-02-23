/**
 * GOAP 前置条件接口
 *
 * 定义动作执行前必须满足的条件。
 * 是一个函数式接口，可以直接使用 lambda 表达式创建。
 *
 * 使用示例：
 * ```kotlin
 * // 检查是否有足够的资源
 * val hasResources = Precondition { state, agent ->
 *     state.getValue(agent, ResourceCount) >= 10
 * }
 *
 * // 检查是否在安全区域
 * val inSafeZone = Precondition { state, agent ->
 *     state.getValue(agent, Location) == LocationType.SAFE_ZONE
 * }
 * ```
 */
package cn.jzl.sect.ai.goap

import cn.jzl.ecs.entity.Entity

/**
 * 前置条件函数式接口
 *
 * 表示动作执行前必须满足的条件
 */
fun interface Precondition {
    /**
     * 检查条件是否满足
     *
     * @param stateReader 世界状态读取器，用于查询当前状态
     * @param agent 目标智能体
     * @return 条件是否满足
     */
    fun satisfiesCondition(stateReader: WorldStateReader, agent: Entity): Boolean
}
