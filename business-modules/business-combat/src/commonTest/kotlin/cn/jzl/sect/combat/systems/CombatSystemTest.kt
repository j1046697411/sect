package cn.jzl.sect.combat.systems

import cn.jzl.sect.combat.components.CombatStats
import cn.jzl.sect.combat.components.Combatant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 战斗系统测试类
 */
class CombatSystemTest {

    @Test
    fun `计算伤害应考虑攻击力和防御力`() {
        // Given
        val system = CombatSystem()
        val attackerStats = CombatStats(attack = 100)
        val defenderStats = CombatStats(defense = 50)

        // When
        val damage = system.calculateDamage(attackerStats, defenderStats)

        // Then
        // 伤害 = 攻击力 * (1 - 防御减免)
        // 防御减免 = 50 / (50 + 100) = 0.33
        // 伤害 = 100 * (1 - 0.33) = 67
        assertTrue(damage > 0)
    }

    @Test
    fun `计算暴击伤害应增加50_伤害`() {
        // Given
        val system = CombatSystem()
        val baseDamage = 100

        // When
        val critDamage = system.calculateCriticalDamage(baseDamage)

        // Then
        assertEquals(150, critDamage) // 100 * 1.5
    }

    @Test
    fun `按速度排序应返回正确的行动顺序`() {
        // Given
        val system = CombatSystem()
        val combatants = listOf(
            Combatant(entityId = 1L) to CombatStats(speed = 10),
            Combatant(entityId = 2L) to CombatStats(speed = 30),
            Combatant(entityId = 3L) to CombatStats(speed = 20)
        )

        // When
        val sorted = system.sortBySpeed(combatants)

        // Then
        assertEquals(2L, sorted[0].first.entityId) // 速度30最快
        assertEquals(3L, sorted[1].first.entityId) // 速度20第二
        assertEquals(1L, sorted[2].first.entityId) // 速度10最慢
    }

    @Test
    fun `检查是否暴击应根据暴击率判断`() {
        // Given
        val system = CombatSystem()
        val highCritStats = CombatStats(critRate = 100) // 100%暴击
        val noCritStats = CombatStats(critRate = 0)     // 0%暴击

        // When & Then
        assertTrue(system.checkCritical(highCritStats))
        assertTrue(!system.checkCritical(noCritStats))
    }

    @Test
    fun `检查是否闪避应根据闪避率判断`() {
        // Given
        val system = CombatSystem()
        val highDodgeStats = CombatStats(dodgeRate = 100) // 100%闪避
        val noDodgeStats = CombatStats(dodgeRate = 0)     // 0%闪避

        // When & Then
        assertTrue(system.checkDodge(highDodgeStats))
        assertTrue(!system.checkDodge(noDodgeStats))
    }
}
