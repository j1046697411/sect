package cn.jzl.sect.skill.components

import kotlin.math.min

/**
 * 已学习功法组件
 * 存储角色已学习的功法信息
 *
 * @property skillId 功法ID
 * @property proficiency 熟练度(0-100)
 * @property learnedTime 学习时间戳
 */
data class SkillLearned(
    val skillId: Long = 0L,
    val proficiency: Int = 0,
    val learnedTime: Long = 0L
) {

    companion object {
        // 掌握功法的熟练度阈值
        const val MASTERY_THRESHOLD = 50

        // 精通功法的熟练度阈值
        const val PERFECTION_THRESHOLD = 100
    }

    /**
     * 增加熟练度
     *
     * @param amount 增加的熟练度数值
     * @return 新的SkillLearned实例
     */
    fun increaseProficiency(amount: Int): SkillLearned {
        return copy(proficiency = min(PERFECTION_THRESHOLD, proficiency + amount))
    }

    /**
     * 检查是否已掌握功法
     * 熟练度达到50表示掌握
     *
     * @return 如果已掌握返回true
     */
    fun isMastered(): Boolean {
        return proficiency >= MASTERY_THRESHOLD
    }

    /**
     * 检查是否已精通功法
     * 熟练度达到100表示精通
     *
     * @return 如果已精通返回true
     */
    fun isPerfected(): Boolean {
        return proficiency >= PERFECTION_THRESHOLD
    }

    /**
     * 获取效果倍率
     * 根据熟练度计算功法效果的实际倍率
     * 公式：熟练度/100 * 0.5 + 0.5
     * 范围：0.5 - 1.0
     *
     * @return 效果倍率
     */
    fun getEffectMultiplier(): Double {
        return proficiency / 100.0 * 0.5 + 0.5
    }

    /**
     * 检查是否可以传承
     * 熟练度达到50才能传承
     *
     * @return 如果可以传承返回true
     */
    fun canInherit(): Boolean {
        return proficiency >= MASTERY_THRESHOLD
    }
}
