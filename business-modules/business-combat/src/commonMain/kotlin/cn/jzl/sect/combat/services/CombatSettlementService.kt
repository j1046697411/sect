/**
 * 战斗结算服务
 *
 * 提供战斗结算功能：
 * - 战斗结果评估
 * - 战斗评价计算
 * - 经验奖励计算
 * - 资源奖励计算
 * - 掉落概率计算
 * - 声望奖励计算
 * - 结算报告生成
 */
package cn.jzl.sect.combat.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.combat.components.Combatant
import kotlin.math.min
import kotlin.random.Random

/**
 * 战斗结算服务
 *
 * 提供战斗结算功能：
 * - 战斗结果评估
 * - 战斗评价计算
 * - 经验奖励计算
 * - 资源奖励计算
 * - 掉落概率计算
 * - 声望奖励计算
 * - 结算报告生成
 *
 * 使用方式：
 * ```kotlin
 * val settlementService by world.di.instance<CombatSettlementService>()
 * val result = settlementService.assessCombatResult(playerTeam, enemyTeam)
 * val report = settlementService.generateSettlementReport(...)
 * ```
 *
 * @property world ECS 世界实例
 */
class CombatSettlementService(override val world: World) : EntityRelationContext {

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
     * 结算报告
     */
    data class SettlementReport(
        val result: CombatResult,
        val rating: CombatRating,
        val experienceGained: Int,
        val resourcesGained: Int,
        val reputationGained: Int
    )

    /**
     * 评估战斗结果
     *
     * @param playerTeam 玩家队伍
     * @param enemyTeam 敌人队伍
     * @return 战斗结果
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
            else -> CombatResult.ESCAPE // 双方都有存活，可能是逃跑
        }
    }

    /**
     * 计算战斗评价
     * 基于玩家队伍剩余生命值
     *
     * @param playerTeam 玩家队伍
     * @return 战斗评价
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
     *
     * @param baseExperience 基础经验值
     * @param rating 战斗评价
     * @return 实际经验值
     */
    fun calculateExperienceReward(
        baseExperience: Int,
        rating: CombatRating
    ): Int {
        return (baseExperience * rating.experienceMultiplier).toInt()
    }

    /**
     * 计算资源奖励
     *
     * @param baseResources 基础资源量
     * @param rating 战斗评价
     * @return 实际资源量
     */
    fun calculateResourceReward(
        baseResources: Int,
        rating: CombatRating
    ): Int {
        return (baseResources * rating.experienceMultiplier).toInt()
    }

    /**
     * 计算掉落概率
     *
     * @param enemyLevel 敌人等级
     * @return 掉落概率(0-100)
     */
    fun calculateDropChance(enemyLevel: Int): Int {
        // 基础掉落率20%，每级增加2%，最高50%
        return min(50, 20 + enemyLevel * 2)
    }

    /**
     * 检查是否掉落物品
     *
     * @param enemyLevel 敌人等级
     * @return 是否掉落
     */
    fun checkDrop(enemyLevel: Int): Boolean {
        val chance = calculateDropChance(enemyLevel)
        return Random.nextInt(100) < chance
    }

    /**
     * 计算声望奖励
     *
     * @param enemyLevel 敌人等级
     * @param rating 战斗评价
     * @return 声望值
     */
    fun calculateReputationReward(
        enemyLevel: Int,
        rating: CombatRating
    ): Int {
        val baseReputation = enemyLevel * 5
        return (baseReputation * rating.experienceMultiplier).toInt()
    }

    /**
     * 生成战斗结算报告
     *
     * @param result 战斗结果
     * @param rating 战斗评价
     * @param experience 获得经验
     * @param resources 获得资源
     * @param reputation 获得声望
     * @return 结算报告
     */
    fun generateSettlementReport(
        result: CombatResult,
        rating: CombatRating,
        experience: Int,
        resources: Int,
        reputation: Int
    ): SettlementReport {
        return SettlementReport(
            result = result,
            rating = rating,
            experienceGained = experience,
            resourcesGained = resources,
            reputationGained = reputation
        )
    }
}
