/**
 * 战斗实力服务
 *
 * 提供战斗实力计算功能：
 * - 综合战斗实力计算
 * - 境界实力计算
 * - 属性实力计算
 * - 战斗等级评估
 * - 战斗力比率计算
 * - 战斗难度评估
 */
package cn.jzl.sect.combat.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.combat.components.CombatStats
import cn.jzl.sect.combat.systems.CombatPowerCalculator
import cn.jzl.sect.core.cultivation.Realm

/**
 * 战斗实力服务
 *
 * 提供战斗实力计算功能的服务代理：
 * - 综合战斗实力计算
 * - 境界实力计算
 * - 属性实力计算
 * - 战斗等级评估
 * - 战斗力比率计算
 * - 战斗难度评估
 *
 * 使用方式：
 * ```kotlin
 * val powerService by world.di.instance<CombatPowerService>()
 * val power = powerService.calculateCombatPower(realm, stats)
 * val level = powerService.assessCombatLevel(power)
 * ```
 *
 * @property world ECS 世界实例
 */
class CombatPowerService(override val world: World) : EntityRelationContext {

    private val powerCalculator by lazy {
        CombatPowerCalculator()
    }

    /**
     * 计算综合战斗实力
     *
     * @param realm 当前境界
     * @param stats 战斗属性
     * @param skillPower 功法实力值
     * @param equipmentPower 装备实力值
     * @return 综合战斗实力
     */
    fun calculateCombatPower(
        realm: Realm,
        stats: CombatStats,
        skillPower: Double = 0.0,
        equipmentPower: Double = 0.0
    ): Int {
        return powerCalculator.calculateCombatPower(realm, stats, skillPower, equipmentPower)
    }

    /**
     * 计算境界实力
     * 公式: 境界等级 * 基数 * 权重
     *
     * @param realm 境界
     * @return 境界实力值
     */
    fun calculateRealmPower(realm: Realm): Int {
        return powerCalculator.calculateRealmPower(realm)
    }

    /**
     * 计算属性实力
     * 综合评估所有战斗属性
     *
     * @param stats 战斗属性
     * @return 属性实力值
     */
    fun calculateAttributePower(stats: CombatStats): Int {
        return powerCalculator.calculateAttributePower(stats)
    }

    /**
     * 评估战斗等级
     *
     * @param power 战斗实力值
     * @return 战斗等级
     */
    fun assessCombatLevel(power: Int): CombatPowerCalculator.CombatLevel {
        return powerCalculator.assessCombatLevel(power)
    }

    /**
     * 计算战斗力比率
     * 用于评估双方实力差距
     *
     * @param attackerPower 攻击方实力
     * @param defenderPower 防守方实力
     * @return 实力比率(>1表示攻击方强，<1表示防守方强)
     */
    fun calculatePowerRatio(attackerPower: Int, defenderPower: Int): Double {
        return powerCalculator.calculatePowerRatio(attackerPower, defenderPower)
    }

    /**
     * 评估战斗难度
     *
     * @param playerPower 玩家实力
     * @param enemyPower 敌人实力
     * @return 难度描述
     */
    fun assessDifficulty(playerPower: Int, enemyPower: Int): String {
        return powerCalculator.assessDifficulty(playerPower, enemyPower)
    }
}
