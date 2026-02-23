package cn.jzl.sect.common.countdown

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.observer.observe
import cn.jzl.ecs.world
import cn.jzl.sect.common.time.timeAddon
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

/**
 * 倒计时服务测试
 */
class CountdownServiceTest : EntityRelationContext {
    override lateinit var world: World

    @BeforeTest
    fun setup() {
        world = world {
            install(timeAddon)
            install(countdownAddon)
        }
    }

    @Test
    fun testCountdownServiceInstallation() {
        val countdownService by world.di.instance<CountdownService>()
        assertNotNull(countdownService, "倒计时服务应该被正确安装")
    }

    @Test
    fun testSetCountdown() {
        val countdownService by world.di.instance<CountdownService>()
        val entity = world.entity()
        
        countdownService.countdown(entity, 5.seconds)
        
        val hasCountdown = world.query { CountdownContext(this) }
            .filter { it.entity == entity }
            .firstOrNull() != null
        
        assertTrue(hasCountdown, "实体应该有倒计时组件")
    }

    @Test
    fun testCountdownNotTriggeredBeforeTime() {
        val countdownService by world.di.instance<CountdownService>()
        val timeService by world.di.instance<TimeService>()
        val entity = world.entity()
        
        var triggered = false
        entity.observe<OnCountdownComplete>().exec {
            triggered = true
        }
        
        countdownService.countdown(entity, 5.seconds)
        
        timeService.update(3.seconds)
        countdownService.update(0.seconds)
        
        assertFalse(triggered, "倒计时未到时间不应该触发")
    }

    @Test
    fun testCountdownTriggeredOnTime() {
        val countdownService by world.di.instance<CountdownService>()
        val timeService by world.di.instance<TimeService>()
        val entity = world.entity()
        
        var triggered = false
        entity.observe<OnCountdownComplete>().exec {
            triggered = true
        }
        
        countdownService.countdown(entity, 5.seconds)
        
        timeService.update(5.seconds)
        countdownService.update(0.seconds)
        
        assertTrue(triggered, "倒计时到时间应该触发")
    }

    @Test
    fun testCountdownTriggeredAfterTime() {
        val countdownService by world.di.instance<CountdownService>()
        val timeService by world.di.instance<TimeService>()
        val entity = world.entity()
        
        var triggered = false
        entity.observe<OnCountdownComplete>().exec {
            triggered = true
        }
        
        countdownService.countdown(entity, 5.seconds)
        
        timeService.update(10.seconds)
        countdownService.update(0.seconds)
        
        assertTrue(triggered, "倒计时超过时间应该触发")
    }

    @Test
    fun testCountdownRemovedAfterTrigger() {
        val countdownService by world.di.instance<CountdownService>()
        val timeService by world.di.instance<TimeService>()
        val entity = world.entity()
        
        countdownService.countdown(entity, 5.seconds)
        
        timeService.update(5.seconds)
        countdownService.update(0.seconds)
        
        val hasCountdown = world.query { CountdownContext(this) }
            .filter { it.entity == entity }
            .firstOrNull() != null
        
        assertFalse(hasCountdown, "倒计时触发后应该被移除")
    }

    @Test
    fun testMultipleEntitiesWithDifferentCountdowns() {
        val countdownService by world.di.instance<CountdownService>()
        val timeService by world.di.instance<TimeService>()
        
        val entity1 = world.entity()
        val entity2 = world.entity()
        
        var triggered1 = false
        var triggered2 = false
        
        entity1.observe<OnCountdownComplete>().exec { triggered1 = true }
        entity2.observe<OnCountdownComplete>().exec { triggered2 = true }
        
        countdownService.countdown(entity1, 3.seconds)
        countdownService.countdown(entity2, 7.seconds)
        
        timeService.update(3.seconds)
        countdownService.update(0.seconds)
        
        assertTrue(triggered1, "第一个倒计时应该触发")
        assertFalse(triggered2, "第二个倒计时未到时间不应该触发")
        
        timeService.update(4.seconds)
        countdownService.update(0.seconds)
        
        assertTrue(triggered2, "第二个倒计时现在应该触发")
    }

    @Test
    fun testZeroIntervalCountdown() {
        val countdownService by world.di.instance<CountdownService>()
        val timeService by world.di.instance<TimeService>()
        val entity = world.entity()
        
        var triggered = false
        entity.observe<OnCountdownComplete>().exec {
            triggered = true
        }
        
        countdownService.countdown(entity, 0.seconds)
        
        countdownService.update(0.seconds)
        
        assertTrue(triggered, "零间隔倒计时应该立即触发")
    }

    private class CountdownContext(world: World) : EntityRelationContext by EntityRelationContext(world) {
        val entity: cn.jzl.ecs.entity.Entity by component()
    }
}
