package cn.jzl.sect.engine.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.time.Season
import cn.jzl.sect.core.time.TimeComponent

/**
 * 时间系统 - 管理游戏时间流逝和季节变化
 */
class TimeSystem(private val world: World) {

    /**
     * 推进游戏时间
     * @param hours 要推进的小时数
     * @return 时间变化信息
     */
    fun advance(hours: Int): TimeChangeInfo {
        val timeQuery = world.query { TimeQueryContext(this) }
        var oldTime: TimeComponent? = null
        var newTime: TimeComponent? = null
        var targetEntity: cn.jzl.ecs.entity.Entity? = null

        // 先收集信息
        timeQuery.forEach { ctx ->
            oldTime = ctx.time
            newTime = ctx.time.addHours(hours)
            targetEntity = ctx.entity
        }

        // 再应用更新
        if (targetEntity != null && newTime != null) {
            world.editor(targetEntity!!) {
                it.addComponent(newTime!!)
            }
        }

        val oldSeason = oldTime?.let { getSeason(it.month) }
        val newSeason = newTime?.let { getSeason(it.month) }
        val seasonChanged = oldSeason != newSeason

        return TimeChangeInfo(
            oldTime = oldTime ?: TimeComponent(),
            newTime = newTime ?: TimeComponent(),
            hoursPassed = hours,
            seasonChanged = seasonChanged,
            newSeason = newSeason
        )
    }

    /**
     * 获取当前时间
     */
    fun getCurrentTime(): TimeComponent? {
        val timeQuery = world.query { TimeQueryContext(this) }
        var currentTime: TimeComponent? = null
        
        timeQuery.forEach { ctx ->
            currentTime = ctx.time
        }
        
        return currentTime
    }

    /**
     * 根据月份获取季节
     */
    private fun getSeason(month: Int): Season {
        return when (month) {
            in 3..5 -> Season.SPRING
            in 6..8 -> Season.SUMMER
            in 9..11 -> Season.AUTUMN
            else -> Season.WINTER
        }
    }

    /**
     * 查询上下文 - 时间
     */
    class TimeQueryContext(world: World) : EntityQueryContext(world) {
        val time: TimeComponent by component()
    }

    /**
     * 时间变化信息
     */
    data class TimeChangeInfo(
        val oldTime: TimeComponent,
        val newTime: TimeComponent,
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
