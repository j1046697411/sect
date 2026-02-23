/**
 * GOAP 规划器接口
 *
 * 定义面向目标动作规划的核心规划接口。
 * 规划器负责使用搜索算法（如 A*）找到从当前状态到达目标状态的动作序列。
 *
 * @see AStarPlanner 基于 A* 算法的实现
 */
package cn.jzl.sect.ai.goap

import cn.jzl.ecs.entity.Entity

/**
 * 规划器接口
 *
 * 定义从当前状态到目标状态的规划方法
 */
interface Planner {
    /**
     * 为智能体生成达成目标的计划
     *
     * 使用搜索算法找到最优动作序列
     *
     * @param agent 目标智能体
     * @param goal 要达成的目标
     * @return 生成的计划，如果无法找到路径则返回 null
     */
    fun plan(agent: Entity, goal: GOAPGoal): Plan?
}
