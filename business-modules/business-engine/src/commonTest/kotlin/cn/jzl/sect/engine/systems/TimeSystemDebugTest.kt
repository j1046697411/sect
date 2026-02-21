package cn.jzl.sect.engine.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.engine.SectWorld
import cn.jzl.sect.engine.state.GameState
import kotlin.test.*

/**
 * 时间系统调试测试
 */
class TimeSystemDebugTest : EntityRelationContext {
    override lateinit var world: World

    @BeforeTest
    fun setup() {
        GameState.resetInstance()
        world = SectWorld.create("Test Sect")
    }

    @Test
    fun testSeasonChangeDebug() {
        val system = TimeSystem(world)
        
        println("Initial time: ${system.getCurrentTime()}")
        println("Initial season: ${system.getCurrentSeason()}")
        
        // 推进90天
        val timeInfo = system.advance(90 * 24)
        
        println("After advance:")
        println("Old time: ${timeInfo.oldTime}")
        println("New time: ${timeInfo.newTime}")
        println("Hours passed: ${timeInfo.hoursPassed}")
        println("New season: ${timeInfo.newSeason}")
        println("Season changed: ${timeInfo.seasonChanged}")
        
        assertTrue(timeInfo.seasonChanged, "季节应该发生变化")
    }
}
