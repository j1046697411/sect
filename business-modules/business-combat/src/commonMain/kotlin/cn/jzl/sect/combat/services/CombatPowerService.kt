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
import cn.jzl.sect.core.cultivation.Realm

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

    companion object {
        // 境界实力权重: 50%
        const val REALM_WEIGHT = 0.5

        // 属性实力权重: 30%
        const val ATTRIBUTE_WEIGHT = 0.3

        // 功法实力权重: 15%
        const val SKILL_WEIGHT = 0.15

        // 装备实力权重: 5%
        const val EQUIPMENT_WEIGHT = 0.05

        // 境界实力基数
        const val REALM_BASE_POWER = 1000
    }

    /**
     * 战斗等级枚举
     */
    enum class CombatLevel(val displayName: String, val minPower: Int) {
        WEAK("弱小", 0),
        AVERAGE("普通", 300),
        COMPETENT("胜任", 800),
        STRONG("强悍", 1500),
        ELITE("精锐", 3000),
        MASTER("大师", 6000),
        LEGENDARY("传奇", 10000);

        companion object {
            fun fromPower(power: Int): CombatLevel {
                return entries.reversed().find { power >= it.minPower } ?: WEAK
            }
        }
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
        val realmPower = calculateRealmPower(realm)
        val attributePower = calculateAttributePower(stats)

        return (
            realmPower * REALM_WEIGHT +
            attributePower * ATTRIBUTE_WEIGHT +
            skillPower * SKILL_WEIGHT +
            equipmentPower * EQUIPMENT_WEIGHT
        ).toInt()
    }

    /**
     * 计算境界实力
     * 公式: 境界等级 * 基数 * 权重
     *
     * @param realm 境界
     * @return 境界实力值
     */
    fun calculateRealmPower(realm: Realm): Int {
        return (realm.level * REALM_BASE_POWER * REALM_WEIGHT).toInt()
    }

    /**
     * 计算属性实力
     * 综合评估所有战斗属性
     *
     * @param stats 战斗属性
     * @return 属性实力值
     */
    fun calculateAttributePower(stats: CombatStats): Int {
        // 攻击和防御权重1，速度和暴击闪避权重2
        val totalAttribute = stats.attack +
                stats.defense +
                stats.speed * 2 +
                stats.critRate * 2 +
                stats.dodgeRate * 2

        return (totalAttribute * ATTRIBUTE_WEIGHT).toInt()
    }

    /**
     * 评估战斗等级
     *
     * @param power 战斗实力值
     * @return 战斗等级
     */
    fun assessCombatLevel(power: Int): CombatLevel {
        return CombatLevel.fromPower(power)
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
        return if (defenderPower > 0) {
            attackerPower.toDouble() / defenderPower
        } else {
            999.0 // 防守方无实力，攻击方极大优势
        }
    }

    /**
     * 评估战斗难度
     *
     * @param playerPower 玩家实力
     * @param enemyPower 敌人实力
     * @return 难度描述
     */
    fun assessDifficulty(playerPower: Int, enemyPower: Int): String {
        val ratio = calculatePowerRatio(enemyPower, playerPower)

        return when {
            ratio < 0.5 -> "简单"
            ratio < 0.8 -> "较易"
            ratio < 1.2 -> "适中"
            ratio < 1.5 -> "困难"
            ratio < 2.0 -> "极难"
            else -> "不可能"
        }
    }
}
