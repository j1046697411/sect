/**
 * 功法传承服务
 *
 * 提供功法传承管理功能：
 * - 检查传承条件（熟练度、境界差距）
 * - 传承功法创建徒弟的学习对象
 * - 计算传承成功率和声望奖励
 */
package cn.jzl.sect.skill.services

import cn.jzl.di.instance
import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.log.Logger
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.skill.components.Skill
import cn.jzl.sect.skill.components.SkillLearned
import cn.jzl.sect.skill.components.SkillRarity
import kotlin.time.Clock

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

    private val log: Logger by world.di.instance(argProvider = { "SkillInheritanceService" })

    companion object {
        // 传承所需的最低熟练度
        const val MIN_PROFICIENCY_FOR_INHERITANCE = 50

        // 允许的最大境界差距
        const val MAX_REALM_GAP = 2
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
        log.debug { "开始检查功法传承条件" }

        // 检查熟练度要求
        if (!learned.canInherit()) {
            log.debug { "功法传承条件检查失败：熟练度不足" }
            return false
        }

        // 检查师父境界是否足够
        if (masterRealm.level < skill.requiredRealm.level) {
            log.debug { "功法传承条件检查失败：师父境界不足" }
            return false
        }

        // 检查境界差距
        val realmGap = masterRealm.level - apprenticeRealm.level
        if (realmGap > MAX_REALM_GAP) {
            log.debug { "功法传承条件检查失败：境界差距过大" }
            return false
        }

        log.debug { "功法传承条件检查通过" }
        return true
    }

    /**
     * 传承功法
     * 创建徒弟的已学习功法对象
     *
     * @param skill 要传承的功法
     * @return 徒弟的已学习功法对象
     */
    fun inheritSkill(skill: Skill): SkillLearned {
        log.debug { "开始传承功法" }
        val learned = SkillLearned(
            skillId = skill.id,
            proficiency = 0, // 传承后熟练度从0开始
            learnedTime = Clock.System.now().toEpochMilliseconds()
        )
        log.debug { "功法传承完成" }
        return learned
    }

    /**
     * 计算师父获得的声望
     * 根据功法品级计算
     *
     * @param rarity 功法品级
     * @return 获得的声望值
     */
    fun calculateMasterReputation(rarity: SkillRarity): Int {
        log.debug { "开始计算师父声望" }
        val reputation = when (rarity) {
            SkillRarity.COMMON -> 10
            SkillRarity.UNCOMMON -> 20
            SkillRarity.RARE -> 35
            SkillRarity.EPIC -> 55
            SkillRarity.LEGENDARY -> 80
            SkillRarity.MYTHIC -> 110
            SkillRarity.DIVINE -> 150
        }
        log.debug { "师父声望计算完成" }
        return reputation
    }

    /**
     * 计算传承成功率
     * 基于师父的熟练度
     *
     * @param proficiency 师父的熟练度
     * @return 成功率(0.0 - 1.0)
     */
    fun calculateInheritanceSuccessRate(proficiency: Int): Double {
        // 基础成功率50% + 熟练度加成
        val baseRate = 0.5
        val proficiencyBonus = (proficiency - MIN_PROFICIENCY_FOR_INHERITANCE) / 100.0 * 0.5
        return (baseRate + proficiencyBonus).coerceIn(0.3, 0.95)
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
        // 徒弟获得师父熟练度的10%-20%，受成功率影响
        val baseTransfer = masterProficiency * 0.1
        val bonusTransfer = masterProficiency * 0.1 * successRate
        return (baseTransfer + bonusTransfer).toInt().coerceIn(0, 30)
    }

    /**
     * 获取传承所需的最低师父境界
     * 根据功法品级
     *
     * @param rarity 功法品级
     * @return 最低境界
     */
    fun getRequiredMasterRealm(rarity: SkillRarity): Realm {
        log.debug { "开始获取传承所需最低师父境界" }
        val realm = when (rarity) {
            SkillRarity.COMMON -> Realm.QI_REFINING
            SkillRarity.UNCOMMON -> Realm.QI_REFINING
            SkillRarity.RARE -> Realm.FOUNDATION
            SkillRarity.EPIC -> Realm.GOLDEN_CORE
            SkillRarity.LEGENDARY -> Realm.NASCENT_SOUL
            SkillRarity.MYTHIC -> Realm.SOUL_TRANSFORMATION
            SkillRarity.DIVINE -> Realm.TRIBULATION
        }
        log.debug { "传承所需最低师父境界获取完成" }
        return realm
    }
}
