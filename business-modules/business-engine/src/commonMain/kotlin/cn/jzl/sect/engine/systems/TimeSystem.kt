package cn.jzl.sect.engine.systems

import cn.jzl.di.instance
import cn.jzl.ecs.World
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.log.Logger
import cn.jzl.sect.core.time.GameTime
import cn.jzl.sect.core.time.Season
import cn.jzl.sect.core.time.toDisplayString
import cn.jzl.sect.engine.state.GameState
import cn.jzl.sect.engine.state.TimeChangeInfo as StateTimeChangeInfo

/**
 * 时间系统 - 管理游戏时间流逝和季节变化
 * 使用全局 GameState 管理时间状态
 */
class TimeSystem(private val world: World) {

    private val log: Logger by world.di.instance(argProvider = { "TimeSystem" })
    private val gameState = GameState.getInstance()

    init {
        // 注册时间变更监听器，同步更新 ECS 世界
        gameState.addTimeChangeListener { timeChangeInfo: StateTimeChangeInfo ->
            syncTimeToECS(timeChangeInfo.newTime)
        }
    }

    /**
     * 推进游戏时间
     * @param hours 要推进的小时数
     * @return 时间变化信息
     */
    fun advance(hours: Int): cn.jzl.sect.engine.systems.TimeChangeInfo {
        log.debug { "开始推进时间: ${hours}小时" }

        // 使用 GameState 推进时间
        val stateInfo = gameState.advanceTime(hours)

        // 检查是否新的一天/月
        val oldTime = stateInfo.oldTime
        val newTime = stateInfo.newTime

        if (oldTime.day != newTime.day) {
            log.info { "新的一天: 第${newTime.day}天" }
        }

        if (oldTime.month != newTime.month || oldTime.year != newTime.year) {
            log.info { "新的一月: ${newTime.year}年${newTime.month}月" }
        }

        log.debug { "时间推进完成" }

        return TimeChangeInfo(
            oldTime = stateInfo.oldTime,
            newTime = stateInfo.newTime,
            hoursPassed = stateInfo.hoursAdvanced,
            seasonChanged = stateInfo.seasonChanged,
            newSeason = stateInfo.newSeason
        )
    }

    /**
     * 获取当前时间
     */
    fun getCurrentTime(): GameTime {
        return gameState.currentTime
    }

    /**
     * 获取当前季节
     */
    fun getCurrentSeason(): Season {
        return gameState.currentSeason
    }

    /**
     * 同步时间到 ECS 世界
     */
    private fun syncTimeToECS(time: GameTime) {
        val timeQuery = world.query { TimeQueryContext(this) }

        timeQuery.forEach { ctx ->
            world.editor(ctx.entity) {
                it.addComponent(time)
            }
        }
    }

    /**
     * 查询上下文 - 时间
     */
    class TimeQueryContext(world: World) : EntityQueryContext(world) {
        val time: GameTime by component()
    }
}

/**
 * 时间变化信息（兼容旧代码）
 */
data class TimeChangeInfo(
    val oldTime: GameTime,
    val newTime: GameTime,
    val hoursPassed: Int,
    val seasonChanged: Boolean,
    val newSeason: Season?
) {
    fun toDisplayString(): String {
        return buildString {
            appendLine("时间推进：${oldTime.toDisplayString()} → ${newTime.toDisplayString()}")
            appendLine("经过：${hoursPassed}小时")
            if (seasonChanged && newSeason != null) {
                appendLine("季节变化：${newSeason.displayName}")
            }
        }
    }
}

/**
 * 季节显示名称扩展
 */
private val Season.displayName: String
    get() = when (this) {
        Season.SPRING -> "春季"
        Season.SUMMER -> "夏季"
        Season.AUTUMN -> "秋季"
        Season.WINTER -> "冬季"
    }
