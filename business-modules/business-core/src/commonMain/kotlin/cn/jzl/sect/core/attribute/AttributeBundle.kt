package cn.jzl.sect.core.attribute

import kotlin.math.max

/**
 * 属性包组件 - 存储实体的完整属性信息
 *
 * 包含基础属性、资质属性、战斗属性和生活属性四大类
 * 支持基础值和加成值的分离计算
 */
data class AttributeBundle(
    // 基础属性
    val realmLevel: Int = 1,        // 境界等级
    val health: Int = 100,          // 气血
    val spirit: Int = 100,          // 灵力

    // 资质属性
    val physique: Int = 10,         // 根骨
    val comprehension: Int = 10,    // 悟性
    val fortune: Int = 10,          // 福缘
    val mental: Int = 10,           // 心性
    val consciousness: Int = 10,    // 神识

    // 战斗属性
    val attack: Int = 10,           // 攻击
    val defense: Int = 10,          // 防御
    val speed: Int = 10,            // 速度
    val critRate: Int = 5,          // 暴击率
    val dodgeRate: Int = 5,         // 闪避率
    val penetration: Int = 0,       // 穿透

    // 生活属性
    val charm: Int = 10,            // 魅力
    val leadership: Int = 10,       // 领导力
    val alchemy: Int = 0,           // 炼丹
    val forging: Int = 0,           // 炼器
    val planting: Int = 0,          // 种植
    val medicine: Int = 0,          // 医术

    // 加成值（用于计算最终属性）
    val healthBonus: Int = 0,       // 气血加成
    val spiritBonus: Int = 0,       // 灵力加成
    val attackBonus: Int = 0,       // 攻击加成
    val defenseBonus: Int = 0,      // 防御加成
    val speedBonus: Int = 0,        // 速度加成
    val critRateBonus: Int = 0,     // 暴击率加成
    val dodgeRateBonus: Int = 0,    // 闪避率加成
    val penetrationBonus: Int = 0,  // 穿透加成
    val charmBonus: Int = 0,        // 魅力加成
    val leadershipBonus: Int = 0,   // 领导力加成
    val alchemyBonus: Int = 0,      // 炼丹加成
    val forgingBonus: Int = 0,      // 炼器加成
    val plantingBonus: Int = 0,     // 种植加成
    val medicineBonus: Int = 0      // 医术加成
) {

    /**
     * 获取最终气血值（基础值 + 加成值）
     */
    fun getFinalHealth(): Int = health + healthBonus

    /**
     * 获取最终灵力值（基础值 + 加成值）
     */
    fun getFinalSpirit(): Int = spirit + spiritBonus

    /**
     * 获取最终攻击值（基础值 + 加成值）
     */
    fun getFinalAttack(): Int = attack + attackBonus

    /**
     * 获取最终防御值（基础值 + 加成值）
     */
    fun getFinalDefense(): Int = defense + defenseBonus

    /**
     * 获取最终速度值（基础值 + 加成值）
     */
    fun getFinalSpeed(): Int = speed + speedBonus

    /**
     * 获取最终暴击率（基础值 + 加成值）
     */
    fun getFinalCritRate(): Int = critRate + critRateBonus

    /**
     * 获取最终闪避率（基础值 + 加成值）
     */
    fun getFinalDodgeRate(): Int = dodgeRate + dodgeRateBonus

    /**
     * 获取最终穿透值（基础值 + 加成值）
     */
    fun getFinalPenetration(): Int = penetration + penetrationBonus

    /**
     * 获取最终魅力值（基础值 + 加成值）
     */
    fun getFinalCharm(): Int = charm + charmBonus

    /**
     * 获取最终领导力值（基础值 + 加成值）
     */
    fun getFinalLeadership(): Int = leadership + leadershipBonus

    /**
     * 获取最终炼丹值（基础值 + 加成值）
     */
    fun getFinalAlchemy(): Int = alchemy + alchemyBonus

    /**
     * 获取最终炼器值（基础值 + 加成值）
     */
    fun getFinalForging(): Int = forging + forgingBonus

    /**
     * 获取最终种植值（基础值 + 加成值）
     */
    fun getFinalPlanting(): Int = planting + plantingBonus

    /**
     * 获取最终医术值（基础值 + 加成值）
     */
    fun getFinalMedicine(): Int = medicine + medicineBonus

    /**
     * 添加属性加成
     *
     * @param healthBonus 气血加成
     * @param spiritBonus 灵力加成
     * @param attackBonus 攻击加成
     * @param defenseBonus 防御加成
     * @param speedBonus 速度加成
     * @param critRateBonus 暴击率加成
     * @param dodgeRateBonus 闪避率加成
     * @param penetrationBonus 穿透加成
     * @param charmBonus 魅力加成
     * @param leadershipBonus 领导力加成
     * @param alchemyBonus 炼丹加成
     * @param forgingBonus 炼器加成
     * @param plantingBonus 种植加成
     * @param medicineBonus 医术加成
     * @return 新的AttributeBundle实例，包含累加后的加成值
     */
    fun addBonus(
        healthBonus: Int = 0,
        spiritBonus: Int = 0,
        attackBonus: Int = 0,
        defenseBonus: Int = 0,
        speedBonus: Int = 0,
        critRateBonus: Int = 0,
        dodgeRateBonus: Int = 0,
        penetrationBonus: Int = 0,
        charmBonus: Int = 0,
        leadershipBonus: Int = 0,
        alchemyBonus: Int = 0,
        forgingBonus: Int = 0,
        plantingBonus: Int = 0,
        medicineBonus: Int = 0
    ): AttributeBundle = copy(
        healthBonus = this.healthBonus + healthBonus,
        spiritBonus = this.spiritBonus + spiritBonus,
        attackBonus = this.attackBonus + attackBonus,
        defenseBonus = this.defenseBonus + defenseBonus,
        speedBonus = this.speedBonus + speedBonus,
        critRateBonus = this.critRateBonus + critRateBonus,
        dodgeRateBonus = this.dodgeRateBonus + dodgeRateBonus,
        penetrationBonus = this.penetrationBonus + penetrationBonus,
        charmBonus = this.charmBonus + charmBonus,
        leadershipBonus = this.leadershipBonus + leadershipBonus,
        alchemyBonus = this.alchemyBonus + alchemyBonus,
        forgingBonus = this.forgingBonus + forgingBonus,
        plantingBonus = this.plantingBonus + plantingBonus,
        medicineBonus = this.medicineBonus + medicineBonus
    )

    /**
     * 移除属性加成
     *
     * @param healthBonus 要移除的气血加成
     * @param spiritBonus 要移除的灵力加成
     * @param attackBonus 要移除的攻击加成
     * @param defenseBonus 要移除的防御加成
     * @param speedBonus 要移除的速度加成
     * @param critRateBonus 要移除的暴击率加成
     * @param dodgeRateBonus 要移除的闪避率加成
     * @param penetrationBonus 要移除的穿透加成
     * @param charmBonus 要移除的魅力加成
     * @param leadershipBonus 要移除的领导力加成
     * @param alchemyBonus 要移除的炼丹加成
     * @param forgingBonus 要移除的炼器加成
     * @param plantingBonus 要移除的种植加成
     * @param medicineBonus 要移除的医术加成
     * @return 新的AttributeBundle实例，加成值不会低于0
     */
    fun removeBonus(
        healthBonus: Int = 0,
        spiritBonus: Int = 0,
        attackBonus: Int = 0,
        defenseBonus: Int = 0,
        speedBonus: Int = 0,
        critRateBonus: Int = 0,
        dodgeRateBonus: Int = 0,
        penetrationBonus: Int = 0,
        charmBonus: Int = 0,
        leadershipBonus: Int = 0,
        alchemyBonus: Int = 0,
        forgingBonus: Int = 0,
        plantingBonus: Int = 0,
        medicineBonus: Int = 0
    ): AttributeBundle = copy(
        healthBonus = max(0, this.healthBonus - healthBonus),
        spiritBonus = max(0, this.spiritBonus - spiritBonus),
        attackBonus = max(0, this.attackBonus - attackBonus),
        defenseBonus = max(0, this.defenseBonus - defenseBonus),
        speedBonus = max(0, this.speedBonus - speedBonus),
        critRateBonus = max(0, this.critRateBonus - critRateBonus),
        dodgeRateBonus = max(0, this.dodgeRateBonus - dodgeRateBonus),
        penetrationBonus = max(0, this.penetrationBonus - penetrationBonus),
        charmBonus = max(0, this.charmBonus - charmBonus),
        leadershipBonus = max(0, this.leadershipBonus - leadershipBonus),
        alchemyBonus = max(0, this.alchemyBonus - alchemyBonus),
        forgingBonus = max(0, this.forgingBonus - forgingBonus),
        plantingBonus = max(0, this.plantingBonus - plantingBonus),
        medicineBonus = max(0, this.medicineBonus - medicineBonus)
    )

    /**
     * 重置所有加成值为0
     *
     * @return 新的AttributeBundle实例，所有加成值为0
     */
    fun resetBonus(): AttributeBundle = copy(
        healthBonus = 0,
        spiritBonus = 0,
        attackBonus = 0,
        defenseBonus = 0,
        speedBonus = 0,
        critRateBonus = 0,
        dodgeRateBonus = 0,
        penetrationBonus = 0,
        charmBonus = 0,
        leadershipBonus = 0,
        alchemyBonus = 0,
        forgingBonus = 0,
        plantingBonus = 0,
        medicineBonus = 0
    )
}
