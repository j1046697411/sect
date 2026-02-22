package cn.jzl.sect.engine

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.id
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.quest.PolicyComponent
import cn.jzl.sect.engine.service.WorldQueryService
import cn.jzl.sect.quest.systems.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 游戏循环管理器
 * 驱动游戏时间流逝和系统更新
 */
class GameLoop(
    private val world: World,
    private val systems: SectSystems
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var gameJob: Job? = null

    // 游戏状态
    private val _gameState = MutableStateFlow<GameState>(GameState.Paused)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // 游戏速度（每秒游戏天数）
    private val _gameSpeed = MutableStateFlow(GameSpeed.NORMAL)
    val gameSpeed: StateFlow<GameSpeed> = _gameSpeed.asStateFlow()

    // 当前游戏时间
    private val queryService = WorldQueryService(world)

    // 任务系统
    private val selectionTaskSystem = SelectionTaskSystem(world)
    private val teamFormationSystem = TeamFormationSystem(world)
    private val questExecutionSystem = QuestExecutionSystem(world)
    private val elderEvaluationSystem = ElderEvaluationSystem(world)
    private val promotionSystem = PromotionSystem(world)
    private val policySystem = PolicySystem(world)

    // 上次选拔年份
    private var lastSelectionYear: Int = 1

    /**
     * 开始游戏循环
     */
    fun start() {
        if (gameJob?.isActive == true) return

        // 初始化政策系统
        policySystem.initialize(world)

        _gameState.value = GameState.Running
        gameJob = scope.launch {
            while (isActive) {
                if (_gameState.value == GameState.Running) {
                    tick()
                }
                delay(_gameSpeed.value.tickIntervalMs)
            }
        }
    }

    /**
     * 暂停游戏
     */
    fun pause() {
        _gameState.value = GameState.Paused
    }

    /**
     * 恢复游戏
     */
    fun resume() {
        _gameState.value = GameState.Running
    }

    /**
     * 停止游戏循环
     */
    fun stop() {
        gameJob?.cancel()
        gameJob = null
        _gameState.value = GameState.Stopped
    }

    /**
     * 设置游戏速度
     */
    fun setSpeed(speed: GameSpeed) {
        _gameSpeed.value = speed
    }

    /**
     * 单步执行（用于调试）
     */
    fun step() {
        if (_gameState.value == GameState.Paused) {
            tick()
        }
    }

    /**
     * 游戏刻 - 执行一次系统更新
     */
    private fun tick() {
        val speed = _gameSpeed.value
        if (speed == GameSpeed.PAUSE) return

        // 计算本次tick应该推进的时间（小时）
        // 每秒执行 1000/tickIntervalMs 次tick
        // 每次tick推进 (daysPerSecond * 24) / (1000/tickIntervalMs) 小时
        val ticksPerSecond = 1000f / speed.tickIntervalMs.toFloat()
        val hoursPerTick = (speed.daysPerSecond * 24) / ticksPerSecond
        val hoursToAdvance = hoursPerTick.toInt().coerceAtLeast(1)

        // 1. 推进时间系统
        systems.timeSystem.advance(hoursToAdvance)

        // 2. 执行AI行为系统
        systems.behaviorSystem.update(1.0f)

        // 3. 执行修炼系统
        systems.cultivationSystem.update(hoursToAdvance)

        // 4. 检查选拔周期
        checkSelectionCycle()

        // 5. 执行资源生产系统
        // systems.resourceProductionSystem 需要检查是否有update方法

        // 6. 执行资源消耗系统
        // systems.resourceConsumptionSystem 需要检查是否有update方法

        // 7. 执行宗门状态系统
        // systems.sectStatusSystem 需要检查是否有update方法
    }

    /**
     * 检查选拔周期
     */
    private fun checkSelectionCycle() {
        val sectInfo = queryService.querySectInfo() ?: return
        val currentYear = sectInfo.currentYear

        val policy = policySystem.getCurrentPolicy(world)
        val cycleYears = policy.selectionCycleYears

        if (selectionTaskSystem.checkSelectionCycle(currentYear, lastSelectionYear, cycleYears)) {
            // 计算选拔名额
            val stats = queryService.queryDiscipleStatistics()
            val quota = selectionTaskSystem.calculateSelectionQuota(stats.outerCount, policy.selectionRatio.toDouble())

            if (quota > 0) {
                // 自动创建选拔任务
                selectionTaskSystem.createSelectionTask(world, quota)
                lastSelectionYear = currentYear
            }
        }
    }

    /**
     * 发布选拔任务（手动触发）
     */
    fun publishSelectionTask(): Boolean {
        val policy = policySystem.getCurrentPolicy(world)
        val stats = queryService.queryDiscipleStatistics()
        val quota = selectionTaskSystem.calculateSelectionQuota(stats.outerCount, policy.selectionRatio.toDouble())

        return if (quota > 0) {
            selectionTaskSystem.createSelectionTask(world, quota)
            true
        } else {
            false
        }
    }

    /**
     * 审批选拔任务
     */
    fun approveSelectionTask(questId: Long, approved: Boolean): Boolean {
        return try {
            if (approved) {
                // 批准任务，组建团队
                val result = teamFormationSystem.formTeam(world, questId)
                result.success
            } else {
                // 拒绝任务，更新状态
                // TODO: 实现拒绝逻辑
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 组建任务团队
     */
    fun formTaskTeam(questId: Long): Boolean {
        return try {
            val result = teamFormationSystem.formTeam(world, questId)
            result.success
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 执行任务
     */
    fun executeQuest(questId: Long): ExecutionResult? {
        return try {
            val result = questExecutionSystem.executeQuest(world, questId)
            ExecutionResult(
                completionRate = result.completionRate,
                efficiency = result.efficiency,
                quality = result.quality,
                survivalRate = result.survivalRate,
                casualties = result.casualties
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 评估候选人
     */
    fun evaluateCandidates(questId: Long): List<CandidateEvaluation> {
        return try {
            // 获取任务执行结果
            val executionResult = questExecutionSystem.executeQuest(world, questId)
            
            // 获取团队中的外门弟子
            val teamResult = teamFormationSystem.formTeam(world, questId)
            if (!teamResult.success) return emptyList()
            
            // 使用长老评估系统评估每个外门弟子
            val elder = teamResult.elder
            val outerDisciples = teamResult.outerDisciples
            
            if (elder == null || outerDisciples.isEmpty()) return emptyList()
            
            // 获取政策配置中的名额
            val policy = policySystem.getCurrentPolicy(world)
            val stats = queryService.queryDiscipleStatistics()
            val quota = selectionTaskSystem.calculateSelectionQuota(stats.outerCount, policy.selectionRatio.toDouble())
            
            // 提名候选人
            val candidates = elderEvaluationSystem.nominateCandidates(outerDisciples, quota, elder)
            
            // 转换为CandidateEvaluation
            candidates.map { score ->
                CandidateEvaluation(
                    discipleId = score.discipleId.id.toLong(),
                    name = "", // 需要在查询中添加名字
                    totalScore = score.totalScore,
                    completionRate = executionResult.completionRate,
                    efficiency = executionResult.efficiency,
                    quality = executionResult.quality,
                    survivalRate = executionResult.survivalRate
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 晋升弟子
     */
    fun promoteDisciples(discipleIds: List<Long>): Boolean {
        return try {
            discipleIds.forEach { id ->
                val entity = findEntityById(id.toInt())
                if (entity != null) {
                    promotionSystem.promoteDisciple(entity)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 根据ID查找实体
     */
    private fun findEntityById(id: Int): cn.jzl.ecs.entity.Entity? {
        val query = world.query { EntityIdQueryContext(world) }
        var foundEntity: cn.jzl.ecs.entity.Entity? = null

        query.forEach { ctx ->
            if (ctx.entity.id == id) {
                foundEntity = ctx.entity
            }
        }

        return foundEntity
    }

    /**
     * 查询上下文 - 实体ID
     */
    class EntityIdQueryContext(world: World) : EntityQueryContext(world)

    /**
     * 获取当前政策
     */
    fun getCurrentPolicy(): PolicyComponent {
        return policySystem.getCurrentPolicy(world)
    }

    /**
     * 更新政策
     */
    fun updatePolicy(policy: PolicyComponent): Boolean {
        return policySystem.updatePolicy(world, policy)
    }

    /**
     * 释放资源
     */
    fun dispose() {
        stop()
        scope.cancel()
    }
}

/**
 * 游戏状态
 */
enum class GameState {
    Running,    // 运行中
    Paused,     // 暂停
    Stopped     // 停止
}

/**
 * 游戏速度
 * @param daysPerSecond 每秒游戏天数
 * @param tickIntervalMs 每次tick间隔（毫秒）
 */
enum class GameSpeed(
    val daysPerSecond: Int,
    val tickIntervalMs: Long,
    val displayName: String
) {
    PAUSE(0, Long.MAX_VALUE, "暂停"),
    SLOW(1, 1000L, "慢速"),
    NORMAL(5, 200L, "正常"),
    FAST(30, 33L, "快速"),
    ULTRA(100, 10L, "极速")
}

/**
 * 候选人评估结果
 */
data class CandidateEvaluation(
    val discipleId: Long,
    val name: String,
    val totalScore: Double,
    val completionRate: Float,
    val efficiency: Float,
    val quality: Float,
    val survivalRate: Float
)

/**
 * 任务执行结果
 */
data class ExecutionResult(
    val completionRate: Float,
    val efficiency: Float,
    val quality: Float,
    val survivalRate: Float,
    val casualties: Int
)
