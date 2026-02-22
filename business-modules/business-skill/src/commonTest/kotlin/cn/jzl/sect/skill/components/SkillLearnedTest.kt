package cn.jzl.sect.skill.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * 已学习功法组件测试类
 */
class SkillLearnedTest {

    @Test
    fun `默认构造函数应创建具有默认值的已学习功法`() {
        // Given & When
        val learned = SkillLearned()

        // Then
        assertEquals(0L, learned.skillId)
        assertEquals(0, learned.proficiency)
        assertEquals(0L, learned.learnedTime)
    }

    @Test
    fun `自定义构造函数应创建具有指定值的已学习功法`() {
        // Given & When
        val learned = SkillLearned(
            skillId = 1L,
            proficiency = 50,
            learnedTime = 1000L
        )

        // Then
        assertEquals(1L, learned.skillId)
        assertEquals(50, learned.proficiency)
        assertEquals(1000L, learned.learnedTime)
    }

    @Test
    fun `increaseProficiency应增加熟练度`() {
        // Given
        val learned = SkillLearned(proficiency = 50)

        // When
        val increased = learned.increaseProficiency(20)

        // Then
        assertEquals(70, increased.proficiency)
        assertEquals(50, learned.proficiency) // 原对象不变
    }

    @Test
    fun `increaseProficiency不应超过最大值100`() {
        // Given
        val learned = SkillLearned(proficiency = 90)

        // When
        val increased = learned.increaseProficiency(20)

        // Then
        assertEquals(100, increased.proficiency)
    }

    @Test
    fun `isMastered应返回true当熟练度达到50时`() {
        // Given
        val mastered = SkillLearned(proficiency = 50)
        val notMastered = SkillLearned(proficiency = 49)

        // When & Then
        assertTrue(mastered.isMastered())
        assertFalse(notMastered.isMastered())
    }

    @Test
    fun `isPerfected应返回true当熟练度达到100时`() {
        // Given
        val perfected = SkillLearned(proficiency = 100)
        val notPerfected = SkillLearned(proficiency = 99)

        // When & Then
        assertTrue(perfected.isPerfected())
        assertFalse(notPerfected.isPerfected())
    }

    @Test
    fun `getEffectMultiplier应返回正确的效果倍率`() {
        // Given
        val lowProficiency = SkillLearned(proficiency = 20)
        val mediumProficiency = SkillLearned(proficiency = 50)
        val highProficiency = SkillLearned(proficiency = 100)

        // When & Then - 熟练度/100 * 0.5 + 0.5
        assertEquals(0.6, lowProficiency.getEffectMultiplier(), 0.01) // 20/100 * 0.5 + 0.5
        assertEquals(0.75, mediumProficiency.getEffectMultiplier(), 0.01) // 50/100 * 0.5 + 0.5
        assertEquals(1.0, highProficiency.getEffectMultiplier(), 0.01) // 100/100 * 0.5 + 0.5
    }

    @Test
    fun `canInherit应返回true当熟练度达到50且已掌握时`() {
        // Given
        val canInherit = SkillLearned(proficiency = 50)
        val cannotInherit = SkillLearned(proficiency = 49)

        // When & Then
        assertTrue(canInherit.canInherit())
        assertFalse(cannotInherit.canInherit())
    }
}
