# 时间服务 (Time Service)

本模块管理游戏时间流逝和日程系统。

## 核心概念

- **游戏时间**：1游戏年 = 现实6分钟（360秒）
- **可调节速度**：支持 0.5x、1x、2x、5x、10x 加速
- **暂停功能**：可随时暂停游戏
- **时间比例**：1游戏年 = 12游戏月 = 360游戏天 = 8640游戏小时

## 时间计算

```kotlin
object TimeConstants {
    const val REAL_SECONDS_PER_GAME_YEAR = 360        // 6分钟
    const val GAME_DAYS_PER_YEAR = 360                // 1年360天
    const val GAME_HOURS_PER_DAY = 24                 // 每天24小时
    
    // 游戏分钟/现实秒 = (360 * 24 * 60) / 360 = 1440
    const val GAME_MINUTES_PER_REAL_SECOND = 1440
}
```

## 组件定义

### GameTime

```kotlin
data class GameTime(
    val year: Int,
    val month: Int,              // 1-12
    val day: Int,                // 1-30
    val hour: Int,               // 0-23
    val minute: Int,             // 0-59
    val totalMinutes: Long,      // 累计游戏分钟（用于计算）
    val speedMultiplier: Float = 1.0f,
    val isPaused: Boolean = false
) {
    fun toDisplayString(): String {
        return "修真纪元${year}年 ${month}月${day}日 ${hour}时"
    }
    
    fun getPeriod(): DayPeriod {
        return DayPeriod.fromHour(hour)
    }
}

enum class DayPeriod(val startHour: Int, val displayName: String) {
    ZI(0, "子时"),      // 23-1  深夜
    CHOU(1, "丑时"),    // 1-3   
    YIN(3, "寅时"),     // 3-5   
    MAO(5, "卯时"),     // 5-7   清晨
    CHEN(7, "辰时"),    // 7-9   早晨 ★重要
    SI(9, "巳时"),      // 9-11  上午 ★修炼
    WU(11, "午时"),     // 11-13 中午 ★休息
    WEI(13, "未时"),    // 13-15 下午 ★修炼
    SHEN(15, "申时"),   // 15-17 下午
    YOU(17, "酉时"),    // 17-19 傍晚 ★任务
    XU(19, "戌时"),     // 19-21 晚上
    HAI(21, "亥时");    // 21-23 深夜 ★休息
    
    companion object {
        fun fromHour(hour: Int): DayPeriod {
            return when (hour) {
                23, 0 -> ZI
                1, 2 -> CHOU
                3, 4 -> YIN
                5, 6 -> MAO
                7, 8 -> CHEN
                9, 10 -> SI
                11, 12 -> WU
                13, 14 -> WEI
                15, 16 -> SHEN
                17, 18 -> YOU
                19, 20 -> XU
                21, 22 -> HAI
                else -> ZI
            }
        }
    }
}
```

## TimeService

```kotlin
class TimeService(override val world: World) : EntityRelationContext, System {
    private var timeAccumulator = 0f
    
    override fun update(deltaTime: Float) {
        val gameTime = world.getSingleton<GameTime>()
        if (gameTime.isPaused) return
        
        // 计算游戏时间流逝
        val gameMinutes = deltaTime * TimeConstants.GAME_MINUTES_PER_REAL_SECOND * gameTime.speedMultiplier
        timeAccumulator += gameMinutes
        
        // 批量处理（每累积1分钟处理一次）
        if (timeAccumulator >= 1.0f) {
            val minutesToProcess = timeAccumulator.toInt()
            timeAccumulator -= minutesToProcess
            advanceTime(gameTime, minutesToProcess)
        }
    }
    
    private fun advanceTime(current: GameTime, minutes: Int) {
        var newTotal = current.totalMinutes + minutes
        var newYear = current.year
        var newMonth = current.month
        var newDay = current.day
        var newHour = current.hour
        var newMinute = current.minute + minutes
        
        // 时间进位计算
        while (newMinute >= 60) {
            newMinute -= 60
            newHour++
        }
        while (newHour >= 24) {
            newHour -= 24
            newDay++
        }
        while (newDay > 30) {
            newDay -= 30
            newMonth++
        }
        while (newMonth > 12) {
            newMonth -= 12
            newYear++
        }
        
        val newTime = current.copy(
            year = newYear,
            month = newMonth,
            day = newDay,
            hour = newHour,
            minute = newMinute,
            totalMinutes = newTotal
        )
        
        world.setSingleton(newTime)
        
        // 触发时间变化事件
        if (newHour != current.hour) {
            world.eventBus.emit(HourChangedEvent(newHour, newTime.getPeriod()))
        }
        if (newDay != current.day) {
            world.eventBus.emit(DayChangedEvent(newYear, newMonth, newDay))
        }
        if (newMonth != current.month) {
            world.eventBus.emit(MonthChangedEvent(newYear, newMonth))
        }
        if (newYear != current.year) {
            world.eventBus.emit(YearChangedEvent(newYear))
        }
    }
    
    /// 设置时间速度
    fun setSpeed(multiplier: Float) {
        val current = world.getSingleton<GameTime>()
        world.setSingleton(current.copy(speedMultiplier = multiplier.coerceIn(0.5f, 10f)))
    }
    
    /// 暂停/恢复
    fun togglePause() {
        val current = world.getSingleton<GameTime>()
        world.setSingleton(current.copy(isPaused = !current.isPaused))
    }
    
    /// 跳转到指定时间（用于测试）
    fun jumpTo(year: Int, month: Int = 1, day: Int = 1, hour: Int = 0) {
        val totalMinutes = calculateTotalMinutes(year, month, day, hour)
        world.setSingleton(GameTime(year, month, day, hour, 0, totalMinutes))
    }
}
```

