package cn.jzl.sect.combat.systems

import cn.jzl.sect.combat.components.Combatant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 战斗结算系统测试类
 */
class CombatSettlementSystemTest {

    @Test
    fun `评估战斗结果应根据双方状态判断`() {
        // Given
        val system = CombatSettlementSystem()
        val playerTeam = listOf(Combatant(entityId = 1L, isAlive = true))
        val enemyTeam = listOf(Combatant(entityId = 2L, isAlive = false))

        // When
        val result = system.assessCombatResult(playerTeam, enemyTeam)

        // Then
        assertEquals(CombatSettlementSystem.CombatResult.VICTORY, result)
    }

    @Test
    fun `计算战斗评价应根据玩家剩余生命值判断`() {
        // Given
        val system = CombatSettlementSystem()
        val playerTeam = listOf(
            Combatant(entityId = 1L, currentHp = 100, maxHp = 100, isAlive = true)
        )

        // When & Then
        assertEquals(
            CombatSettlementSystem.CombatRating.PERFECT,
            system.calculateCombatRating(playerTeam)
        )

        // 损失一些生命值
        val damagedTeam = listOf(
            Combatant(entityId = 1L, currentHp = 70, maxHp = 100, isAlive = true)
        )
        assertEquals(
            CombatSettlementSystem.CombatRating.GOOD,
            system.calculateCombatRating(damagedTeam)
        )
    }

    @Test
    fun `计算经验奖励应根据战斗评价`() {
        // Given
        val system = CombatSettlementSystem()
        val baseExp = 100

        // When & Then
        assertEquals(200, system.calculateExperienceReward(baseExp, CombatSettlementSystem.CombatRating.PERFECT))
        assertEquals(150, system.calculateExperienceReward(baseExp, CombatSettlementSystem.CombatRating.EXCELLENT))
        assertEquals(120, system.calculateExperienceReward(baseExp, CombatSettlementSystem.CombatRating.GOOD))
        assertEquals(100, system.calculateExperienceReward(baseExp, CombatSettlementSystem.CombatRating.NORMAL))
        assertEquals(70, system.calculateExperienceReward(baseExp, CombatSettlementSystem.CombatRating.PYRRHIC))
    }

    @Test
    fun `计算掉落概率应根据敌人等级`() {
        // Given
        val system = CombatSettlementSystem()

        // When & Then
        val lowLevelDrop = system.calculateDropChance(1)
        val highLevelDrop = system.calculateDropChance(10)

        assertTrue(highLevelDrop >= lowLevelDrop)
    }
}
