package cn.jzl.sect.engine.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.engine.SectWorld
import kotlin.test.*

/**
 * 时间系统测试
 */
class TimeSystemTest : EntityRelationContext {
    override lateinit var world: World

    @BeforeTest
    fun setup() {
        world = SectWorld.create("Test Sect")
    }

    @Test
    fun testSystemInitialization() {
        val system = TimeSystem(world)
        assertNotNull(system)
    }

    @Test
    fun testGetCurrentTime() {
        val system = TimeSystem(world)
        val time = system.getCurrentTime()

        assertNotNull(time, "应该能获取当前时间")
        assertEquals(1, time.year, "初始年份应该是1")
        assertEquals(1, time.month, "初始月份应该是1")
        assertEquals(1, time.day, "初始日期应该是1")
        assertEquals(0, time.hour, "初始小时应该是0")
    }

    @Test
    fun testAdvanceTime() {
        val system = TimeSystem(world)
        val initialTime = system.getCurrentTime()!!

        // 推进24小时
        val timeInfo = system.advance(24)

        assertEquals(24, timeInfo.hoursPassed, "应该推进24小时")
        assertEquals(initialTime.toDisplayString(), timeInfo.oldTime.toDisplayString(), "旧时间应该正确")
        assertEquals("1年1月2日 0时", timeInfo.newTime.toDisplayString(), "新时间应该增加1天")
    }

    @Test
    fun testAdvanceMultipleDays() {
        val system = TimeSystem(world)

        // 推进72小时（3天）
        val timeInfo = system.advance(72)

        assertEquals("1年1月4日 0时", timeInfo.newTime.toDisplayString(), "应该增加3天")
    }

    @Test
    fun testMonthTransition() {
        val system = TimeSystem(world)

        // 推进30天（720小时）
        val timeInfo = system.advance(720)

        assertEquals("1年2月1日 0时", timeInfo.newTime.toDisplayString(), "应该进入第2个月")
    }

    @Test
    fun testYearTransition() {
        val system = TimeSystem(world)

        // 推进360天（8640小时）
        val timeInfo = system.advance(8640)

        assertEquals("2年1月1日 0时", timeInfo.newTime.toDisplayString(), "应该进入第2年")
    }

    @Test
    fun testSeasonChange() {
        val system = TimeSystem(world)

        // 推进90天（进入春季到夏季）
        val timeInfo = system.advance(90 * 24)

        assertTrue(timeInfo.seasonChanged, "季节应该发生变化")
        assertNotNull(timeInfo.newSeason, "新季节不应该为空")
    }

    @Test
    fun testTimeChangeInfoDisplay() {
        val system = TimeSystem(world)
        val timeInfo = system.advance(24)

        val displayString = timeInfo.toDisplayString()
        assertTrue(displayString.contains("时间推进"), "应该包含时间推进信息")
        assertTrue(displayString.contains("24小时"), "应该包含经过的小时数")
    }
}
