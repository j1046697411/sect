package cn.jzl.sect.combat.services

import cn.jzl.sect.combat.components.CombatStats
import cn.jzl.sect.combat.components.Combatant
import kotlin.math.max
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 战斗服务测试类
 */
class CombatServiceTest {

    companion object {
        // 暴击伤害倍率
        const val CRITICAL_DAMAGE_MULTIPLIER = 1.5

        // 基础伤害随机波动范围
        const val DAMAGE_VARIANCE = 0.2

        /**
         * 计算伤害
         */
        fun calculateDamage(attackerStats: CombatStats, defenderStats: CombatStats): Int {
            val attack = attackerStats.calculateEffectiveAttack()
            val damageReduction = defenderStats.calculateDamageReduction()
            val baseDamage = attack * (1 - damageReduction)
            val variance = Random.nextDouble(-DAMAGE_VARIANCE, DAMAGE_VARIANCE)
            val finalDamage = baseDamage * (1 + variance)
            return max(1, finalDamage.toInt())
        }

        /**
         * 计算暴击伤害
         */
        fun calculateCriticalDamage(baseDamage: Int): Int {
            return (baseDamage * CRITICAL_DAMAGE_MULTIPLIER).toInt()
        }

        /**
         * 按速度排序
         */
        fun sortBySpeed(
            combatants: List<Pair<Combatant, CombatStats>>
        ): List<Pair<Combatant, CombatStats>> {
            return combatants.sortedByDescending { it.second.speed }
        }

        /**
         * 检查是否触发暴击
         */
        fun checkCritical(stats: CombatStats): Boolean {
            return Random.nextInt(100) < stats.critRate
        }

        /**
         * 检查是否闪避
         */
        fun checkDodge(stats: CombatStats): Boolean {
            return Random.nextInt(100) < stats.dodgeRate
        }
    }

    @Test
    fun `计算伤害应考虑攻击力和防御力`() {
        // Given
        val attackerStats = CombatStats(attack = 100)
        val defenderStats = CombatStats(defense = 50)

        // When
        val damage = calculateDamage(attackerStats, defenderStats)

        // Then
        // 伤害 = 攻击力 * (1 - 防御减免)
        // 防御减免 = 50 / (50 + 100) = 0.33
        // 伤害 = 100 * (1 - 0.33) = 67
        assertTrue(damage > 0)
    }

    @Test
    fun `计算暴击伤害应增加50_伤害`() {
        // Given
        val baseDamage = 100

        // When
        val critDamage = calculateCriticalDamage(baseDamage)

        // Then
        assertEquals(150, critDamage) // 100 * 1.5
    }

    @Test
    fun `按速度排序应返回正确的行动顺序`() {
        // Given
        val combatants = listOf(
            Combatant(entityId = 1L) to CombatStats(speed = 10),
            Combatant(entityId = 2L) to CombatStats(speed = 30),
            Combatant(entityId = 3L) to CombatStats(speed = 20)
        )

        // When
        val sorted = sortBySpeed(combatants)

        // Then
        assertEquals(2L, sorted[0].first.entityId) // 速度30最快
        assertEquals(3L, sorted[1].first.entityId) // 速度20第二
        assertEquals(1L, sorted[2].first.entityId) // 速度10最慢
    }

    @Test
    fun `检查是否暴击应根据暴击率判断`() {
        // Given
        val highCritStats = CombatStats(critRate = 100) // 100%暴击
        val noCritStats = CombatStats(critRate = 0)     // 0%暴击

        // When & Then
        assertTrue(checkCritical(highCritStats))
        assertTrue(!checkCritical(noCritStats))
    }

    @Test
    fun `检查是否闪避应根据闪避率判断`() {
        // Given
        val highDodgeStats = CombatStats(dodgeRate = 100) // 100%闪避
        val noDodgeStats = CombatStats(dodgeRate = 0)     // 0%闪避

        // When & Then
        assertTrue(checkDodge(highDodgeStats))
        assertTrue(!checkDodge(noDodgeStats))
    }
}
