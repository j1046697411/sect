package cn.jzl.sect.engine.state

import cn.jzl.sect.core.time.GameTime
import cn.jzl.sect.core.time.Season

/**
 * 游戏全局状态管理器
 * 管理游戏时间、季节等全局状态
 */
class GameState {

    // 当前游戏时间
    private var _currentTime: GameTime = GameTime()
    val currentTime: GameTime get() = _currentTime

    // 当前季节 - 根据当前时间计算
    private var _currentSeason: Season = calculateSeason(GameTime().month)
    val currentSeason: Season get() = _currentSeason

    // 游戏是否暂停
    private var _isPaused: Boolean = false
    val isPaused: Boolean get() = _isPaused

    // 游戏速度倍率（1.0 = 正常速度）
    private var _gameSpeed: Float = 1.0f
    val gameSpeed: Float get() = _gameSpeed

    // 时间变更监听器列表
    private val timeChangeListeners = mutableListOf<(TimeChangeInfo) -> Unit>()

    /**
     * 推进游戏时间
     * @param hours 推进的小时数
     * @return 时间变更信息
     */
    fun advanceTime(hours: Int): TimeChangeInfo {
        val oldTime = _currentTime
        val newTime = calculateNewTime(oldTime, hours)
        val oldSeason = _currentSeason
        val newSeason = calculateSeason(newTime.month)

        _currentTime = newTime

        // 如果季节变更，更新季节状态
        if (newSeason != oldSeason) {
            _currentSeason = newSeason
        }

        val timeChangeInfo = TimeChangeInfo(
            oldTime = oldTime,
            newTime = newTime,
            hoursAdvanced = hours,
            dayChanged = oldTime.day != newTime.day,
            monthChanged = oldTime.month != newTime.month,
            yearChanged = oldTime.year != newTime.year,
            seasonChanged = oldSeason != newSeason,
            oldSeason = oldSeason,
            newSeason = newSeason
        )

        // 通知所有监听器
        timeChangeListeners.forEach { it(timeChangeInfo) }

        return timeChangeInfo
    }

    /**
     * 注册时间变更监听器
     */
    fun addTimeChangeListener(listener: (TimeChangeInfo) -> Unit) {
        timeChangeListeners.add(listener)
    }

    /**
     * 移除时间变更监听器
     */
    fun removeTimeChangeListener(listener: (TimeChangeInfo) -> Unit) {
        timeChangeListeners.remove(listener)
    }

    /**
     * 设置游戏时间
     */
    fun setTime(time: GameTime) {
        _currentTime = time
        _currentSeason = calculateSeason(time.month)
    }

    /**
     * 设置游戏暂停状态
     */
    fun setPaused(paused: Boolean) {
        _isPaused = paused
    }

    /**
     * 设置游戏速度
     */
    fun setGameSpeed(speed: Float) {
        _gameSpeed = speed.coerceIn(0.1f, 10.0f)
    }

    /**
     * 计算新的游戏时间
     */
    private fun calculateNewTime(current: GameTime, hours: Int): GameTime {
        var newHour = current.hour + hours
        var newDay = current.day
        var newMonth = current.month
        var newYear = current.year

        // 每24小时为一天
        while (newHour >= 24) {
            newHour -= 24
            newDay++

            // 每月30天
            if (newDay > 30) {
                newDay = 1
                newMonth++

                // 每年12个月
                if (newMonth > 12) {
                    newMonth = 1
                    newYear++
                }
            }
        }

        return GameTime(
            year = newYear,
            month = newMonth,
            day = newDay,
            hour = newHour
        )
    }

    /**
     * 根据月份计算季节
     */
    private fun calculateSeason(month: Int): Season {
        return when (month) {
            in 3..5 -> Season.SPRING
            in 6..8 -> Season.SUMMER
            in 9..11 -> Season.AUTUMN
            else -> Season.WINTER
        }
    }

    companion object {
        @Volatile
        private var instance: GameState? = null

        /**
         * 获取单例实例
         */
        fun getInstance(): GameState {
            return instance ?: synchronized(this) {
                instance ?: GameState().also { instance = it }
            }
        }

        /**
         * 重置单例（用于测试）
         */
        fun resetInstance() {
            instance = null
        }
    }
}

/**
 * 时间变更信息
 */
data class TimeChangeInfo(
    val oldTime: GameTime,
    val newTime: GameTime,
    val hoursAdvanced: Int,
    val dayChanged: Boolean,
    val monthChanged: Boolean,
    val yearChanged: Boolean,
    val seasonChanged: Boolean,
    val oldSeason: Season,
    val newSeason: Season
) {
    fun toDisplayString(): String {
        return buildString {
            append("时间推进: ${oldTime.toDisplayString()} → ${newTime.toDisplayString()}")
            if (dayChanged) append(" [日期变更]")
            if (monthChanged) append(" [月份变更]")
            if (yearChanged) append(" [年份变更]")
            if (seasonChanged) append(" [季节变更: ${oldSeason.displayName} → ${newSeason.displayName}]")
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

/**
 * 时间显示字符串扩展
 */
private fun GameTime.toDisplayString(): String {
    return "${year}年${month}月${day}日 ${hour}时"
}
