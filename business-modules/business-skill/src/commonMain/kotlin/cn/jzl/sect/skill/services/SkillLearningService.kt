/**
 * 功法学习服务
 *
 * 提供功法学习管理功能：
 * - 检查学习条件（境界、悟性、前置功法）
 * - 学习功法创建已学习对象
 * - 计算学习成功率和所需时间
 */
package cn.jzl.sect.skill.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.cultivation.Talent
import cn.jzl.sect.skill.components.Skill
import cn.jzl.sect.skill.components.SkillLearned
import cn.jzl.sect.skill.systems.SkillLearningSystem

/**
 * 功法学习服务
 *
 * 提供功法学习管理功能的核心服务：
 * - 检查学习条件（境界、悟性、前置功法）
 * - 学习功法创建已学习对象
 * - 计算学习成功率和所需时间
 *
 * 使用方式：
 * ```kotlin
 * val skillLearningService by world.di.instance<SkillLearningService>()
 * val canLearn = skillLearningService.canLearnSkill(skill, currentRealm, talent, learnedSkillIds)
 * val learned = skillLearningService.learnSkill(skill)
 * ```
 *
 * @property world ECS 世界实例
 */
class SkillLearningService(override val world: World) : EntityRelationContext {

    private val skillLearningSystem by lazy {
        SkillLearningSystem()
    }

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
        return skillLearningSystem.canLearnSkill(skill, currentRealm, talent, learnedSkillIds)
    }

    /**
     * 学习功法
     * 创建已学习功法对象
     *
     * @param skill 要学习的功法
     * @return 已学习功法对象
     */
    fun learnSkill(skill: Skill): SkillLearned {
        return skillLearningSystem.learnSkill(skill)
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
        return skillLearningSystem.calculateLearningSuccessRate(skill, talent)
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
        return skillLearningSystem.calculateLearningTime(skill, talent)
    }
}
