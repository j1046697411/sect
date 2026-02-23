package cn.jzl.sect.skill.services

import cn.jzl.sect.skill.components.SkillEffect
import cn.jzl.sect.skill.components.SkillEffectType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 功法效果服务测试类
 */
class SkillEffectServiceTest {

    @Test
    fun `应用属性加成效果应返回正确的加成值`() {
        // Given
        val service = SkillEffectService()
        val effect = SkillEffect(
            type = SkillEffectType.ATTRIBUTE_BONUS,
            targetAttribute = "attack",
            baseValue = 10.0
        )

        // When
        val bonus = service.applyEffect(effect, proficiency = 50)

        // Then - 基础值 * 熟练度倍率(0.75)
        assertEquals(7.5, bonus, 0.01)
    }

    @Test
    fun `应用效率加成效果应返回正确的加成值`() {
        // Given
        val service = SkillEffectService()
        val effect = SkillEffect(
            type = SkillEffectType.EFFICIENCY_BONUS,
            targetAttribute = "cultivation",
            baseValue = 20.0
        )

        // When
        val bonus = service.applyEffect(effect, proficiency = 100)

        // Then - 基础值 * 熟练度倍率(1.0)
        assertEquals(20.0, bonus, 0.01)
    }

    @Test
    fun `计算总属性加成应累加所有效果`() {
        // Given
        val service = SkillEffectService()
        val effects = listOf(
            SkillEffect(type = SkillEffectType.ATTRIBUTE_BONUS, targetAttribute = "attack", baseValue = 10.0),
            SkillEffect(type = SkillEffectType.ATTRIBUTE_BONUS, targetAttribute = "attack", baseValue = 5.0)
        )

        // When
        val totalBonus = service.calculateTotalAttributeBonus(effects, "attack", proficiency = 100)

        // Then
        assertEquals(15.0, totalBonus, 0.01)
    }

    @Test
    fun `计算修炼效率加成应返回正确值`() {
        // Given
        val service = SkillEffectService()
        val effects = listOf(
            SkillEffect(type = SkillEffectType.EFFICIENCY_BONUS, targetAttribute = "cultivation", baseValue = 20.0),
            SkillEffect(type = SkillEffectType.EFFICIENCY_BONUS, targetAttribute = "cultivation", baseValue = 10.0)
        )

        // When
        val totalBonus = service.calculateTotalEfficiencyBonus(effects, "cultivation", proficiency = 100)

        // Then
        assertEquals(30.0, totalBonus, 0.01)
    }

    @Test
    fun `获取被动技能效果应返回正确的效果列表`() {
        // Given
        val service = SkillEffectService()
        val effects = listOf(
            SkillEffect(type = SkillEffectType.PASSIVE_SKILL, targetAttribute = "regeneration", baseValue = 5.0),
            SkillEffect(type = SkillEffectType.ATTRIBUTE_BONUS, targetAttribute = "attack", baseValue = 10.0)
        )

        // When
        val passiveEffects = service.getPassiveSkillEffects(effects)

        // Then
        assertEquals(1, passiveEffects.size)
        assertEquals("regeneration", passiveEffects[0].targetAttribute)
    }
}
