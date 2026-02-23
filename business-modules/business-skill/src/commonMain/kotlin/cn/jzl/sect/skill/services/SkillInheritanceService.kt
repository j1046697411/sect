/**
 * 功法传承服务
 *
 * 提供功法传承管理功能：
 * - 检查传承条件（熟练度、境界差距）
 * - 传承功法创建徒弟的学习对象
 * - 计算传承成功率和声望奖励
 */
package cn.jzl.sect.skill.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.skill.components.Skill
import cn.jzl.sect.skill.components.SkillLearned
import cn.jzl.sect.skill.components.SkillRarity
import cn.jzl.sect.skill.systems.SkillInheritanceSystem

/**
 * 功法传承服务
 *
 * 提供功法传承管理功能的核心服务：
 * - 检查传承条件（熟练度、境界差距）
 * - 传承功法创建徒弟的学习对象
 * - 计算传承成功率和声望奖励
 *
 * 使用方式：
 * ```kotlin
 * val skillInheritanceService by world.di.instance<SkillInheritanceService>()
 * val canInherit = skillInheritanceService.canInherit(skill, learned, masterRealm, apprenticeRealm)
 * val apprenticeLearned = skillInheritanceService.inheritSkill(skill)
 * ```
 *
 * @property world ECS 世界实例
 */
class SkillInheritanceService(override val world: World) : EntityRelationContext {

    private val skillInheritanceSystem by lazy {
        SkillInheritanceSystem()
    }

    /**
     * 检查是否可以传承功法
     *
     * @param skill 要传承的功法
     * @param learned 师父对该功法的掌握情况
     * @param masterRealm 师父境界
     * @param apprenticeRealm 徒弟境界
     * @return 是否可以传承
     */
    fun canInherit(
        skill: Skill,
        learned: SkillLearned,
        masterRealm: Realm,
        apprenticeRealm: Realm
    ): Boolean {
        return skillInheritanceSystem.canInherit(skill, learned, masterRealm, apprenticeRealm)
    }

    /**
     * 传承功法
     * 创建徒弟的已学习功法对象
     *
     * @param skill 要传承的功法
     * @return 徒弟的已学习功法对象
     */
    fun inheritSkill(skill: Skill): SkillLearned {
        return skillInheritanceSystem.inheritSkill(skill)
    }

    /**
     * 计算师父获得的声望
     * 根据功法品级计算
     *
     * @param rarity 功法品级
     * @return 获得的声望值
     */
    fun calculateMasterReputation(rarity: SkillRarity): Int {
        return skillInheritanceSystem.calculateMasterReputation(rarity)
    }

    /**
     * 计算传承成功率
     * 基于师父的熟练度
     *
     * @param proficiency 师父的熟练度
     * @return 成功率(0.0 - 1.0)
     */
    fun calculateInheritanceSuccessRate(proficiency: Int): Double {
        return skillInheritanceSystem.calculateInheritanceSuccessRate(proficiency)
    }

    /**
     * 计算传承后徒弟获得的初始熟练度
     * 基于师父的熟练度和成功率
     *
     * @param masterProficiency 师父熟练度
     * @param successRate 成功率
     * @return 徒弟初始熟练度
     */
    fun calculateApprenticeInitialProficiency(masterProficiency: Int, successRate: Double): Int {
        return skillInheritanceSystem.calculateApprenticeInitialProficiency(masterProficiency, successRate)
    }

    /**
     * 获取传承所需的最低师父境界
     * 根据功法品级
     *
     * @param rarity 功法品级
     * @return 最低境界
     */
    fun getRequiredMasterRealm(rarity: SkillRarity): Realm {
        return skillInheritanceSystem.getRequiredMasterRealm(rarity)
    }
}
