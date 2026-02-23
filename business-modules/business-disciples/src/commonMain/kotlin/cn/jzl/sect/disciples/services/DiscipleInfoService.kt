/**
 * 弟子信息服务
 *
 * 提供弟子信息查询和管理功能：
 * - 获取所有弟子列表
 * - 按职位筛选弟子
 * - 获取弟子统计信息
 */
package cn.jzl.sect.disciples.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.disciples.systems.DiscipleInfo
import cn.jzl.sect.disciples.systems.DiscipleInfoSystem
import cn.jzl.sect.disciples.systems.DiscipleStatistics

/**
 * 弟子信息服务
 *
 * 提供弟子信息查询和管理功能的核心服务：
 * - 获取所有弟子列表
 * - 按职位筛选弟子
 * - 获取弟子统计信息
 *
 * 使用方式：
 * ```kotlin
 * val discipleInfoService by world.di.instance<DiscipleInfoService>()
 * val disciples = discipleInfoService.getAllDisciples()
 * val statistics = discipleInfoService.getDiscipleStatistics()
 * ```
 *
 * @property world ECS 世界实例
 */
class DiscipleInfoService(override val world: World) : EntityRelationContext {

    private val discipleInfoSystem by lazy {
        DiscipleInfoSystem(world)
    }

    /**
     * 获取所有弟子列表
     *
     * @return 按职位排序的弟子信息列表
     */
    fun getAllDisciples(): List<DiscipleInfo> {
        return discipleInfoSystem.getAllDisciples()
    }

    /**
     * 按职位筛选弟子
     *
     * @param position 职位类型
     * @return 符合条件的弟子列表
     */
    fun getDisciplesByPosition(position: SectPositionType): List<DiscipleInfo> {
        return discipleInfoSystem.getDisciplesByPosition(position)
    }

    /**
     * 获取弟子统计信息
     *
     * @return 弟子统计数据
     */
    fun getDiscipleStatistics(): DiscipleStatistics {
        return discipleInfoSystem.getDiscipleStatistics()
    }
}
