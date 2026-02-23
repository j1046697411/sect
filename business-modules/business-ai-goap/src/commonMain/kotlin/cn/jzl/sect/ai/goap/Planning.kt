/**
 * GOAP 规划系统核心模块
 *
 * 包含 GOAP（面向目标的动作规划）系统的核心组件：
 * - 世界状态和智能体状态定义
 * - 动作、目标和规划器接口
 * - A* 搜索算法实现
 * - 规划服务和 addon 配置
 *
 * 使用示例：
 * ```kotlin
 * // 在 World 设置中安装规划 addon
 * world.planning {
 *     register(MyActionProvider())
 *     register(MyGoalProvider())
 *     register(MyStateResolverRegistry())
 * }
 *
 * // 使用规划服务
 * val planningService by world.di.instance<PlanningService>()
 * val plan = planningService.planBestGoal(agent)
 * if (plan != null) {
 *     planningService.execPlan(agent, plan)
 * }
 * ```
 */
package cn.jzl.sect.ai.goap

import cn.jzl.di.instance
import cn.jzl.di.new
import cn.jzl.di.singleton
import cn.jzl.ecs.ECSDsl
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.addon.AddonSetup
import cn.jzl.ecs.addon.Phase
import cn.jzl.ecs.addon.WorldSetup
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.component.tag
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.log.logAddon

/**
 * 增加整数值状态
 *
 * @param agent 智能体实体
 * @param key 整数类型的状态键
 * @param value 增加的值
 */
fun WorldStateWriter.increase(agent: Entity, key: StateKey<Int>, value: Int) {
    setValue(key, getValue(agent, key) + value)
}

/**
 * 减少整数值状态
 *
 * @param agent 智能体实体
 * @param key 整数类型的状态键
 * @param value 减少的值
 */
fun WorldStateWriter.decrease(agent: Entity, key: StateKey<Int>, value: Int) {
    setValue(key, getValue(agent, key) - value)
}

/**
 * 增加长整数值状态
 *
 * @param agent 智能体实体
 * @param key 长整数类型的状态键
 * @param value 增加的值
 */
fun WorldStateWriter.increase(agent: Entity, key: StateKey<Long>, value: Long) {
    setValue(key, getValue(agent, key) + value)
}

/**
 * 减少长整数值状态
 *
 * @param agent 智能体实体
 * @param key 长整数类型的状态键
 * @param value 减少的值
 */
fun WorldStateWriter.decrease(agent: Entity, key: StateKey<Long>, value: Long) {
    setValue(key, getValue(agent, key) - value)
}

/**
 * 在 WorldSetup 中安装规划 addon
 *
 * @param block 配置块
 */
@ECSDsl
fun WorldSetup.planning(block: PlanningRegistry.() -> Unit) =
    install(planningAddon) { config(block) }

/**
 * 在 AddonSetup 中安装规划 addon
 *
 * @param block 配置块
 */
@ECSDsl
fun AddonSetup<*>.planning(block: PlanningRegistry.() -> Unit) =
    install(planningAddon) { config(block) }

/**
 * 规划系统 Addon
 *
 * 注册规划相关的服务：
 * - [PlanningService] 规划核心服务
 * - [PlanningExecuteService] 计划执行服务
 * - [OnPlanExecutionCompleted] 计划完成事件标签
 */
val planningAddon = createAddon("planning", { GOAPBuilder() }) {
    install(logAddon)
    injects {
        this bind singleton { new(::PlanningService) }
        this bind singleton { new(::PlanningExecuteService) }
    }
    on(Phase.ENABLE) {
        val service by world.di.instance<PlanningService>()
        configuration.apply(service)
    }

    components {
        world.componentId<OnPlanExecutionCompleted> { it.tag() }
    }
}

/**
 * 智能体世界状态
 *
 * 智能体在规划过程中的状态表示。
 * 维护一个本地状态缓存，支持状态的复制和效果合并。
 *
 * @property planningService 规划服务
 * @property agent 智能体实体
 * @property states 本地状态缓存
 */
