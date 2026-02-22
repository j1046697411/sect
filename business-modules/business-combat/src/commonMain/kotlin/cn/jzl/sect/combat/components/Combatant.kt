package cn.jzl.sect.combat.components

import kotlin.math.max
import kotlin.math.min

/**
 * 战斗参与者组件
 * 存储角色在战斗中的状态
 *
 * @property entityId 实体ID
 * @property currentHp 当前生命值
 * @property maxHp 最大生命值
 * @property isAlive 是否存活
 * @property currentAction 当前行动
 */
data class Combatant(
    val entityId: Long = 0L,
    val currentHp: Int = 100,
    val maxHp: Int = 100,
    val isAlive: Boolean = false,
    val currentAction: CombatActionType = CombatActionType.ATTACK
) {

    companion object {
        // 危急生命值阈值(20%)
        const val CRITICAL_HP_THRESHOLD = 0.2
    }

    /**
     * 受到伤害
     *
     * @param damage 伤害值
     * @return 更新后的Combatant实例
     */
    fun takeDamage(damage: Int): Combatant {
        val newHp = max(0, currentHp - damage)
        return copy(
            currentHp = newHp,
            isAlive = newHp > 0
        )
    }

    /**
     * 恢复生命值
     *
     * @param amount 恢复量
     * @return 更新后的Combatant实例
     */
    fun heal(amount: Int): Combatant {
        return copy(currentHp = min(maxHp, currentHp + amount))
    }

    /**
     * 获取生命值百分比
     *
     * @return 生命值百分比(0.0 - 1.0)
     */
    fun getHpPercentage(): Double {
        return if (maxHp > 0) currentHp.toDouble() / maxHp else 0.0
    }

    /**
     * 检查是否处于危急状态
     * 生命值低于20%
     *
     * @return 是否危急
     */
    fun isCriticalHp(): Boolean {
        return getHpPercentage() < CRITICAL_HP_THRESHOLD
    }

    /**
     * 设置行动
     *
     * @param action 行动类型
     * @return 更新后的Combatant实例
     */
    fun setAction(action: CombatActionType): Combatant {
        return copy(currentAction = action)
    }

    /**
     * 复活
     *
     * @param hpPercent 复活后的生命值百分比
     * @return 更新后的Combatant实例
     */
    fun revive(hpPercent: Double = 0.5): Combatant {
        val newHp = (maxHp * hpPercent).toInt().coerceIn(1, maxHp)
        return copy(
            currentHp = newHp,
            isAlive = true
        )
    }
}
