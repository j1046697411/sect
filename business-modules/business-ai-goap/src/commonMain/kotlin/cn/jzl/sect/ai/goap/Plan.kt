/**
 * GOAP 计划数据类
 *
 * 表示 A* 规划算法生成的动作执行计划。
 * 一个计划包含：
 * - 目标：计划要达成的目标
 * - 动作序列：按顺序执行的动作列表
 * - 总成本：所有动作成本的总和
 *
 * 使用示例：
 * ```kotlin
 * val plan = planner.plan(agent, goal)
 * if (plan != null) {
 *     println("计划目标: ${plan.goal.name}")
 *     println("动作数: ${plan.actions.size}")
 *     println("总成本: ${plan.cost}")
 * }
 * ```
 */
package cn.jzl.sect.ai.goap

/**
 * GOAP 计划
 *
 * A* 搜索生成的动作执行计划
 *
 * @property goal 计划要达成的目标
 * @property actions 按顺序执行的动作列表
 * @property cost 计划的总成本（所有动作成本之和）
 */
data class Plan(
    val goal: GOAPGoal,
    val actions: List<GOAPAction>,
    val cost: Double
)
