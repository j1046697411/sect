package cn.jzl.sect.disciples.systems

import cn.jzl.sect.core.relation.Relationship
import cn.jzl.sect.core.relation.RelationshipType

/**
 * 关系系统
 * 管理角色之间的各种关系
 */
class RelationshipSystem {

    // 存储所有关系，使用Pair(sourceId, targetId)作为键
    private val relationships = mutableMapOf<Pair<Long, Long>, Relationship>()

    /**
     * 建立关系
     *
     * @param sourceId 关系发起者ID
     * @param targetId 关系目标ID
     * @param type 关系类型
     * @param level 初始关系等级(0-100)
     * @param establishedTime 建立时间戳
     * @return 创建的关系对象
     */
    fun establishRelationship(
        sourceId: Long,
        targetId: Long,
        type: RelationshipType,
        level: Int = 50,
        establishedTime: Long = System.currentTimeMillis()
    ): Relationship {
        val relationship = Relationship(
            sourceId = sourceId,
            targetId = targetId,
            type = type,
            level = level,
            establishedTime = establishedTime
        )
        relationships[Pair(sourceId, targetId)] = relationship
        return relationship
    }

    /**
     * 解除关系
     *
     * @param sourceId 关系发起者ID
     * @param targetId 关系目标ID
     */
    fun dissolveRelationship(sourceId: Long, targetId: Long) {
        relationships.remove(Pair(sourceId, targetId))
    }

    /**
     * 获取角色的所有关系
     *
     * @param entityId 角色ID
     * @return 关系列表
     */
    fun getRelationships(entityId: Long): List<Relationship> {
        return relationships.values.filter { it.sourceId == entityId }
    }

    /**
     * 获取特定类型的关系
     *
     * @param entityId 角色ID
     * @param type 关系类型
     * @return 关系列表
     */
    fun getRelationshipsByType(entityId: Long, type: RelationshipType): List<Relationship> {
        return relationships.values.filter { it.sourceId == entityId && it.type == type }
    }

    /**
     * 获取所有特定类型的关系（不限制sourceId）
     *
     * @param type 关系类型
     * @return 关系列表
     */
    fun getAllRelationshipsByType(type: RelationshipType): List<Relationship> {
        return relationships.values.filter { it.type == type }
    }

    /**
     * 获取两个角色之间的关系
     *
     * @param sourceId 关系发起者ID
     * @param targetId 关系目标ID
     * @return 关系对象，如果不存在返回null
     */
    fun getRelationship(sourceId: Long, targetId: Long): Relationship? {
        return relationships[Pair(sourceId, targetId)]
    }

    /**
     * 获取关系等级
     *
     * @param sourceId 关系发起者ID
     * @param targetId 关系目标ID
     * @return 关系等级，如果不存在返回0
     */
    fun getRelationshipLevel(sourceId: Long, targetId: Long): Int {
        return relationships[Pair(sourceId, targetId)]?.level ?: 0
    }

    /**
     * 改善关系
     *
     * @param sourceId 关系发起者ID
     * @param targetId 关系目标ID
     * @param amount 增加的等级
     */
    fun improveRelationship(sourceId: Long, targetId: Long, amount: Int) {
        val key = Pair(sourceId, targetId)
        relationships[key]?.let { relationship ->
            relationships[key] = relationship.improve(amount)
        }
    }

    /**
     * 恶化关系
     *
     * @param sourceId 关系发起者ID
     * @param targetId 关系目标ID
     * @param amount 降低的等级
     */
    fun worsenRelationship(sourceId: Long, targetId: Long, amount: Int) {
        val key = Pair(sourceId, targetId)
        relationships[key]?.let { relationship ->
            relationships[key] = relationship.worsen(amount)
        }
    }

    /**
     * 获取关系效果加成
     *
     * @param sourceId 关系发起者ID
     * @param targetId 关系目标ID
     * @return 加成值，如果不存在返回0
     */
    fun getRelationshipEffectBonus(sourceId: Long, targetId: Long): Int {
        return relationships[Pair(sourceId, targetId)]?.getEffectBonus() ?: 0
    }

    /**
     * 检查是否存在关系
     *
     * @param sourceId 关系发起者ID
     * @param targetId 关系目标ID
     * @return 是否存在关系
     */
    fun hasRelationship(sourceId: Long, targetId: Long): Boolean {
        return relationships.containsKey(Pair(sourceId, targetId))
    }

    /**
     * 清除所有关系
     */
    fun clearAll() {
        relationships.clear()
    }
}
