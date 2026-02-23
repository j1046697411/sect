/**
 * GOAL 目标实现类
 *
 * 提供一个简单的 [GOAPGoal] 实现，使用 lambda 表达式定义目标行为。
 *
 * 使用示例：
 * ```kotlin
 * Goal(
 *     name = "生存",
 *     priority = 10.0,
 *     satisfied = { agent -> getValue(agent, Health) > 0 },
 *     desirability = { agent ->
 *         val health = getValue(agent, Health)
 *         if (health < 30) 1.0 else 0.1
 *     },
 *     heuristic = { agent ->
 *         (100 - getValue(agent, Health)).toDouble()
 *     }
 * )
 * ```
 */
package cn.jzl.sect.ai.goap

import cn.jzl.ecs.entity.Entity

/**
 * GOAP 目标的具体实现
 *
 * @property name 目标名称
 * @property priority 目标优先级，默认为 1.0
 * @property satisfied 判断目标是否满足的函数
 * @property desirability 计算期望度的函数，默认为始终返回 1.0
 * @property heuristic 计算启发式值的函数，默认为始终返回 1.0
 */
data class Goal(
    override val name: String,
    override val priority: Double = 1.0,
    private val satisfied: WorldStateReader.(Entity) -> Boolean,
    private val desirability: WorldStateReader.(Entity) -> Double = { 1.0 },
    private val heuristic: WorldStateReader.(Entity) -> Double = { 1.0 }
) : GOAPGoal {

    override fun isSatisfied(worldState: WorldStateReader, agent: Entity): Boolean {
        return worldState.satisfied(agent)
    }

    override fun calculateDesirability(worldState: WorldStateReader, agent: Entity): Double {
        return worldState.desirability(agent)
    }

    override fun calculateHeuristic(worldState: WorldStateReader, agent: Entity): Double {
        return worldState.heuristic(agent)
    }
}
