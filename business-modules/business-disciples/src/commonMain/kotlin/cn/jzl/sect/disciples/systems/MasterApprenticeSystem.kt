package cn.jzl.sect.disciples.systems

import cn.jzl.sect.core.relation.RelationshipType

/**
 * 师徒系统
 * 管理师父和徒弟之间的关系，提供师徒特有的功能
 *
 * @property relationshipSystem 关系系统实例
 */
class MasterApprenticeSystem(
    private val relationshipSystem: RelationshipSystem
) {

    companion object {
        // 师徒关系提供的修炼效率加成 (%)
        const val CULTIVATION_EFFICIENCY_BONUS = 20

        // 师徒关系提供的功法学习加成 (%)
        const val SKILL_LEARNING_BONUS = 30
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
        // 检查徒弟是否已有师父
        if (getMaster(apprenticeId) != null) {
            return false
        }

        // 建立师徒关系，初始等级为60
        relationshipSystem.establishRelationship(
            sourceId = apprenticeId,
            targetId = masterId,
            type = RelationshipType.MASTER_APPRENTICE,
            level = 60
        )

        return true
    }

    /**
     * 获取徒弟的师父
     *
     * @param apprenticeId 徒弟ID
     * @return 师父ID，如果没有师父返回null
     */
    fun getMaster(apprenticeId: Long): Long? {
        val relationships = relationshipSystem.getRelationshipsByType(
            apprenticeId,
            RelationshipType.MASTER_APPRENTICE
        )
        return relationships.firstOrNull()?.targetId
    }

    /**
     * 获取师父的所有徒弟
     *
     * @param masterId 师父ID
     * @return 徒弟ID列表
     */
    fun getApprentices(masterId: Long): List<Long> {
        // 遍历所有师徒关系，找到目标为masterId的关系
        // 注意：师徒关系是徒弟->师父存储的
        return relationshipSystem.getAllRelationshipsByType(RelationshipType.MASTER_APPRENTICE)
            .filter { it.targetId == masterId }
            .map { it.sourceId }
    }

    /**
     * 解除师徒关系
     *
     * @param apprenticeId 徒弟ID
     * @param masterId 师父ID
     */
    fun dissolveMasterApprenticeRelationship(apprenticeId: Long, masterId: Long) {
        relationshipSystem.dissolveRelationship(apprenticeId, masterId)
    }

    /**
     * 获取修炼效率加成
     * 师徒关系提供20%修炼效率加成
     *
     * @param apprenticeId 徒弟ID
     * @return 加成百分比
     */
    fun getCultivationEfficiencyBonus(apprenticeId: Long): Int {
        return if (getMaster(apprenticeId) != null) {
            CULTIVATION_EFFICIENCY_BONUS
        } else {
            0
        }
    }

    /**
     * 获取功法学习加成
     * 师徒关系提供30%功法学习加成
     *
     * @param apprenticeId 徒弟ID
     * @return 加成百分比
     */
    fun getSkillLearningBonus(apprenticeId: Long): Int {
        return if (getMaster(apprenticeId) != null) {
            SKILL_LEARNING_BONUS
        } else {
            0
        }
    }

    /**
     * 检查是否为师徒关系
     *
     * @param apprenticeId 徒弟ID
     * @param masterId 师父ID
     * @return 是否为师徒关系
     */
    fun isMasterApprenticeRelationship(apprenticeId: Long, masterId: Long): Boolean {
        val relationship = relationshipSystem.getRelationship(apprenticeId, masterId)
        return relationship?.type == RelationshipType.MASTER_APPRENTICE
    }
}
