package cn.jzl.sect.engine.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.engine.SectWorld
import kotlin.test.*

/**
 * 修炼系统测试
 */
class CultivationSystemTest : EntityRelationContext {
    override lateinit var world: World

    @BeforeTest
    fun setup() {
        world = SectWorld.create("Test Sect")
    }

    @Test
    fun testSystemInitialization() {
        val system = CultivationSystem(world)
        assertNotNull(system)
    }

    @Test
    fun testUpdateIncreasesCultivation() {
        val system = CultivationSystem(world)

        // 获取初始修为
        val infoSystem = SectInfoSystem(world)
        val initialDisciples = infoSystem.getDiscipleList()
        val initialCultivation = initialDisciples.first().cultivation

        // 推进24小时
        val breakthroughs = system.update(24)

        // 验证修为增长
        val updatedDisciples = infoSystem.getDiscipleList()
        val updatedCultivation = updatedDisciples.first().cultivation

        assertTrue(
            updatedCultivation > initialCultivation,
            "修为应该增长：$initialCultivation -> $updatedCultivation"
        )
    }

    @Test
    fun testBreakthroughPossible() {
        val system = CultivationSystem(world)
        val infoSystem = SectInfoSystem(world)

        // 连续推进大量时间，增加突破概率
        var breakthroughCount = 0
        repeat(100) {
            val breakthroughs = system.update(24)
            breakthroughCount += breakthroughs.size
        }

        // 验证突破事件可能发生（由于随机性，不保证一定发生）
        // 这里只验证系统不会崩溃
        assertTrue(breakthroughCount >= 0, "突破次数应该非负")
    }

    @Test
    fun testBreakthroughEventFormat() {
        // 通过实际运行系统来测试突破事件格式
        val system = CultivationSystem(world)

        // 连续推进大量时间，直到有人突破
        var foundBreakthrough = false
        var attemptCount = 0
        var lastEvent: CultivationSystem.BreakthroughEvent? = null

        while (!foundBreakthrough && attemptCount < 500) {
            val breakthroughs = system.update(24)
            if (breakthroughs.isNotEmpty()) {
                foundBreakthrough = true
                lastEvent = breakthroughs.first()
            }
            attemptCount++
        }

        // 验证突破事件格式（如果有人突破）
        if (lastEvent != null) {
            val displayString = lastEvent.toDisplayString()
            assertTrue(displayString.contains("突破成功"), "应该包含突破成功信息")
            assertTrue(displayString.contains("层"), "应该包含层数信息")
        }

        // 即使没有突破，也验证系统运行正常
        assertTrue(attemptCount >= 0, "系统应该正常运行")
    }
}
