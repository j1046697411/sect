/**
 * 修炼服务
 *
 * 提供弟子修炼管理功能：
 * - 修为增长计算
 * - 境界突破处理
 * - 突破事件通知
 */
package cn.jzl.sect.cultivation.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.cultivation.systems.CultivationSystem

/**
 * 修炼服务
 *
 * 提供弟子修炼管理功能的核心服务：
 * - 修为增长计算
 * - 境界突破处理
 * - 突破事件通知
 *
 * 使用方式：
 * ```kotlin
 * val cultivationService by world.di.instance<CultivationService>()
 * val breakthroughs = cultivationService.update(hours)
 * ```
 *
 * @property world ECS 世界实例
 */
class CultivationService(override val world: World) : EntityRelationContext {

    private val cultivationSystem by lazy {
        CultivationSystem(world)
    }

    /**
     * 更新修炼状态
     * @param hours 经过的游戏小时数
     * @return 突破事件列表
     */
    fun update(hours: Int): List<CultivationSystem.BreakthroughEvent> {
        return cultivationSystem.update(hours)
    }
}
