/**
 * 关系服务
 *
 * 提供角色关系管理功能：
 * - 建立和解除关系
 * - 查询角色间关系
 * - 改善或恶化关系
 */
package cn.jzl.sect.disciples.services

import cn.jzl.di.instance
import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.log.Logger
import cn.jzl.sect.disciples.components.Relationship
import cn.jzl.sect.disciples.components.RelationshipType
import kotlin.time.Clock

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
class RelationshipService(override val world: World) : EntityRelationContext {

    private val log: Logger = cn.jzl.core.log.ConsoleLogger(cn.jzl.core.log.LogLevel.DEBUG, "RelationshipService")

    // 存储所有关系，使用Pair(sourceId, targetId)作为键
    private val relationships = mutableMapOf<Pair<Long, Long>, Relationship>()

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
        establishedTime: Long = Clock.System.now().toEpochMilliseconds()
    ): Relationship {
        log.debug { "建立关系: sourceId=$sourceId, targetId=$targetId, type=$type, level=$level" }
        val relationship = Relationship(
            sourceId = sourceId,
            targetId = targetId,
            type = type,
            level = level,
            establishedTime = establishedTime
        )
        relationships[Pair(sourceId, targetId)] = relationship
        log.debug { "关系建立完成: ${relationships.size} 个关系" }
        return relationship
    }

    /**
     * 解除关系
     *
     * @param sourceId 关系发起者ID
     * @param targetId 关系目标ID
     */
    fun dissolveRelationship(sourceId: Long, targetId: Long) {
        log.debug { "解除关系: sourceId=$sourceId, targetId=$targetId" }
        relationships.remove(Pair(sourceId, targetId))
        log.debug { "关系解除完成: ${relationships.size} 个关系" }
    }

    /**
     * 获取角色的所有关系
     *
     * @param entityId 角色ID
     * @return 关系列表
     */
    fun getRelationships(entityId: Long): List<Relationship> {
        log.debug { "获取角色关系: entityId=$entityId" }
        val result = relationships.values.filter { it.sourceId == entityId }
        log.debug { "获取角色关系完成: ${result.size} 个关系" }
        return result
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
        log.debug { "改善关系: sourceId=$sourceId, targetId=$targetId, amount=$amount" }
        val key = Pair(sourceId, targetId)
        relationships[key]?.let { relationship ->
            relationships[key] = relationship.improve(amount)
            log.debug { "关系改善完成: 新等级 ${relationships[key]?.level}" }
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
        log.debug { "恶化关系: sourceId=$sourceId, targetId=$targetId, amount=$amount" }
        val key = Pair(sourceId, targetId)
        relationships[key]?.let { relationship ->
            relationships[key] = relationship.worsen(amount)
            log.debug { "关系恶化完成: 新等级 ${relationships[key]?.level}" }
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
