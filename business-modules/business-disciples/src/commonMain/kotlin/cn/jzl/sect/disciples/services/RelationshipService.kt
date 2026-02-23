/**
 * 关系服务
 *
 * 提供角色关系管理功能：
 * - 建立和解除关系
 * - 查询角色间关系
 * - 改善或恶化关系
 */
package cn.jzl.sect.disciples.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.core.relation.Relationship
import cn.jzl.sect.core.relation.RelationshipType
import cn.jzl.sect.disciples.systems.RelationshipSystem

/**
 * 关系服务
 *
 * 提供角色关系管理功能的核心服务：
 * - 建立和解除关系
 * - 查询角色间关系
 * - 改善或恶化关系
 * - 获取关系效果加成
 *
 * 使用方式：
 * ```kotlin
 * val relationshipService by world.di.instance<RelationshipService>()
 *
 * // 建立关系
 * relationshipService.establishRelationship(sourceId, targetId, RelationshipType.FRIEND)
 *
 * // 查询关系
 * val relationships = relationshipService.getRelationships(entityId)
 *
 * // 改善关系
 * relationshipService.improveRelationship(sourceId, targetId, 10)
 * ```
 */
class RelationshipService : EntityRelationContext {

    override lateinit var world: World

    internal val relationshipSystem by lazy {
        RelationshipSystem()
    }

    /**
     * 建立关系
     *
     * @param sourceId 关系发起者ID
     * @param targetId 关系目标ID
     * @param type 关系类型
     * @param level 初始关系等级(0-100)，默认为50
     * @param establishedTime 建立时间戳，默认为当前时间
     * @return 创建的关系对象
     */
    fun establishRelationship(
        sourceId: Long,
        targetId: Long,
        type: RelationshipType,
        level: Int = 50,
        establishedTime: Long = System.currentTimeMillis()
    ): Relationship {
        return relationshipSystem.establishRelationship(sourceId, targetId, type, level, establishedTime)
    }

    /**
     * 解除关系
     *
     * @param sourceId 关系发起者ID
     * @param targetId 关系目标ID
     */
    fun dissolveRelationship(sourceId: Long, targetId: Long) {
        relationshipSystem.dissolveRelationship(sourceId, targetId)
    }

    /**
     * 获取角色的所有关系
     *
     * @param entityId 角色ID
     * @return 关系列表
     */
    fun getRelationships(entityId: Long): List<Relationship> {
        return relationshipSystem.getRelationships(entityId)
    }

    /**
     * 获取特定类型的关系
     *
     * @param entityId 角色ID
     * @param type 关系类型
     * @return 关系列表
     */
    fun getRelationshipsByType(entityId: Long, type: RelationshipType): List<Relationship> {
        return relationshipSystem.getRelationshipsByType(entityId, type)
    }

    /**
     * 获取所有特定类型的关系（不限制sourceId）
     *
     * @param type 关系类型
     * @return 关系列表
     */
    fun getAllRelationshipsByType(type: RelationshipType): List<Relationship> {
        return relationshipSystem.getAllRelationshipsByType(type)
    }

    /**
     * 获取两个角色之间的关系
     *
     * @param sourceId 关系发起者ID
     * @param targetId 关系目标ID
     * @return 关系对象，如果不存在返回null
     */
    fun getRelationship(sourceId: Long, targetId: Long): Relationship? {
        return relationshipSystem.getRelationship(sourceId, targetId)
    }

    /**
     * 获取关系等级
     *
     * @param sourceId 关系发起者ID
     * @param targetId 关系目标ID
     * @return 关系等级，如果不存在返回0
     */
    fun getRelationshipLevel(sourceId: Long, targetId: Long): Int {
        return relationshipSystem.getRelationshipLevel(sourceId, targetId)
    }

    /**
     * 改善关系
     *
     * @param sourceId 关系发起者ID
     * @param targetId 关系目标ID
     * @param amount 增加的等级
     */
    fun improveRelationship(sourceId: Long, targetId: Long, amount: Int) {
        relationshipSystem.improveRelationship(sourceId, targetId, amount)
    }

    /**
     * 恶化关系
     *
     * @param sourceId 关系发起者ID
     * @param targetId 关系目标ID
     * @param amount 降低的等级
     */
    fun worsenRelationship(sourceId: Long, targetId: Long, amount: Int) {
        relationshipSystem.worsenRelationship(sourceId, targetId, amount)
    }

    /**
     * 获取关系效果加成
     *
     * @param sourceId 关系发起者ID
     * @param targetId 关系目标ID
     * @return 加成值，如果不存在返回0
     */
    fun getRelationshipEffectBonus(sourceId: Long, targetId: Long): Int {
        return relationshipSystem.getRelationshipEffectBonus(sourceId, targetId)
    }

    /**
     * 检查是否存在关系
     *
     * @param sourceId 关系发起者ID
     * @param targetId 关系目标ID
     * @return 是否存在关系
     */
    fun hasRelationship(sourceId: Long, targetId: Long): Boolean {
        return relationshipSystem.hasRelationship(sourceId, targetId)
    }

    /**
     * 清除所有关系
     */
    fun clearAll() {
        relationshipSystem.clearAll()
    }
}
