/**
 * 师徒服务
 *
 * 提供师徒关系管理功能：
 * - 拜师和收徒
 * - 查询师徒关系
 * - 获取师徒加成效果
 */
package cn.jzl.sect.disciples.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.disciples.systems.MasterApprenticeSystem

/**
 * 师徒服务
 *
 * 提供师徒关系管理功能的核心服务：
 * - 拜师和收徒
 * - 查询师徒关系
 * - 获取师徒加成效果（修炼效率、功法学习）
 * - 解除师徒关系
 *
 * 使用方式：
 * ```kotlin
 * val masterApprenticeService by world.di.instance<MasterApprenticeService>()
 *
 * // 拜师
 * val success = masterApprenticeService.apprenticeToMaster(apprenticeId, masterId)
 *
 * // 获取师父
 * val masterId = masterApprenticeService.getMaster(apprenticeId)
 *
 * // 获取徒弟列表
 * val apprentices = masterApprenticeService.getApprentices(masterId)
 *
 * // 获取修炼加成
 * val bonus = masterApprenticeService.getCultivationEfficiencyBonus(apprenticeId)
 * ```
 */
class MasterApprenticeService : EntityRelationContext {

    override lateinit var world: World

    private val relationshipService by lazy {
        RelationshipService()
    }

    private val masterApprenticeSystem by lazy {
        MasterApprenticeSystem(relationshipService.relationshipSystem)
    }

    /**
     * 拜师
     * 建立师徒关系
     *
     * @param apprenticeId 徒弟ID
     * @param masterId 师父ID
     * @return 是否成功建立关系（如果徒弟已有师父则返回false）
     */
    fun apprenticeToMaster(apprenticeId: Long, masterId: Long): Boolean {
        return masterApprenticeSystem.apprenticeToMaster(apprenticeId, masterId)
    }

    /**
     * 获取徒弟的师父
     *
     * @param apprenticeId 徒弟ID
     * @return 师父ID，如果没有师父返回null
     */
    fun getMaster(apprenticeId: Long): Long? {
        return masterApprenticeSystem.getMaster(apprenticeId)
    }

    /**
     * 获取师父的所有徒弟
     *
     * @param masterId 师父ID
     * @return 徒弟ID列表
     */
    fun getApprentices(masterId: Long): List<Long> {
        return masterApprenticeSystem.getApprentices(masterId)
    }

    /**
     * 解除师徒关系
     *
     * @param apprenticeId 徒弟ID
     * @param masterId 师父ID
     */
    fun dissolveMasterApprenticeRelationship(apprenticeId: Long, masterId: Long) {
        masterApprenticeSystem.dissolveMasterApprenticeRelationship(apprenticeId, masterId)
    }

    /**
     * 获取修炼效率加成
     * 师徒关系提供20%修炼效率加成
     *
     * @param apprenticeId 徒弟ID
     * @return 加成百分比
     */
    fun getCultivationEfficiencyBonus(apprenticeId: Long): Int {
        return masterApprenticeSystem.getCultivationEfficiencyBonus(apprenticeId)
    }

    /**
     * 获取功法学习加成
     * 师徒关系提供30%功法学习加成
     *
     * @param apprenticeId 徒弟ID
     * @return 加成百分比
     */
    fun getSkillLearningBonus(apprenticeId: Long): Int {
        return masterApprenticeSystem.getSkillLearningBonus(apprenticeId)
    }

    /**
     * 检查是否为师徒关系
     *
     * @param apprenticeId 徒弟ID
     * @param masterId 师父ID
     * @return 是否为师徒关系
     */
    fun isMasterApprenticeRelationship(apprenticeId: Long, masterId: Long): Boolean {
        return masterApprenticeSystem.isMasterApprenticeRelationship(apprenticeId, masterId)
    }

    companion object {
        // 师徒关系提供的修炼效率加成 (%)
        const val CULTIVATION_EFFICIENCY_BONUS = MasterApprenticeSystem.CULTIVATION_EFFICIENCY_BONUS

        // 师徒关系提供的功法学习加成 (%)
        const val SKILL_LEARNING_BONUS = MasterApprenticeSystem.SKILL_LEARNING_BONUS
    }
}
