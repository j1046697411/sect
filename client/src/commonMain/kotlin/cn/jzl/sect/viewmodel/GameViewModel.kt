package cn.jzl.sect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.jzl.ecs.World
import cn.jzl.sect.core.quest.PolicyComponent
import cn.jzl.sect.engine.*
import cn.jzl.sect.engine.WorldProvider
import cn.jzl.sect.engine.service.WorldQueryService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 游戏视图模型
 * 管理游戏循环、时间流逝和玩家交互
 */
class GameViewModel : ViewModel() {

    // 通过WorldProvider获取World实例
    private val world: World = WorldProvider.world
    private val systems: SectSystems = SectWorld.getSystems(world)
    private val queryService = WorldQueryService(world)

    // 游戏循环
    private val gameLoop: GameLoop = GameLoop(world, systems)

    // 游戏状态
    val gameState: StateFlow<GameState> = gameLoop.gameState

    // 游戏速度
    val gameSpeed: StateFlow<GameSpeed> = gameLoop.gameSpeed

    // 当前游戏时间
    private val _currentTime = MutableStateFlow("第1年1月1日")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    // 待审批任务
    private val _pendingTasks = MutableStateFlow<List<TaskInfo>>(emptyList())
    val pendingTasks: StateFlow<List<TaskInfo>> = _pendingTasks.asStateFlow()

    // 已完成任务
    private val _completedTasks = MutableStateFlow<List<CompletedTaskInfo>>(emptyList())
    val completedTasks: StateFlow<List<CompletedTaskInfo>> = _completedTasks.asStateFlow()

    // 当前政策
    private val _currentPolicy = MutableStateFlow<PolicyInfo?>(null)
    val currentPolicy: StateFlow<PolicyInfo?> = _currentPolicy.asStateFlow()

    // 候选人列表
    private val _candidates = MutableStateFlow<List<CandidateInfo>>(emptyList())
    val candidates: StateFlow<List<CandidateInfo>> = _candidates.asStateFlow()

    // 自动刷新任务
    private var refreshJob: kotlinx.coroutines.Job? = null

    init {
        // 启动游戏循环
        gameLoop.start()

        // 定期更新UI
        viewModelScope.launch {
            while (true) {
                updateCurrentTime()
                delay(1000)
            }
        }

        // 启动自动刷新
        startAutoRefresh()

        // 加载初始政策
        loadPolicy()
    }

    /**
     * 启动自动刷新
     */
    private fun startAutoRefresh() {
        refreshJob = viewModelScope.launch {
            while (isActive) {
                refreshTasks()
                delay(2000) // 每2秒刷新一次
            }
        }
    }

    /**
     * 刷新任务列表
     */
    private fun refreshTasks() {
        // TODO: 从ECS世界查询任务列表
        // 暂时使用模拟数据
    }

    /**
     * 更新当前时间显示
     */
    private fun updateCurrentTime() {
        val info = queryService.querySectInfo()
        if (info != null) {
            _currentTime.value = "第${info.currentYear}年${info.currentMonth}月${info.currentDay}日"
        }
    }

    /**
     * 暂停游戏
     */
    fun pauseGame() {
        gameLoop.pause()
    }

    /**
     * 恢复游戏
     */
    fun resumeGame() {
        gameLoop.resume()
    }

    /**
     * 设置游戏速度
     */
    fun setGameSpeed(speed: GameSpeed) {
        gameLoop.setSpeed(speed)
    }

    /**
     * 发布选拔任务
     */
    fun publishSelectionTask(): Boolean {
        return gameLoop.publishSelectionTask()
    }

    /**
     * 审批任务
     */
    fun approveTask(taskId: Long, approved: Boolean): Boolean {
        return gameLoop.approveSelectionTask(taskId, approved)
    }

    /**
     * 执行任务
     */
    fun executeTask(taskId: Long): ExecutionResult? {
        return gameLoop.executeQuest(taskId)
    }

    /**
     * 加载候选人
     */
    fun loadCandidates(taskId: Long) {
        viewModelScope.launch {
            val evaluations = gameLoop.evaluateCandidates(taskId)
            _candidates.value = evaluations.map { eval ->
                CandidateInfo(
                    id = eval.discipleId,
                    name = eval.name,
                    score = eval.totalScore,
                    completionRate = eval.completionRate,
                    efficiency = eval.efficiency,
                    quality = eval.quality,
                    survivalRate = eval.survivalRate
                )
            }
        }
    }

    /**
     * 晋升弟子
     */
    fun promoteDisciples(discipleIds: List<Long>): Boolean {
        return gameLoop.promoteDisciples(discipleIds)
    }

    /**
     * 加载政策
     */
    fun loadPolicy() {
        val policy = gameLoop.getCurrentPolicy()
        _currentPolicy.value = PolicyInfo(
            selectionCycle = policy.selectionCycleYears,
            selectionRatio = policy.selectionRatio,
            cultivationRatio = policy.resourceAllocation.cultivation,
            facilityRatio = policy.resourceAllocation.facility,
            reserveRatio = policy.resourceAllocation.reserve
        )
    }

    /**
     * 保存政策
     */
    fun savePolicy(policyInfo: PolicyInfo): Boolean {
        val policy = PolicyComponent(
            selectionCycleYears = policyInfo.selectionCycle,
            selectionRatio = policyInfo.selectionRatio,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = cn.jzl.sect.core.quest.ResourceAllocation(
                cultivation = policyInfo.cultivationRatio,
                facility = policyInfo.facilityRatio,
                reserve = policyInfo.reserveRatio
            )
        )
        return gameLoop.updatePolicy(policy)
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
        gameLoop.dispose()
    }
}

/**
 * 任务信息
 */
data class TaskInfo(
    val id: Long,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val createdAt: String
)

/**
 * 已完成任务信息
 */
data class CompletedTaskInfo(
    val id: Long,
    val title: String,
    val completionRate: Float,
    val efficiency: Float,
    val quality: Float,
    val survivalRate: Float,
    val casualties: Int
)

/**
 * 任务状态
 */
enum class TaskStatus {
    PENDING_APPROVAL,   // 待审批
    APPROVED,           // 已批准
    IN_PROGRESS,        // 进行中
    COMPLETED,          // 已完成
    CANCELLED           // 已取消
}

/**
 * 政策信息
 */
data class PolicyInfo(
    val selectionCycle: Int,      // 选拔周期（年）
    val selectionRatio: Float,    // 选拔比例
    val cultivationRatio: Int,    // 修炼分配比例
    val facilityRatio: Int,       // 设施分配比例
    val reserveRatio: Int         // 储备分配比例
)

/**
 * 候选人信息
 */
data class CandidateInfo(
    val id: Long,
    val name: String,
    val score: Double,
    val completionRate: Float,
    val efficiency: Float,
    val quality: Float,
    val survivalRate: Float
)
