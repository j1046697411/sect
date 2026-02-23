package cn.jzl.sect.common.time

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.world
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

/**
 * 时间服务测试
 */
class TimeServiceTest : EntityRelationContext {
    override lateinit var world: World

    @BeforeTest
    fun setup() {
        world = world {
            install(timeAddon)
        }
    }

    @Test
    fun testTimeAddonInstallation() {
        val timeService by world.di.instance<TimeService>()
        assertNotNull(timeService, "时间服务应该被正确安装")
    }

    @Test
    fun testInitialGameTime() {
        val timeService by world.di.instance<TimeService>()
        val currentTime = timeService.getCurrentGameTime()
        
        assertEquals(0.seconds, currentTime, "初始游戏时间应该是0")
    }

    @Test
    fun testUpdateGameTime() {
        val timeService by world.di.instance<TimeService>()
        
        timeService.update(1.seconds)
        
        val currentTime = timeService.getCurrentGameTime()
        assertEquals(1.seconds, currentTime, "更新后游戏时间应该是1秒")
    }

    @Test
    fun testMultipleUpdates() {
        val timeService by world.di.instance<TimeService>()
        
        timeService.update(1.seconds)
        timeService.update(2.seconds)
        timeService.update(3.seconds)
        
        val currentTime = timeService.getCurrentGameTime()
        assertEquals(6.seconds, currentTime, "多次更新后游戏时间应该是6秒")
    }

    @Test
    fun testZeroDeltaUpdate() {
        val timeService by world.di.instance<TimeService>()
        
        timeService.update(0.seconds)
        
        val currentTime = timeService.getCurrentGameTime()
        assertEquals(0.seconds, currentTime, "零增量更新后游戏时间应该保持为0")
    }

    @Test
    fun testNegativeDeltaUpdate() {
        val timeService by world.di.instance<TimeService>()
        
        timeService.update(10.seconds)
        timeService.update(-5.seconds)
        
        val currentTime = timeService.getCurrentGameTime()
        assertEquals(5.seconds, currentTime, "负增量更新应该正常累加")
    }

    @Test
    fun testTimerEntityCreation() {
        val timers = world.query { TimerContext(this) }
        val timer = timers.map { it.timer }.firstOrNull()
        
        assertNotNull(timer, "应该创建计时器实体")
        assertEquals(0.seconds, timer.duration, "计时器初始时长应该是0")
    }

    private class TimerContext(world: World) : EntityRelationContext by EntityRelationContext(world) {
        val timer: Timer by component()
    }
}
