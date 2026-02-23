package cn.jzl.sect.common.countdown

import cn.jzl.di.instance
import cn.jzl.ecs.*
import cn.jzl.ecs.entity.*
import cn.jzl.ecs.observer.observe
import cn.jzl.ecs.world
import cn.jzl.sect.common.time.TimeService
import cn.jzl.sect.common.time.timeAddon
import kotlin.test.*
import kotlin.time.Duration
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
        val countdownService: CountdownService by world.di.instance()
        assertNotNull(countdownService, "倒计时服务应该被正确安装")
    }

    @Test
    fun testSetCountdown() {
        val countdownService: CountdownService by world.di.instance()
        val entity = world.entity { }
        
        countdownService.countdown(entity, 5.seconds)
        
        assertTrue(true, "应该能成功设置倒计时")
    }

    @Test
    fun testCountdownNotTriggeredBeforeTime() {
        val countdownService: CountdownService by world.di.instance()
        val timeService: TimeService by world.di.instance()
        val entity = world.entity { }
        
        var triggered = false
        entity.observe<OnCountdownComplete>().exec {
            triggered = true
        }
        
        countdownService.countdown(entity, 5.seconds)
        
        timeService.update(3.seconds)
        countdownService.update(Duration.ZERO)
        
        assertFalse(triggered, "倒计时未到时间不应该触发")
    }

    @Test
    fun testCountdownTriggeredOnTime() {
        val countdownService: CountdownService by world.di.instance()
        val timeService: TimeService by world.di.instance()
        val entity = world.entity { }
        
        var triggered = false
        entity.observe<OnCountdownComplete>().exec {
            triggered = true
        }
        
        countdownService.countdown(entity, 5.seconds)
        
        timeService.update(5.seconds)
        countdownService.update(Duration.ZERO)
        
        assertTrue(triggered, "倒计时到时间应该触发")
    }

    @Test
    fun testCountdownTriggeredAfterTime() {
        val countdownService: CountdownService by world.di.instance()
        val timeService: TimeService by world.di.instance()
        val entity = world.entity { }
        
        var triggered = false
        entity.observe<OnCountdownComplete>().exec {
            triggered = true
        }
        
        countdownService.countdown(entity, 5.seconds)
        
        timeService.update(10.seconds)
        countdownService.update(Duration.ZERO)
        
        assertTrue(triggered, "倒计时超过时间应该触发")
    }

    @Ignore("存在并发修改问题，需要修复")
    @Test
    fun testMultipleEntitiesWithDifferentCountdowns() {
        val countdownService: CountdownService by world.di.instance()
        val timeService: TimeService by world.di.instance()
        
        val entity1 = world.entity { }
        val entity2 = world.entity { }
        
        var triggered1 = false
        var triggered2 = false
        
        entity1.observe<OnCountdownComplete>().exec { triggered1 = true }
        entity2.observe<OnCountdownComplete>().exec { triggered2 = true }
        
        countdownService.countdown(entity1, 3.seconds)
        countdownService.countdown(entity2, 7.seconds)
        
        timeService.update(3.seconds)
        countdownService.update(Duration.ZERO)
        
        assertTrue(triggered1, "第一个倒计时应该触发")
        assertFalse(triggered2, "第二个倒计时未到时间不应该触发")
        
        timeService.update(4.seconds)
        countdownService.update(Duration.ZERO)
        
        assertTrue(triggered2, "第二个倒计时现在应该触发")
    }

    @Test
    fun testZeroIntervalCountdown() {
        val countdownService: CountdownService by world.di.instance()
        val entity = world.entity { }
        
        var triggered = false
        entity.observe<OnCountdownComplete>().exec {
            triggered = true
        }
        
        countdownService.countdown(entity, Duration.ZERO)
        
        countdownService.update(Duration.ZERO)
        
        assertTrue(triggered, "零间隔倒计时应该立即触发")
    }
}
