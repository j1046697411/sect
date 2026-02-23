package cn.jzl.sect.combat.services

import cn.jzl.sect.combat.components.Combatant
import kotlin.math.min
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 战斗结算服务测试类
 */
class CombatSettlementServiceTest {

    companion object {
        /**
         * 战斗结果枚举
         */
        enum class CombatResult {
            VICTORY,    // 胜利
            DEFEAT,     // 失败
            DRAW,       // 平局
            ESCAPE      // 逃跑
        }

        /**
         * 战斗评价枚举
         */
        enum class CombatRating(val displayName: String, val experienceMultiplier: Double) {
            PERFECT("完美", 2.0),       // 无伤胜利
            EXCELLENT("优秀", 1.5),     // 损失少量生命值
            GOOD("良好", 1.2),          // 损失一定生命值
            NORMAL("普通", 1.0),        // 正常胜利
            PYRRHIC("惨胜", 0.7);       // 损失大量生命值

            companion object {
                fun fromHpPercentage(percentage: Double): CombatRating {
                    return when {
                        percentage >= 1.0 -> PERFECT
                        percentage >= 0.8 -> EXCELLENT
                        percentage >= 0.5 -> GOOD
                        percentage >= 0.2 -> NORMAL
                        else -> PYRRHIC
                    }
                }
            }
        }

        /**
         * 评估战斗结果
         */
        fun assessCombatResult(
            playerTeam: List<Combatant>,
            enemyTeam: List<Combatant>
        ): CombatResult {
            val playerAlive = playerTeam.any { it.isAlive }
            val enemyAlive = enemyTeam.any { it.isAlive }

            return when {
                playerAlive && !enemyAlive -> CombatResult.VICTORY
                !playerAlive && enemyAlive -> CombatResult.DEFEAT
                !playerAlive && !enemyAlive -> CombatResult.DRAW
                else -> CombatResult.ESCAPE
            }
        }

        /**
         * 计算战斗评价
         */
        fun calculateCombatRating(playerTeam: List<Combatant>): CombatRating {
            val totalMaxHp = playerTeam.sumOf { it.maxHp }
            val totalCurrentHp = playerTeam.sumOf { it.currentHp }

            val hpPercentage = if (totalMaxHp > 0) {
                totalCurrentHp.toDouble() / totalMaxHp
            } else {
                0.0
            }

            return CombatRating.fromHpPercentage(hpPercentage)
        }

        /**
         * 计算经验奖励
         */
        fun calculateExperienceReward(
            baseExperience: Int,
            rating: CombatRating
        ): Int {
            return (baseExperience * rating.experienceMultiplier).toInt()
        }

        /**
         * 计算掉落概率
         */
        fun calculateDropChance(enemyLevel: Int): Int {
            return min(50, 20 + enemyLevel * 2)
        }
    }

    @Test
    fun `评估战斗结果应根据双方状态判断`() {
        // Given
        val playerTeam = listOf(Combatant(entityId = 1L, isAlive = true))
        val enemyTeam = listOf(Combatant(entityId = 2L, isAlive = false))

        // When
        val result = assessCombatResult(playerTeam, enemyTeam)

        // Then
        assertEquals(CombatResult.VICTORY, result)
    }

    @Test
    fun `计算战斗评价应根据玩家剩余生命值判断`() {
        // Given
        val playerTeam = listOf(
            Combatant(entityId = 1L, currentHp = 100, maxHp = 100, isAlive = true)
        )

        // When & Then
        assertEquals(
            CombatRating.PERFECT,
            calculateCombatRating(playerTeam)
        )

        // 损失一些生命值
        val damagedTeam = listOf(
            Combatant(entityId = 1L, currentHp = 70, maxHp = 100, isAlive = true)
        )
        assertEquals(
            CombatRating.GOOD,
            calculateCombatRating(damagedTeam)
        )
    }

    @Test
    fun `计算经验奖励应根据战斗评价`() {
        // Given
        val baseExp = 100

        // When & Then
        assertEquals(200, calculateExperienceReward(baseExp, CombatRating.PERFECT))
        assertEquals(150, calculateExperienceReward(baseExp, CombatRating.EXCELLENT))
        assertEquals(120, calculateExperienceReward(baseExp, CombatRating.GOOD))
        assertEquals(100, calculateExperienceReward(baseExp, CombatRating.NORMAL))
        assertEquals(70, calculateExperienceReward(baseExp, CombatRating.PYRRHIC))
    }

    @Test
    fun `计算掉落概率应根据敌人等级`() {
        // When & Then
        val lowLevelDrop = calculateDropChance(1)
        val highLevelDrop = calculateDropChance(10)

        assertTrue(highLevelDrop >= lowLevelDrop)
    }
}
