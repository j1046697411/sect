package cn.jzl.sect.engine

import cn.jzl.ecs.World
import cn.jzl.sect.engine.service.WorldQueryService
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

    /**
     * 开始游戏循环
     */
    fun start() {
        if (gameJob?.isActive == true) return

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

        // 4. 执行资源生产系统
        // systems.resourceProductionSystem 需要检查是否有update方法

        // 5. 执行资源消耗系统
        // systems.resourceConsumptionSystem 需要检查是否有update方法

        // 6. 执行宗门状态系统
        // systems.sectStatusSystem 需要检查是否有update方法
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
