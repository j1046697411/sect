package cn.jzl.sect.combat.systems

import cn.jzl.sect.combat.components.CombatStats
import cn.jzl.sect.combat.components.Combatant
import kotlin.math.max
import kotlin.random.Random

/**
 * 战斗系统
 * 管理回合制战斗的核心逻辑
 */
class CombatSystem {

    companion object {
        // 暴击伤害倍率
        const val CRITICAL_DAMAGE_MULTIPLIER = 1.5

        // 基础伤害随机波动范围
        const val DAMAGE_VARIANCE = 0.2
    }

    /**
     * 计算伤害
     * 基于攻击力和防御力计算实际伤害
     *
     * @param attackerStats 攻击者属性
     * @param defenderStats 防守者属性
     * @return 伤害值
     */
    fun calculateDamage(attackerStats: CombatStats, defenderStats: CombatStats): Int {
        val attack = attackerStats.calculateEffectiveAttack()
        val damageReduction = defenderStats.calculateDamageReduction()

        // 基础伤害 = 攻击力 * (1 - 伤害减免)
        val baseDamage = attack * (1 - damageReduction)

        // 添加随机波动 (±20%)
        val variance = Random.nextDouble(-DAMAGE_VARIANCE, DAMAGE_VARIANCE)
        val finalDamage = baseDamage * (1 + variance)

        return max(1, finalDamage.toInt()) // 最小伤害为1
    }

    /**
     * 计算暴击伤害
     *
     * @param baseDamage 基础伤害
     * @return 暴击伤害
     */
    fun calculateCriticalDamage(baseDamage: Int): Int {
        return (baseDamage * CRITICAL_DAMAGE_MULTIPLIER).toInt()
    }

    /**
     * 按速度排序战斗参与者
     * 速度快的先行动
     *
     * @param combatants 战斗参与者列表
     * @return 排序后的列表
     */
    fun sortBySpeed(
        combatants: List<Pair<Combatant, CombatStats>>
    ): List<Pair<Combatant, CombatStats>> {
        return combatants.sortedByDescending { it.second.speed }
    }

    /**
     * 检查是否触发暴击
     *
     * @param stats 战斗属性
     * @return 是否暴击
     */
    fun checkCritical(stats: CombatStats): Boolean {
        return Random.nextInt(100) < stats.critRate
    }

    /**
     * 检查是否闪避
     *
     * @param stats 战斗属性
     * @return 是否闪避成功
     */
    fun checkDodge(stats: CombatStats): Boolean {
        return Random.nextInt(100) < stats.dodgeRate
    }

    /**
     * 执行攻击
     *
     * @param attacker 攻击者
     * @param attackerStats 攻击者属性
     * @param defender 防守者
     * @param defenderStats 防守者属性
     * @return 攻击结果(伤害值, 是否暴击, 是否闪避)
     */
    fun executeAttack(
        attacker: Combatant,
        attackerStats: CombatStats,
        defender: Combatant,
        defenderStats: CombatStats
    ): AttackResult {
        // 检查闪避
        if (checkDodge(defenderStats)) {
            return AttackResult(0, false, true)
        }

        // 计算基础伤害
        var damage = calculateDamage(attackerStats, defenderStats)
        var isCritical = false

        // 检查暴击
        if (checkCritical(attackerStats)) {
            damage = calculateCriticalDamage(damage)
            isCritical = true
        }

        return AttackResult(damage, isCritical, false)
    }

    /**
     * 攻击结果
     */
    data class AttackResult(
        val damage: Int,
        val isCritical: Boolean,
        val isDodged: Boolean
    )
}
