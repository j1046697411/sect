/**
 * GOAP 目标接口
 *
 * 定义面向目标动作规划中目标的核心契约。
 * 每个 GOAP 目标包含：
 * - 名称：用于调试和日志
 * - 优先级：用于目标选择
 * - 满足判断：判断目标是否已达成
 * - 期望度：计算目标在当前状态下的重要性
 * - 启发式值：用于 A* 搜索的启发函数
 *
 * @see Goal 具体实现类
 */
package cn.jzl.sect.ai.goap

import cn.jzl.ecs.entity.Entity

/**
 * GOAP 目标接口
 *
 * 定义目标的所有属性，用于 A* 规划算法
 */
interface GOAPGoal {
    /**
     * 目标名称
     *
     * 用于调试、日志和可视化
     */
    val name: String

    /**
     * 目标优先级
     *
     * 用于在多个目标中选择最优先的目标
     * 值越大，优先级越高
     */
    val priority: Double

    /**
     * 判断目标是否已满足
     *
     * @param worldState 世界状态读取器
     * @param agent 目标智能体
     * @return 目标是否已达成
     */
    fun isSatisfied(worldState: WorldStateReader, agent: Entity): Boolean

    /**
     * 计算目标的期望度
     *
     * 用于评估目标在当前状态下的重要性，
     * 结合优先级用于目标选择
     *
     * @param worldState 世界状态读取器
     * @param agent 目标智能体
     * @return 期望度值，值越大越应该追求此目标
     */
    fun calculateDesirability(worldState: WorldStateReader, agent: Entity): Double

    /**
     * 计算启发式值
     *
     * 用于 A* 搜索中的启发函数，
     * 估计从当前状态到达目标的代价
     *
     * @param worldState 世界状态读取器
     * @param agent 目标智能体
     * @return 启发式值，值越小表示越接近目标
     */
    fun calculateHeuristic(worldState: WorldStateReader, agent: Entity): Double
}
