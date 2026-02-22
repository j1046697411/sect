package cn.jzl.sect.core.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class Personality6Test {

    @Test
    fun `创建默认性格所有维度为0`() {
        val personality = Personality6()

        assertEquals(0.0, personality.ambition, 0.001)
        assertEquals(0.0, personality.diligence, 0.001)
        assertEquals(0.0, personality.loyalty, 0.001)
        assertEquals(0.0, personality.greed, 0.001)
        assertEquals(0.0, personality.kindness, 0.001)
        assertEquals(0.0, personality.aloofness, 0.001)
    }

    @Test
    fun `创建特定值性格`() {
        val personality = Personality6(
            ambition = 0.5,
            diligence = -0.3,
            loyalty = 0.8,
            greed = -0.5,
            kindness = 0.2,
            aloofness = -0.1
        )

        assertEquals(0.5, personality.ambition, 0.001)
        assertEquals(-0.3, personality.diligence, 0.001)
        assertEquals(0.8, personality.loyalty, 0.001)
        assertEquals(-0.5, personality.greed, 0.001)
        assertEquals(0.2, personality.kindness, 0.001)
        assertEquals(-0.1, personality.aloofness, 0.001)
    }

    @Test
    fun `性格值超出范围抛出异常`() {
        assertFailsWith<IllegalArgumentException> {
            Personality6(ambition = 1.5)
        }

        assertFailsWith<IllegalArgumentException> {
            Personality6(diligence = -1.5)
        }
    }

    @Test
    fun `随机生成性格在范围内`() {
        repeat(10) {
            val personality = Personality6.random()

            assertTrue(personality.ambition in -0.5..0.5)
            assertTrue(personality.diligence in -0.5..0.5)
            assertTrue(personality.loyalty in -0.5..0.5)
            assertTrue(personality.greed in -0.5..0.5)
            assertTrue(personality.kindness in -0.5..0.5)
            assertTrue(personality.aloofness in -0.5..0.5)
        }
    }

    @Test
    fun `高勤勉性格勤勉值较高`() {
        repeat(10) {
            val personality = Personality6.diligent()
            assertTrue(personality.diligence >= 0.5)
        }
    }

    @Test
    fun `高野心性格野心值较高`() {
        repeat(10) {
            val personality = Personality6.ambitious()
            assertTrue(personality.ambition >= 0.5)
        }
    }

    @Test
    fun `高忠诚性格忠诚值较高`() {
        repeat(10) {
            val personality = Personality6.loyal()
            assertTrue(personality.loyalty >= 0.5)
        }
    }

    @Test
    fun `获取主要性格特征`() {
        val ambitious = Personality6(ambition = 0.8)
        assertEquals("野心勃勃", ambitious.getPrimaryTrait())

        val diligent = Personality6(diligence = 0.8)
        assertEquals("勤勉刻苦", diligent.getPrimaryTrait())

        val loyal = Personality6(loyalty = 0.8)
        assertEquals("忠诚可靠", loyal.getPrimaryTrait())

        val greedy = Personality6(greed = 0.8)
        assertEquals("贪婪自私", greedy.getPrimaryTrait())

        val kind = Personality6(kindness = 0.8)
        assertEquals("和善友善", kind.getPrimaryTrait())

        val aloof = Personality6(aloofness = 0.8)
        assertEquals("冷漠孤僻", aloof.getPrimaryTrait())
    }

    @Test
    fun `性格平和当所有值相等`() {
        val neutral = Personality6()
        assertEquals("性格平和", neutral.getPrimaryTrait())
    }

    @Test
    fun `显示字符串包含所有维度`() {
        val personality = Personality6(
            ambition = 0.5,
            diligence = -0.3,
            loyalty = 0.8
        )

        val display = personality.toDisplayString()

        assertTrue(display.contains("野心"))
        assertTrue(display.contains("勤勉"))
        assertTrue(display.contains("忠诚"))
        assertTrue(display.contains("贪婪"))
        assertTrue(display.contains("和善"))
        assertTrue(display.contains("冷漠"))
    }
}
