package cn.jzl.sect.skill.components

import cn.jzl.sect.core.cultivation.Realm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * 功法组件测试类
 */
class SkillTest {

    @Test
    fun `默认构造函数应创建具有默认值的功法`() {
        // Given & When
        val skill = Skill()

        // Then
        assertEquals(0L, skill.id)
        assertEquals("", skill.name)
        assertEquals("", skill.description)
        assertEquals(SkillType.CULTIVATION, skill.type)
        assertEquals(SkillRarity.COMMON, skill.rarity)
        assertEquals(Realm.MORTAL, skill.requiredRealm)
        assertEquals(0, skill.requiredComprehension)
        assertEquals(emptyList<Long>(), skill.prerequisiteSkillIds)
    }

    @Test
    fun `自定义构造函数应创建具有指定值的功法`() {
        // Given & When
        val skill = Skill(
            id = 1L,
            name = "基础吐纳术",
            description = "最基础的修炼功法",
            type = SkillType.CULTIVATION,
            rarity = SkillRarity.COMMON,
            requiredRealm = Realm.QI_REFINING,
            requiredComprehension = 30,
            prerequisiteSkillIds = listOf()
        )

        // Then
        assertEquals(1L, skill.id)
        assertEquals("基础吐纳术", skill.name)
        assertEquals("最基础的修炼功法", skill.description)
        assertEquals(SkillType.CULTIVATION, skill.type)
        assertEquals(SkillRarity.COMMON, skill.rarity)
        assertEquals(Realm.QI_REFINING, skill.requiredRealm)
        assertEquals(30, skill.requiredComprehension)
    }

    @Test
    fun `getLearningDifficulty应返回功法品级的学习难度`() {
        // Given
        val commonSkill = Skill(rarity = SkillRarity.COMMON)
        val legendarySkill = Skill(rarity = SkillRarity.LEGENDARY)

        // When & Then
        assertEquals(10, commonSkill.getLearningDifficulty())
        assertEquals(80, legendarySkill.getLearningDifficulty())
    }

    @Test
    fun `hasPrerequisites应返回true当功法有前置功法时`() {
        // Given
        val skillWithPrereq = Skill(prerequisiteSkillIds = listOf(1L, 2L))
        val skillWithoutPrereq = Skill(prerequisiteSkillIds = emptyList())

        // When & Then
        assertTrue(skillWithPrereq.hasPrerequisites())
        assertFalse(skillWithoutPrereq.hasPrerequisites())
    }

    @Test
    fun `getDisplayName应返回完整的功法名称`() {
        // Given
        val skill = Skill(
            name = "基础吐纳术",
            rarity = SkillRarity.RARE
        )

        // When & Then
        assertEquals("【玄品】基础吐纳术", skill.getDisplayName())
    }
}
