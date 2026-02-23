/**
 * 选拔任务服务
 *
 * 提供选拔任务管理功能：
 * - 检查选拔周期是否到达
 * - 计算选拔名额
 * - 创建选拔任务
 */
package cn.jzl.sect.quest.services

import cn.jzl.di.instance
import cn.jzl.ecs.World
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.entity
import cn.jzl.log.Logger
import cn.jzl.sect.quest.components.QuestComponent
import cn.jzl.sect.quest.components.QuestDifficulty
import cn.jzl.sect.quest.components.QuestExecutionComponent
import cn.jzl.sect.quest.components.QuestStatus
import cn.jzl.sect.quest.components.QuestType
import kotlin.time.Clock

/**
 * 选拔任务服务
 *
 * 提供选拔任务管理功能的核心服务：
 * - 检查选拔周期是否到达
 * - 计算选拔名额
 * - 创建选拔任务
 *
 * 使用方式：
 * ```kotlin
 * val selectionTaskService by world.di.instance<SelectionTaskService>()
 * val task = selectionTaskService.createSelectionTask(quota)
 * ```
 *
 * @property world ECS 世界实例
 */
class SelectionTaskService(override val world: World) : EntityRelationContext {

    private val log: Logger by world.di.instance(argProvider = { "SelectionTaskService" })
    private var nextQuestId: Long = 1

    /**
     * 检查选拔周期是否到达
     *
     * @param currentYear 当前年份
     * @param lastSelectionYear 上次选拔年份
     * @param cycleYears 选拔周期（年）
     * @return 是否到达选拔时间
     */
    fun checkSelectionCycle(currentYear: Int, lastSelectionYear: Int, cycleYears: Int): Boolean {
        log.debug { "开始检查选拔周期: currentYear=$currentYear, lastSelectionYear=$lastSelectionYear, cycleYears=$cycleYears" }
        if (cycleYears <= 0) {
            log.debug { "检查选拔周期完成: true (cycleYears <= 0)" }
            return true
        }
        val result = (currentYear - lastSelectionYear) >= cycleYears
        log.debug { "检查选拔周期完成: $result" }
        return result
    }

    /**
     * 计算选拔名额
     *
     * @param outerDiscipleCount 外门弟子数量
     * @param ratio 选拔比例
     * @return 选拔名额数
     */
    fun calculateSelectionQuota(outerDiscipleCount: Int, ratio: Double): Int {
        if (outerDiscipleCount <= 0) return 0
        val quota = (outerDiscipleCount * ratio).toInt()
        return quota.coerceAtLeast(1)
    }

    /**
     * 创建选拔任务
     *
     * @param quota 选拔名额
     * @return 创建的任务实体
     */
    fun createSelectionTask(quota: Int): Entity {
        log.debug { "开始创建选拔任务: quota=$quota" }
        val questId = generateQuestId()
        val currentTime = Clock.System.now().toEpochMilliseconds()

        val entity = world.entity {
            it.addComponent(
                QuestComponent(
                    questId = questId,
                    type = QuestType.RUIN_EXPLORATION,
                    difficulty = QuestDifficulty.NORMAL,
                    status = QuestStatus.PENDING_APPROVAL,
                    createdAt = currentTime,
                    maxParticipants = quota,
                    description = "外门弟子选拔任务：从外门弟子中选拔$quota 名优秀弟子晋升内门"
                )
            )
            it.addComponent(
                QuestExecutionComponent(
                    questId = questId,
                    elderId = 0L,
                    innerDiscipleIds = emptyList(),
                    outerDiscipleIds = emptyList(),
                    progress = 0.0f,
                    startTime = 0L,
                    estimatedEndTime = 0L
                )
            )
        }
        log.debug { "创建选拔任务完成: questId=$questId" }
        return entity
    }

    /**
     * 生成唯一的任务ID
     */
    private fun generateQuestId(): Long {
        return nextQuestId++
    }
}
