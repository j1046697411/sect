package cn.jzl.sect.skill.services

import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.cultivation.Talent
import cn.jzl.sect.skill.components.Skill
import cn.jzl.sect.skill.components.SkillRarity
import cn.jzl.sect.skill.components.SkillType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 功法学习服务测试类
 */
class SkillLearningServiceTest {

    @Test
    fun `满足学习条件时应成功学习功法`() {
        // Given
        val service = SkillLearningService()
        val skill = Skill(
            id = 1L,
            name = "基础吐纳术",
            type = SkillType.CULTIVATION,
            rarity = SkillRarity.COMMON,
            requiredRealm = Realm.QI_REFINING,
            requiredComprehension = 30
        )
        val talent = Talent(comprehension = 50)

        // When
        val result = service.canLearnSkill(skill, Realm.QI_REFINING, talent, emptyList())

        // Then
        assertTrue(result)
    }

    @Test
    fun `境界不足时应无法学习功法`() {
        // Given
        val service = SkillLearningService()
        val skill = Skill(
            id = 1L,
            name = "基础吐纳术",
            requiredRealm = Realm.QI_REFINING,
            requiredComprehension = 30
        )
        val talent = Talent(comprehension = 50)

        // When
        val result = service.canLearnSkill(skill, Realm.MORTAL, talent, emptyList())

        // Then
        assertFalse(result)
    }

    @Test
    fun `悟性不足时应无法学习功法`() {
        // Given
        val service = SkillLearningService()
        val skill = Skill(
            id = 1L,
            name = "基础吐纳术",
            requiredRealm = Realm.QI_REFINING,
            requiredComprehension = 50
        )
        val talent = Talent(comprehension = 30)

        // When
        val result = service.canLearnSkill(skill, Realm.QI_REFINING, talent, emptyList())

        // Then
        assertFalse(result)
    }

    @Test
    fun `缺少前置功法时应无法学习功法`() {
        // Given
        val service = SkillLearningService()
        val skill = Skill(
            id = 2L,
            name = "高级功法",
            requiredRealm = Realm.QI_REFINING,
            requiredComprehension = 30,
            prerequisiteSkillIds = listOf(1L)
        )
        val talent = Talent(comprehension = 50)

        // When
        val result = service.canLearnSkill(skill, Realm.QI_REFINING, talent, emptyList())

        // Then
        assertFalse(result)
    }

    @Test
    fun `拥有前置功法时应可以学习功法`() {
        // Given
        val service = SkillLearningService()
        val skill = Skill(
            id = 2L,
            name = "高级功法",
            requiredRealm = Realm.QI_REFINING,
            requiredComprehension = 30,
            prerequisiteSkillIds = listOf(1L)
        )
        val talent = Talent(comprehension = 50)
        val learnedSkills = listOf(1L)

        // When
        val result = service.canLearnSkill(skill, Realm.QI_REFINING, talent, learnedSkills)

        // Then
        assertTrue(result)
    }

    @Test
    fun `学习功法应返回已学习功法对象`() {
        // Given
        val service = SkillLearningService()
        val skill = Skill(
            id = 1L,
            name = "基础吐纳术",
            type = SkillType.CULTIVATION,
            rarity = SkillRarity.COMMON
        )

        // When
        val learned = service.learnSkill(skill)

        // Then
        assertNotNull(learned)
        assertEquals(1L, learned.skillId)
        assertEquals(0, learned.proficiency)
    }

    @Test
    fun `计算学习成功率应根据悟性返回正确值`() {
        // Given
        val service = SkillLearningService()
        val skill = Skill(rarity = SkillRarity.COMMON)

        // When & Then
        val lowComprehension = Talent(comprehension = 20)
        val highComprehension = Talent(comprehension = 80)

        val lowSuccess = service.calculateLearningSuccessRate(skill, lowComprehension)
        val highSuccess = service.calculateLearningSuccessRate(skill, highComprehension)

        assertTrue(lowSuccess < highSuccess)
    }
}
