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
import cn.jzl.sect.combat.systems.CombatSettlementSystem

/**
 * 战斗结算服务
 *
 * 提供战斗结算功能的服务代理：
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

    private val settlementSystem by lazy {
        CombatSettlementSystem()
    }

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
    ): CombatSettlementSystem.CombatResult {
        return settlementSystem.assessCombatResult(playerTeam, enemyTeam)
    }

    /**
     * 计算战斗评价
     * 基于玩家队伍剩余生命值
     *
     * @param playerTeam 玩家队伍
     * @return 战斗评价
     */
    fun calculateCombatRating(playerTeam: List<Combatant>): CombatSettlementSystem.CombatRating {
        return settlementSystem.calculateCombatRating(playerTeam)
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
        rating: CombatSettlementSystem.CombatRating
    ): Int {
        return settlementSystem.calculateExperienceReward(baseExperience, rating)
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
        rating: CombatSettlementSystem.CombatRating
    ): Int {
        return settlementSystem.calculateResourceReward(baseResources, rating)
    }

    /**
     * 计算掉落概率
     *
     * @param enemyLevel 敌人等级
     * @return 掉落概率(0-100)
     */
    fun calculateDropChance(enemyLevel: Int): Int {
        return settlementSystem.calculateDropChance(enemyLevel)
    }

    /**
     * 检查是否掉落物品
     *
     * @param enemyLevel 敌人等级
     * @return 是否掉落
     */
    fun checkDrop(enemyLevel: Int): Boolean {
        return settlementSystem.checkDrop(enemyLevel)
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
        rating: CombatSettlementSystem.CombatRating
    ): Int {
        return settlementSystem.calculateReputationReward(enemyLevel, rating)
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
        result: CombatSettlementSystem.CombatResult,
        rating: CombatSettlementSystem.CombatRating,
        experience: Int,
        resources: Int,
        reputation: Int
    ): CombatSettlementSystem.SettlementReport {
        return settlementSystem.generateSettlementReport(
            result,
            rating,
            experience,
            resources,
            reputation
        )
    }
}
