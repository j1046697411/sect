package cn.jzl.sect.combat.components

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 战斗属性组件测试类
 */
class CombatStatsTest {

    @Test
    fun `默认构造函数应创建具有默认值的战斗属性`() {
        // Given & When
        val stats = CombatStats()

        // Then
        assertEquals(10, stats.attack)
        assertEquals(10, stats.defense)
        assertEquals(10, stats.speed)
        assertEquals(5, stats.critRate)
        assertEquals(5, stats.dodgeRate)
    }

    @Test
    fun `自定义构造函数应创建具有指定值的战斗属性`() {
        // Given & When
        val stats = CombatStats(
            attack = 50,
            defense = 40,
            speed = 30,
            critRate = 15,
            dodgeRate = 10
        )

        // Then
        assertEquals(50, stats.attack)
        assertEquals(40, stats.defense)
        assertEquals(30, stats.speed)
        assertEquals(15, stats.critRate)
        assertEquals(10, stats.dodgeRate)
    }

    @Test
    fun `calculateEffectiveAttack应返回有效攻击力`() {
        // Given
        val stats = CombatStats(attack = 100)

        // When & Then
        assertEquals(100, stats.calculateEffectiveAttack())
    }

    @Test
    fun `calculateEffectiveDefense应返回有效防御力`() {
        // Given
        val stats = CombatStats(defense = 80)

        // When & Then
        assertEquals(80, stats.calculateEffectiveDefense())
    }

    @Test
    fun `calculateDamageReduction应返回正确的伤害减免百分比`() {
        // Given
        val lowDefense = CombatStats(defense = 10)
        val highDefense = CombatStats(defense = 100)

        // When & Then - 防御力/(防御力+100) 公式
        assertEquals(0.09, lowDefense.calculateDamageReduction(), 0.01)
        assertEquals(0.5, highDefense.calculateDamageReduction(), 0.01)
    }
}
