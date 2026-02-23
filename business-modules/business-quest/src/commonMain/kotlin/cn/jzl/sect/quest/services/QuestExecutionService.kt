/**
 * 任务执行服务
 *
 * 提供任务执行管理功能：
 * - 计算任务成功率
 * - 计算伤亡人数
 * - 执行任务并返回结果
 */
package cn.jzl.sect.quest.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.core.quest.ExecutionResult
import cn.jzl.sect.core.quest.QuestDifficulty
import cn.jzl.sect.quest.systems.QuestExecutionSystem
import cn.jzl.sect.quest.systems.TeamFormationResult

/**
 * 任务执行服务
 *
 * 提供任务执行管理功能的核心服务：
 * - 计算任务成功率
 * - 计算伤亡人数
 * - 执行任务并返回结果
 *
 * 使用方式：
 * ```kotlin
 * val questExecutionService by world.di.instance<QuestExecutionService>()
 * val result = questExecutionService.executeQuest(questId)
 * ```
 *
 * @property world ECS 世界实例
 */
class QuestExecutionService(override val world: World) : EntityRelationContext {

    private val questExecutionSystem by lazy {
        QuestExecutionSystem(world)
    }

    /**
     * 计算任务成功率
     *
     * @param difficulty 任务难度
     * @param team 团队组成
     * @return 成功率（0.0 - 1.0）
     */
    fun calculateSuccessRate(difficulty: QuestDifficulty, team: TeamFormationResult): Double {
        return questExecutionSystem.calculateSuccessRate(difficulty, team)
    }

    /**
     * 计算伤亡人数
     *
     * @param outerDisciples 外门弟子列表
     * @param difficulty 任务难度
     * @return 伤亡人数
     */
    fun calculateCasualties(outerDisciples: List<cn.jzl.ecs.entity.Entity>, difficulty: QuestDifficulty): Int {
        return questExecutionSystem.calculateCasualties(outerDisciples, difficulty)
    }

    /**
     * 执行任务
     *
     * @param questId 任务ID
     * @return 执行结果
     */
    fun executeQuest(questId: Long): ExecutionResult {
        return questExecutionSystem.executeQuest(world, questId)
    }
}
