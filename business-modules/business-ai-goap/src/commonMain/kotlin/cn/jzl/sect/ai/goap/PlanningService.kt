/**
 * GOAP 规划服务
 *
 * GOAP 系统的核心服务，负责：
 * - 管理动作提供者、目标提供者和状态解析器
 * - 创建智能体状态和世界状态
 * - 执行规划算法
 * - 协调计划执行
 *
 * 使用示例：
 * ```kotlin
 * val planningService by world.di.instance<PlanningService>()
 *
 * // 为智能体规划最佳目标
 * val plan = planningService.planBestGoal(agent)
 *
 * // 执行计划
 * if (plan != null) {
 *     planningService.execPlan(agent, plan)
 * }
 * ```
 */
package cn.jzl.sect.ai.goap

import cn.jzl.di.instance
import cn.jzl.ecs.World
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext

/**
 * 规划服务
 *
 * GOAP 系统的核心服务，实现 [PlanningRegistry] 接口
 *
 * @property world ECS 世界实例
 */
class PlanningService(override val world: World) : EntityRelationContext, WorldStateReader, PlanningRegistry {

    private val actionProviders = mutableSetOf<ActionProvider>()
    private val goalProviders = mutableSetOf<GoalProvider>()
    private val stateHandlerProviders = mutableListOf<StateResolverRegistry>()
    private val planner: Planner = AStarPlanner(this)
    private val planningExecuteService by world.di.instance<PlanningExecuteService>()

    override fun register(stateHandlerProvider: StateResolverRegistry) {
        this.stateHandlerProviders.add(stateHandlerProvider)
    }

    override fun register(actionProvider: ActionProvider) {
        this.actionProviders.add(actionProvider)
    }

    override fun register(goalProvider: GoalProvider) {
        this.goalProviders.add(goalProvider)
    }

    /**
     * 创建智能体状态
     *
     * @param agent 智能体实体
     * @return 新的智能体状态实例
     */
    fun createAgentState(agent: Entity): AgentState = AgentWorldState(this, agent)

    /**
     * 创建世界状态
     *
     * @param map 初始状态键值对
     * @return 新的世界状态实例
     */
    fun createWorldState(map: Map<StateKey<*>, Any?>): WorldState = WorldStateImpl(map)

    override fun <K : StateKey<T>, T> getValue(agent: Entity, key: K): T {
        return getStateHandler(key).run { getWorldState(agent, key) }
    }

    /**
     * 获取智能体可用的所有动作
     *
     * @param agent 智能体实体
     * @return 所有可用动作的序列
     */
    fun getAllActions(agent: Entity): Sequence<GOAPAction> = actionProviders.asSequence().flatMap { it.getActions(this, agent) }

    private fun getAllGoals(agent: Entity): Sequence<GOAPGoal> = goalProviders.asSequence().flatMap { it.getGoals(this, agent) }

    /**
     * 为智能体规划达成特定目标的计划
     *
     * @param agent 智能体实体
     * @param goal 要达成的目标
     * @return 生成的计划，如果无法找到路径则返回 null
     */
    fun plan(agent: Entity, goal: GOAPGoal): Plan? = planner.plan(agent, goal)

    /**
     * 为智能体规划最佳目标的计划
     *
     * 从所有可用目标中选择优先级和期望度最高的目标进行规划
     *
     * @param agent 智能体实体
     * @return 生成的计划，如果没有可执行的计划则返回 null
     */
    fun planBestGoal(agent: Entity): Plan? {
        val worldState = createAgentState(agent)
        return getAllGoals(agent).map { goal -> goal to goal.calculateDesirability(worldState, agent) }.sortedByDescending { (goal, desirability) -> goal.priority * desirability }
            .mapNotNull { planner.plan(agent, it.first) }.firstOrNull()
    }

    /**
     * 执行计划
     *
     * @param agent 智能体实体
     * @param plan 要执行的计划
     * @return 执行任务的调度实体
     */
    fun execPlan(agent: Entity, plan: Plan): Entity = planningExecuteService.executePlan(agent, plan)

    /**
     * 获取状态键对应的解析器
     *
     * @param K 状态键类型
     * @param T 状态值类型
     * @param key 状态键
     * @return 状态解析器
     */
    fun <K : StateKey<T>, T> getStateHandler(key: K): StateResolver<K, T> {
        return stateHandlerProviders.asSequence().mapNotNull { it.getStateHandler(key) }.first()
    }
}
