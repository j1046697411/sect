/**
 * 团队组建服务
 *
 * 提供团队组建管理功能：
 * - 查找可用的长老
 * - 查找可用的内门弟子
 * - 查找可用的外门弟子
 * - 组建完整团队
 */
package cn.jzl.sect.quest.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.quest.systems.TeamFormationResult
import cn.jzl.sect.quest.systems.TeamFormationSystem

/**
 * 团队组建服务
 *
 * 提供团队组建管理功能的核心服务：
 * - 查找可用的长老
 * - 查找可用的内门弟子
 * - 查找可用的外门弟子
 * - 组建完整团队
 *
 * 使用方式：
 * ```kotlin
 * val teamFormationService by world.di.instance<TeamFormationService>()
 * val team = teamFormationService.formTeam(questId)
 * ```
 *
 * @property world ECS 世界实例
 */
class TeamFormationService(override val world: World) : EntityRelationContext {

    private val teamFormationSystem by lazy {
        TeamFormationSystem(world)
    }

    /**
     * 查找可用的长老
     *
     * @return 找到的长老实体的ID，如果没有则返回null
     */
    fun findAvailableElder(): Entity? {
        return teamFormationSystem.findAvailableElder(world)
    }

    /**
     * 查找可用的内门弟子
     *
     * @param min 最少人数（默认3）
     * @param max 最多人数（默认5）
     * @return 内门弟子实体列表，如果人数不足则返回空列表
     */
    fun findAvailableInnerDisciples(min: Int = 3, max: Int = 5): List<Entity> {
        return teamFormationSystem.findAvailableInnerDisciples(world, min, max)
    }

    /**
     * 查找可用的外门弟子
     *
     * @param min 最少人数（默认10）
     * @param max 最多人数（默认20）
     * @return 外门弟子实体列表，如果人数不足则返回空列表
     */
    fun findAvailableOuterDisciples(min: Int = 10, max: Int = 20): List<Entity> {
        return teamFormationSystem.findAvailableOuterDisciples(world, min, max)
    }

    /**
     * 组建团队
     *
     * @param questId 任务ID
     * @return 团队组建结果
     */
    fun formTeam(questId: Long): TeamFormationResult {
        return teamFormationSystem.formTeam(world, questId)
    }
}
