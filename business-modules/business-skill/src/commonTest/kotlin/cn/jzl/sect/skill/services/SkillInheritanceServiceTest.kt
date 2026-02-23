package cn.jzl.sect.skill.services

import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.skill.components.Skill
import cn.jzl.sect.skill.components.SkillLearned
import cn.jzl.sect.skill.components.SkillRarity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 功法传承服务测试类
 */
class SkillInheritanceServiceTest {

    @Test
    fun `满足传承条件时应可以传承`() {
        // Given
        val service = SkillInheritanceService()
        val skill = Skill(
            id = 1L,
            name = "基础吐纳术",
            rarity = SkillRarity.COMMON,
            requiredRealm = Realm.QI_REFINING
        )
        val learned = SkillLearned(skillId = 1L, proficiency = 60)

        // When
        val result = service.canInherit(
            skill = skill,
            learned = learned,
            masterRealm = Realm.FOUNDATION,
            apprenticeRealm = Realm.QI_REFINING
        )

        // Then
        assertTrue(result)
    }

    @Test
    fun `熟练度不足时应无法传承`() {
        // Given
        val service = SkillInheritanceService()
        val skill = Skill(id = 1L, rarity = SkillRarity.COMMON)
        val learned = SkillLearned(skillId = 1L, proficiency = 40) // 低于50

        // When
        val result = service.canInherit(
            skill = skill,
            learned = learned,
            masterRealm = Realm.FOUNDATION,
            apprenticeRealm = Realm.QI_REFINING
        )

        // Then
        assertFalse(result)
    }

    @Test
    fun `师父境界不足时应无法传承高品级功法`() {
        // Given
        val service = SkillInheritanceService()
        // 天品功法需要金丹期以上才能传承
        val skill = Skill(id = 1L, rarity = SkillRarity.LEGENDARY, requiredRealm = Realm.GOLDEN_CORE)
        val learned = SkillLearned(skillId = 1L, proficiency = 80)

        // When - 师父只有筑基期
        val result = service.canInherit(
            skill = skill,
            learned = learned,
            masterRealm = Realm.FOUNDATION,
            apprenticeRealm = Realm.QI_REFINING
        )

        // Then
        assertFalse(result)
    }

    @Test
    fun `徒弟境界差距过大时应无法传承`() {
        // Given
        val service = SkillInheritanceService()
        val skill = Skill(id = 1L, rarity = SkillRarity.RARE, requiredRealm = Realm.FOUNDATION)
        val learned = SkillLearned(skillId = 1L, proficiency = 70)

        // When - 徒弟只有凡人境界，差距超过2级
        val result = service.canInherit(
            skill = skill,
            learned = learned,
            masterRealm = Realm.NASCENT_SOUL,
            apprenticeRealm = Realm.MORTAL
        )

        // Then
        assertFalse(result)
    }

    @Test
    fun `传承功法应返回新的已学习功法对象`() {
        // Given
        val service = SkillInheritanceService()
        val skill = Skill(id = 1L, name = "基础吐纳术", rarity = SkillRarity.COMMON)
        val learned = SkillLearned(skillId = 1L, proficiency = 60)

        // When
        val inherited = service.inheritSkill(skill)

        // Then
        assertNotNull(inherited)
        assertEquals(1L, inherited.skillId)
        assertEquals(0, inherited.proficiency) // 传承后熟练度从0开始
    }

    @Test
    fun `计算师父获得的声望应根据功法品级返回正确值`() {
        // Given
        val service = SkillInheritanceService()

        // When & Then
        val commonReputation = service.calculateMasterReputation(SkillRarity.COMMON)
        val legendaryReputation = service.calculateMasterReputation(SkillRarity.LEGENDARY)

        assertEquals(10, commonReputation)
        assertTrue(legendaryReputation > commonReputation)
    }

    @Test
    fun `计算传承成功率应根据熟练度返回正确值`() {
        // Given
        val service = SkillInheritanceService()

        // When
        val lowProficiency = service.calculateInheritanceSuccessRate(50)
        val highProficiency = service.calculateInheritanceSuccessRate(100)

        // Then
        assertTrue(highProficiency > lowProficiency)
    }
}