class AgentWorldState(
    private val planningService: PlanningService,
    private val agent: Entity,
    private val states: MutableMap<StateKey<*>, Any?> = mutableMapOf()
) : AgentState, EntityRelationContext, WorldStateWriter, WorldOwner by planningService {

    override val stateKeys: Sequence<StateKey<*>> = states.keys.asSequence()

    override fun copy(): AgentState = AgentWorldState(planningService, agent, states.toMutableMap())

    override fun mergeEffects(effects: Sequence<ActionEffect>): AgentState {
        val newWorldState = AgentWorldState(planningService, agent, states.toMutableMap())
        effects.forEach { it.apply(newWorldState, agent) }
        return newWorldState
    }

    override fun satisfiesConditions(
        conditions: Sequence<Precondition>
    ): Boolean = conditions.all { it.satisfiesCondition(this, agent) }

    @Suppress("UNCHECKED_CAST")
    override fun <K : StateKey<T>, T> getValue(agent: Entity, key: K): T {
        return states.getOrPut(key) { planningService.getValue(agent, key) } as T
    }

    override fun <K : StateKey<T>, T> setValue(key: K, value: T) {
        states[key] = value
    }
}

/**
 * A* 规划器
 *
 * 使用 A* 搜索算法从当前状态找到到达目标状态的最优动作序列。
 *
 * 算法特性：
 * - 使用最小堆（优先队列）管理开放列表
 * - 使用状态哈希进行去重
 * - 支持最大搜索深度限制
 *
 * @property planningService 规划服务
 * @property maxSearchDepth 最大搜索深度，默认为 50
 */
class AStarPlanner(
    private val planningService: PlanningService,
    private val maxSearchDepth: Int = 50
) : Planner {

    override fun plan(agent: Entity, goal: GOAPGoal): Plan? {
        val startState = planningService.createAgentState(agent)
        // Materialize once; `Sequence` may be one-shot and can't be safely iterated repeatedly.
        val allActions: List<GOAPAction> = planningService.getAllActions(agent).toList()

        // 如果目标已经满足，返回空计划
        if (goal.isSatisfied(startState, agent)) {
            return Plan(goal, emptyList(), 0.0)
        }

        // 使用最小堆管理开放列表（Kotlin 多平台没有 PriorityQueue）
        val openSet = MinHeap<AStarNode>()
        val closedSet = mutableSetOf<String>()

        // 初始节点
        val startNode = AStarNode(
            worldState = startState,
            actions = emptyList(),
            cost = 0.0,
            heuristic = calculateHeuristic(startState, goal, agent)
        )
        openSet.add(startNode)

        // A*搜索主循环
        while (openSet.isNotEmpty()) {
            val current = openSet.poll()

            // 检查是否达到目标
            if (goal.isSatisfied(current.worldState, agent)) {
                return Plan(goal, current.actions, current.cost)
            }

            // 检查搜索深度限制
            if (current.actions.size >= maxSearchDepth) {
                continue
            }

            // 生成状态哈希用于去重
            val stateHash = getStateHash(current.worldState, agent)
            if (stateHash in closedSet) {
                continue
            }
            closedSet.add(stateHash)

            // 扩展节点：尝试所有可用动作
            for (action in allActions) {
                // 检查前置条件是否满足
                if (!current.worldState.satisfiesConditions(action.preconditions)) continue
                // 应用动作效果，创建新状态
                val newState = current.worldState.mergeEffects(action.effects)
                val newCost = current.cost + action.cost
                val newHeuristic = calculateHeuristic(newState, goal, agent)
                val newActions = current.actions + action

                val newNode = AStarNode(
                    worldState = newState,
                    actions = newActions,
                    cost = newCost,
                    heuristic = newHeuristic
                )

                // 检查新状态是否已经在关闭列表中
                val newStateHash = getStateHash(newState, agent)
                if (newStateHash !in closedSet) {
                    openSet.add(newNode)
                }
            }
        }
        return null
    }

    /**
     * 计算启发式值
     *
     * @param state 世界状态
     * @param goal 目标
     * @param agent 智能体
     * @return 启发式值，目标已满足时返回 0
     */
    private fun calculateHeuristic(state: WorldState, goal: GOAPGoal, agent: Entity): Double {
        if (goal.isSatisfied(state, agent)) return 0.0
        return goal.calculateHeuristic(state, agent)
    }

    /**
     * 生成状态哈希
     *
     * 用于状态去重
     *
     * @param state 世界状态
     * @param agent 智能体
     * @return 状态哈希字符串
     */
    private fun getStateHash(state: WorldState, agent: Entity): String {
        // 使用状态的键和值生成哈希
        val keys = state.stateKeys.sortedBy { it.hashCode() }
        return keys.joinToString("|") { key ->
            // 获取键对应的值并包含在哈希中
            try {
                @Suppress("UNCHECKED_CAST") val value = state.getValue(agent, key as StateKey<Any?>)
                "$key=$value"
            } catch (e: Exception) {
                "$key=null"
            }
        }
    }

    /**
     * A* 搜索节点
     *
     * 表示搜索过程中的一个状态
     *
     * @property worldState 当前世界状态
     * @property actions 到达此状态的动作序列
     * @property cost 累计成本
     * @property heuristic 启发式值
     */
    private data class AStarNode(
        val worldState: AgentState,
        val actions: List<GOAPAction>,
        val cost: Double,
        val heuristic: Double
    ) : Comparable<AStarNode> {
        /**
         * f 成本 = g 成本 + h 启发值
         */
        val fCost: Double get() = cost + heuristic

        override fun compareTo(other: AStarNode): Int {
            return fCost.compareTo(other.fCost)
        }
    }
}

