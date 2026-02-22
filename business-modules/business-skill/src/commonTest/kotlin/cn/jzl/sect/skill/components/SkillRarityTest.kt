package cn.jzl.sect.skill.components

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 功法品级枚举测试类
 */
class SkillRarityTest {

    @Test
    fun `功法品级应包含7个品级`() {
        // Given & When
        val rarities = SkillRarity.entries

        // Then
        assertEquals(7, rarities.size)
    }

    @Test
    fun `COMMON应表示凡品一阶`() {
        // Given & When & Then
        assertEquals(1, SkillRarity.COMMON.level)
        assertEquals("凡品", SkillRarity.COMMON.displayName)
    }

    @Test
    fun `UNCOMMON应表示灵品二阶`() {
        // Given & When & Then
        assertEquals(2, SkillRarity.UNCOMMON.level)
        assertEquals("灵品", SkillRarity.UNCOMMON.displayName)
    }

    @Test
    fun `RARE应表示玄品三阶`() {
        // Given & When & Then
        assertEquals(3, SkillRarity.RARE.level)
        assertEquals("玄品", SkillRarity.RARE.displayName)
    }

    @Test
    fun `EPIC应表示地品四阶`() {
        // Given & When & Then
        assertEquals(4, SkillRarity.EPIC.level)
        assertEquals("地品", SkillRarity.EPIC.displayName)
    }

    @Test
    fun `LEGENDARY应表示天品五阶`() {
        // Given & When & Then
        assertEquals(5, SkillRarity.LEGENDARY.level)
        assertEquals("天品", SkillRarity.LEGENDARY.displayName)
    }

    @Test
    fun `MYTHIC应表示仙品六阶`() {
        // Given & When & Then
        assertEquals(6, SkillRarity.MYTHIC.level)
        assertEquals("仙品", SkillRarity.MYTHIC.displayName)
    }

    @Test
    fun `DIVINE应表示神品七阶`() {
        // Given & When & Then
        assertEquals(7, SkillRarity.DIVINE.level)
        assertEquals("神品", SkillRarity.DIVINE.displayName)
    }

    @Test
    fun `getLearningDifficulty应返回正确的学习难度`() {
        // Given & When & Then
        assertEquals(10, SkillRarity.COMMON.getLearningDifficulty())
        assertEquals(20, SkillRarity.UNCOMMON.getLearningDifficulty())
        assertEquals(35, SkillRarity.RARE.getLearningDifficulty())
        assertEquals(55, SkillRarity.EPIC.getLearningDifficulty())
        assertEquals(80, SkillRarity.LEGENDARY.getLearningDifficulty())
        assertEquals(110, SkillRarity.MYTHIC.getLearningDifficulty())
        assertEquals(150, SkillRarity.DIVINE.getLearningDifficulty())
    }
}
