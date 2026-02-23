package cn.jzl.sect.cultivation.components

import cn.jzl.sect.core.cultivation.AptitudeLevel

/**
 * 资质评级枚举 - 根据资质属性平均值划分等级
 */
enum class TalentAptitudeLevel(val description: String) {
    GENIUS("天才"),      // 平均值 >= 80
    EXCELLENT("优秀"),   // 平均值 >= 60
    GOOD("良好"),        // 平均值 >= 40
    AVERAGE("普通"),     // 平均值 >= 20
    POOR("平庸")         // 平均值 < 20
}

/**
 * 修炼天赋组件 - 存储弟子的修炼天赋属性
 *
 * 包含三类属性：
 * 1. 资质属性：影响修炼速度和突破概率
 * 2. 战斗属性：影响战斗能力
 * 3. 生活属性：影响生活技能效率
 */
data class Talent(
    // 资质属性
    val physique: Int = 50,         // 根骨 - 影响气血上限和炼体效率
    val comprehension: Int = 50,    // 悟性 - 影响修炼速度和功法领悟
    val fortune: Int = 50,          // 福缘 - 影响奇遇概率和突破成功率
    val mental: Int = 50,           // 心性 - 影响心魔抵抗和修炼稳定性

    // 战斗属性
    val strength: Int = 50,         // 力量 - 影响物理攻击力
    val agility: Int = 50,          // 敏捷 - 影响速度和闪避率
    val intelligence: Int = 50,     // 智力 - 影响法术攻击力和策略
    val endurance: Int = 50,        // 耐力 - 影响防御力和持久力

    // 生活属性
    val charm: Int = 50,            // 魅力 - 影响社交和招募效果
    val alchemyTalent: Int = 50,    // 炼丹天赋 - 影响炼丹成功率
    val forgingTalent: Int = 50     // 炼器天赋 - 影响炼器成功率
) {

    /**
     * 获取资质属性总评分
     *
     * @return 根骨、悟性、福缘、心性四项属性之和
     */
    fun getTotalAptitudeScore(): Int = physique + comprehension + fortune + mental

    /**
     * 获取资质属性平均值
     *
     * @return 四项资质属性的平均值
     */
    fun getAverageAptitude(): Double = getTotalAptitudeScore() / 4.0

    /**
     * 获取战斗能力总评分
     *
     * @return 力量、敏捷、智力、耐力四项属性之和
     */
    fun getCombatPower(): Int = strength + agility + intelligence + endurance

    /**
     * 获取生活技能总评分
     *
     * @return 魅力、炼丹天赋、炼器天赋三项属性之和
     */
    fun getLifeSkillScore(): Int = charm + alchemyTalent + forgingTalent

    /**
     * 获取综合总评分
     *
     * @return 所有属性之和
     */
    fun getOverallScore(): Int = getTotalAptitudeScore() + getCombatPower() + getLifeSkillScore()

    /**
     * 获取资质评级
     *
     * 根据资质属性平均值划分等级：
     * - 天才：平均值 >= 80
     * - 优秀：平均值 >= 60
     * - 良好：平均值 >= 40
     * - 普通：平均值 >= 20
     * - 平庸：平均值 < 20
     *
     * @return 资质评级枚举
     */
    fun getAptitudeLevel(): TalentAptitudeLevel {
        val average = getAverageAptitude()
        return when {
            average >= 80.0 -> TalentAptitudeLevel.GENIUS
            average >= 60.0 -> TalentAptitudeLevel.EXCELLENT
            average >= 40.0 -> TalentAptitudeLevel.GOOD
            average >= 20.0 -> TalentAptitudeLevel.AVERAGE
            else -> TalentAptitudeLevel.POOR
        }
    }
}
