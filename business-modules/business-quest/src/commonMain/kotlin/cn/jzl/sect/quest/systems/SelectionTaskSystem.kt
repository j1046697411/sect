package cn.jzl.sect.quest.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.entity
import cn.jzl.sect.core.quest.*

/**
 * 选拔任务系统 - 检测选拔周期、计算名额、生成选拔任务
 */
class SelectionTaskSystem(private val world: World) {

    private var nextQuestId: Long = 1

    /**
     * 检查选拔周期是否到达
     * @param currentYear 当前年份
     * @param lastSelectionYear 上次选拔年份
     * @param cycleYears 选拔周期（年）
     * @return 是否到达选拔时间
     */
    fun checkSelectionCycle(currentYear: Int, lastSelectionYear: Int, cycleYears: Int): Boolean {
        if (cycleYears <= 0) return true
        return (currentYear - lastSelectionYear) >= cycleYears
    }

    /**
     * 计算选拔名额
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
     * @param world ECS世界
     * @param quota 选拔名额
     * @return 创建的任务实体
     */
    fun createSelectionTask(world: World, quota: Int): Entity {
        val questId = generateQuestId()
        val currentTime = System.currentTimeMillis()

        return world.entity {
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
    }

    /**
     * 生成唯一的任务ID
     */
    private fun generateQuestId(): Long {
        return nextQuestId++
    }
}
