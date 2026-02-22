package cn.jzl.sect.skill.systems

import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.cultivation.Talent
import cn.jzl.sect.skill.components.Skill
import cn.jzl.sect.skill.components.SkillLearned

/**
 * 功法学习系统
 * 管理功法的检查学习条件、学习功能
 */
class SkillLearningSystem {

    /**
     * 检查是否可以学习功法
     *
     * @param skill 要学习的功法
     * @param currentRealm 当前境界
     * @param talent 角色天赋
     * @param learnedSkillIds 已学习的功法ID列表
     * @return 是否可以学习
     */
    fun canLearnSkill(
        skill: Skill,
        currentRealm: Realm,
        talent: Talent,
        learnedSkillIds: List<Long>
    ): Boolean {
        // 检查境界要求
        if (currentRealm.level < skill.requiredRealm.level) {
            return false
        }

        // 检查悟性要求
        if (talent.comprehension < skill.requiredComprehension) {
            return false
        }

        // 检查前置功法
        if (skill.hasPrerequisites()) {
            val hasAllPrerequisites = skill.prerequisiteSkillIds.all { prereqId ->
                learnedSkillIds.contains(prereqId)
            }
            if (!hasAllPrerequisites) {
                return false
            }
        }

        return true
    }

    /**
     * 学习功法
     * 创建已学习功法对象
     *
     * @param skill 要学习的功法
     * @return 已学习功法对象
     */
    fun learnSkill(skill: Skill): SkillLearned {
        return SkillLearned(
            skillId = skill.id,
            proficiency = 0,
            learnedTime = System.currentTimeMillis()
        )
    }

    /**
     * 计算学习成功率
     * 基于角色悟性和功法难度计算
     *
     * @param skill 要学习的功法
     * @param talent 角色天赋
     * @return 成功率(0.0 - 1.0)
     */
    fun calculateLearningSuccessRate(skill: Skill, talent: Talent): Double {
        val difficulty = skill.getLearningDifficulty()
        val comprehensionBonus = talent.comprehension / 100.0 * 0.5 // 悟性提供最多50%加成

        // 基础成功率60% + 悟性加成 - 难度惩罚
        val baseRate = 0.6
        val difficultyPenalty = difficulty / 200.0 // 难度惩罚

        return (baseRate + comprehensionBonus - difficultyPenalty).coerceIn(0.1, 0.95)
    }

    /**
     * 计算学习所需时间
     * 基于功法难度和角色悟性
     *
     * @param skill 要学习的功法
     * @param talent 角色天赋
     * @return 学习时间(游戏时间单位)
     */
    fun calculateLearningTime(skill: Skill, talent: Talent): Int {
        val difficulty = skill.getLearningDifficulty()
        val comprehensionFactor = 1.0 - (talent.comprehension / 200.0) // 悟性越高时间越短

        return (difficulty * 10 * comprehensionFactor).toInt()
    }
}
