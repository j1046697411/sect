package cn.jzl.sect.combat.components

/**
 * 战斗属性组件
 * 存储角色的战斗相关属性
 *
 * @property attack 攻击力
 * @property defense 防御力
 * @property speed 速度(决定行动顺序)
 * @property critRate 暴击率(百分比)
 * @property dodgeRate 闪避率(百分比)
 */
data class CombatStats(
    val attack: Int = 10,
    val defense: Int = 10,
    val speed: Int = 10,
    val critRate: Int = 5,      // 百分比
    val dodgeRate: Int = 5      // 百分比
) {

    /**
     * 计算有效攻击力
     *
     * @return 有效攻击力
     */
    fun calculateEffectiveAttack(): Int {
        return attack
    }

    /**
     * 计算有效防御力
     *
     * @return 有效防御力
     */
    fun calculateEffectiveDefense(): Int {
        return defense
    }

    /**
     * 计算伤害减免百分比
     * 使用防御力/(防御力+100)公式
     *
     * @return 伤害减免百分比(0.0 - 1.0)
     */
    fun calculateDamageReduction(): Double {
        return defense.toDouble() / (defense + 100)
    }

    /**
     * 计算战斗实力值
     * 综合评估战斗能力
     *
     * @return 战斗实力值
     */
    fun calculateCombatPower(): Int {
        return attack + defense + speed * 2 + critRate * 2 + dodgeRate * 2
    }
}