/**
 * 最小堆实现
 *
 * 用于替代 Kotlin 多平台中不可用的 PriorityQueue。
 * 基于 Comparable 接口进行比较，保证堆顶元素始终是最小值。
 *
 * @param T 堆元素类型，必须实现 Comparable
 */
private class MinHeap<T : Comparable<T>> {
    private val data: MutableList<T> = mutableListOf()

    val size: Int get() = data.size

    fun isNotEmpty(): Boolean = data.isNotEmpty()

    /**
     * 添加元素
     *
     * 时间复杂度：O(log n)
     *
     * @param element 要添加的元素
     */
    fun add(element: T) {
        data.add(element)
        siftUp(data.lastIndex)
    }

    /**
     * 取出并移除堆顶元素
     *
     * 时间复杂度：O(log n)
     *
     * @return 堆顶元素（最小值）
     * @throws NoSuchElementException 如果堆为空
     */
    fun poll(): T {
        if (data.isEmpty()) throw NoSuchElementException("Heap is empty")
        val result = data[0]
        val last = data.removeAt(data.lastIndex)
        if (data.isNotEmpty()) {
            data[0] = last
            siftDown(0)
        }
        return result
    }

    private fun siftUp(index: Int) {
        var i = index
        while (i > 0) {
            val parent = (i - 1) / 2
            if (data[i] < data[parent]) {
                data.swap(i, parent)
                i = parent
            } else break
        }
    }

    private fun siftDown(index: Int) {
        var i = index
        val n = data.size
        while (true) {
            val left = 2 * i + 1
            val right = 2 * i + 2
            var smallest = i
            if (left < n && data[left] < data[smallest]) smallest = left
            if (right < n && data[right] < data[smallest]) smallest = right
            if (smallest != i) {
                data.swap(i, smallest)
                i = smallest
            } else break
        }
    }

    private fun MutableList<T>.swap(i: Int, j: Int) {
        val temp = this[i]
        this[i] = this[j]
        this[j] = temp
    }
}
