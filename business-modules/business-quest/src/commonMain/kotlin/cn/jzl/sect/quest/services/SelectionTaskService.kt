/**
 * 选拔任务服务
 *
 * 提供选拔任务管理功能：
 * - 检查选拔周期是否到达
 * - 计算选拔名额
 * - 创建选拔任务
 */
package cn.jzl.sect.quest.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.quest.systems.SelectionTaskSystem

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

    private val selectionTaskSystem by lazy {
        SelectionTaskSystem(world)
    }

    /**
     * 检查选拔周期是否到达
     *
     * @param currentYear 当前年份
     * @param lastSelectionYear 上次选拔年份
     * @param cycleYears 选拔周期（年）
     * @return 是否到达选拔时间
     */
    fun checkSelectionCycle(currentYear: Int, lastSelectionYear: Int, cycleYears: Int): Boolean {
        return selectionTaskSystem.checkSelectionCycle(currentYear, lastSelectionYear, cycleYears)
    }

    /**
     * 计算选拔名额
     *
     * @param outerDiscipleCount 外门弟子数量
     * @param ratio 选拔比例
     * @return 选拔名额数
     */
    fun calculateSelectionQuota(outerDiscipleCount: Int, ratio: Double): Int {
        return selectionTaskSystem.calculateSelectionQuota(outerDiscipleCount, ratio)
    }

    /**
     * 创建选拔任务
     *
     * @param quota 选拔名额
     * @return 创建的任务实体
     */
    fun createSelectionTask(quota: Int): Entity {
        return selectionTaskSystem.createSelectionTask(world, quota)
    }
}