## 时间事件

```kotlin
data class HourChangedEvent(val hour: Int, val period: DayPeriod)
data class DayChangedEvent(val year: Int, val month: Int, val day: Int)
data class MonthChangedEvent(val year: Int, val month: Int)
data class YearChangedEvent(val year: Int)
```

## 使用场景

### 日程系统响应

```kotlin
class ScheduleSystem(override val world: World) : EntityRelationContext, System {
    private var lastHour = -1
    
    init {
        // 订阅小时变化事件
        world.eventBus.subscribe<HourChangedEvent> { event ->
            onHourChanged(event.period)
        }
    }
    
    private fun onHourChanged(period: DayPeriod) {
        // 根据时辰触发不同行为
        when (period) {
            DayPeriod.CHEN -> morningPractice()    // 辰时：晨练
            DayPeriod.SI -> startCultivation()     // 巳时：开始修炼
            DayPeriod.WU -> restTime()             // 午时：休息
            DayPeriod.WEI -> continueCultivation() // 未时：继续修炼
            DayPeriod.YOU -> assignMissions()      // 酉时：分配任务
            DayPeriod.HAI -> restAndRecover()      // 亥时：休息恢复
            else -> {} // 其他时辰不处理
        }
    }
    
    private fun startCultivation() {
        // 切换所有空闲弟子为修炼状态
        world.query {
            entityFilter {
                hasTag<IdleTag>()
                hasTag<AliveTag>()
                !hasTag<InjuredTag>()
            }
        }.forEach { ctx ->
            changeBehaviorState(ctx.entity, CultivatingTag::class)
        }
    }
}
```

### 俸禄发放（每月1日）

```kotlin
class SalarySystem(override val world: World) : EntityRelationContext, System {
    private var lastMonth = -1
    
    init {
        world.eventBus.subscribe<MonthChangedEvent> {
            if (it.month != lastMonth) {
                lastMonth = it.month
                distributeSalaries()
            }
        }
    }
    
    private fun distributeSalaries() {
        world.query {
            entityFilter {
                hasTag<InSectTag>()
                hasTag<AliveTag>()
                hasComponent<Position>()
            }
        }.forEach { ctx ->
            val position = ctx.getComponent<Position>()
            val salary = calculateSalary(ctx.entity, position)
            giveSalary(ctx.entity, salary)
        }
    }
}
```

## UI 集成

```kotlin
@Composable
fun TimeDisplay(gameTime: GameTime) {
    Row(modifier = Modifier.padding(16.dp)) {
        Text(
            text = gameTime.toDisplayString(),
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 速度控制
        SpeedControlButton(
            currentSpeed = gameTime.speedMultiplier,
            onSpeedChange = { timeService.setSpeed(it) }
        )
        
        // 暂停按钮
        IconButton(onClick = { timeService.togglePause() }) {
            Icon(
                imageVector = if (gameTime.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (gameTime.isPaused) "继续" else "暂停"
            )
        }
    }
}
```

## 依赖关系

- **依赖**：`core` 模块（使用基础组件）
- **被依赖**：几乎所有其他服务模块

## 性能优化

1. **批量处理**：时间累积后批量更新
2. **事件驱动**：通过事件触发而非轮询
3. **可变速度**：支持加速运行，节省时间
