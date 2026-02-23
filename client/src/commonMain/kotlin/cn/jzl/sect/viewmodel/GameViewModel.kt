package cn.jzl.sect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.jzl.ecs.World
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

    // 详细游戏时间（含时辰）
    private val _detailedGameTime = MutableStateFlow<DetailedGameTime?>(null)
    val detailedGameTime: StateFlow<DetailedGameTime?> = _detailedGameTime.asStateFlow()

    // 待审批任务
    private val _pendingTasks = MutableStateFlow<List<TaskInfo>>(emptyList())
    val pendingTasks: StateFlow<List<TaskInfo>> = _pendingTasks.asStateFlow()

    // 已完成任务
    private val _completedTasks = MutableStateFlow<List<CompletedTaskInfo>>(emptyList())
    val completedTasks: StateFlow<List<CompletedTaskInfo>> = _completedTasks.asStateFlow()

    // 资源产量
    private val _resourceProduction = MutableStateFlow<ResourceProductionInfo?>(null)
    val resourceProduction: StateFlow<ResourceProductionInfo?> = _resourceProduction.asStateFlow()

    // 进行中的任务
    private val _activeTasks = MutableStateFlow<List<ActiveTaskInfo>>(emptyList())
    val activeTasks: StateFlow<List<ActiveTaskInfo>> = _activeTasks.asStateFlow()

    // 待处理事件
    private val _pendingEvents = MutableStateFlow<List<PendingEvent>>(emptyList())
    val pendingEvents: StateFlow<List<PendingEvent>> = _pendingEvents.asStateFlow()

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

            // 计算时辰（24小时制转换为12时辰）
            val hour = (info.currentDay * 24 / 30) % 24 // 简化计算
            val timeOfDay = when (hour) {
                in 23..24, in 0..1 -> "子时"
                in 1..3 -> "丑时"
                in 3..5 -> "寅时"
                in 5..7 -> "卯时"
                in 7..9 -> "辰时"
                in 9..11 -> "巳时"
                in 11..13 -> "午时"
                in 13..15 -> "未时"
                in 15..17 -> "申时"
                in 17..19 -> "酉时"
                in 19..21 -> "戌时"
                else -> "亥时"
            }

            _detailedGameTime.value = DetailedGameTime(
                year = info.currentYear,
                month = info.currentMonth,
                day = info.currentDay,
                timeOfDay = timeOfDay
            )

            // 更新资源产量（模拟数据）
            updateResourceProduction()

            // 更新待处理事件
            updatePendingEvents(info.currentYear)
        }
    }

    /**
     * 更新资源产量
     */
    private fun updateResourceProduction() {
        // 模拟资源产量计算
        val stats = queryService.queryDiscipleStatistics()
        val spiritStonesPerHour = stats.totalCount * 2 + stats.innerCount * 5
        val contributionPerHour = stats.totalCount * 1 + stats.elderCount * 3

        _resourceProduction.value = ResourceProductionInfo(
            spiritStonesPerHour = spiritStonesPerHour,
            contributionPointsPerHour = contributionPerHour
        )
    }

    /**
     * 更新待处理事件
     */
    private fun updatePendingEvents(currentYear: Int) {
        val events = mutableListOf<PendingEvent>()

        // 检查是否有弟子可以突破（模拟）
        val disciples = queryService.queryAllDisciples()
        val breakthroughCount = disciples.count { it.cultivation > it.maxCultivation * 0.9 }
        if (breakthroughCount > 0) {
            events.add(PendingEvent.BreakthroughReminder(breakthroughCount))
        }

        _pendingEvents.value = events
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
 * 详细游戏时间
 */
data class DetailedGameTime(
    val year: Int,
    val month: Int,
    val day: Int,
    val timeOfDay: String // 时辰
)

/**
 * 资源产量信息
 */
data class ResourceProductionInfo(
    val spiritStonesPerHour: Int,
    val contributionPointsPerHour: Int
)

/**
 * 进行中任务信息
 */
data class ActiveTaskInfo(
    val id: Long,
    val name: String,
    val progress: Float,
    val remainingDays: Int
)

/**
 * 待处理事件
 */
sealed class PendingEvent {
    data class BreakthroughReminder(val count: Int) : PendingEvent()
}
