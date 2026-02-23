/**
 * 简单行为服务
 *
 * 提供弟子行为决策功能：
 * - 根据状态决定行为（修炼/休息/工作）
 * - 执行行为效果
 * - 更新行为状态
 */
package cn.jzl.sect.cultivation.services

import cn.jzl.ecs.World
import cn.jzl.ecs.Updatable
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.cultivation.systems.SimpleBehaviorSystem
import kotlin.time.Duration

/**
 * 简单行为服务
 *
 * 提供弟子行为决策功能的核心服务：
 * - 根据状态决定行为（修炼/休息/工作）
 * - 执行行为效果
 * - 更新行为状态
 *
 * 使用方式：
 * ```kotlin
 * val behaviorService by world.di.instance<SimpleBehaviorService>()
 * behaviorService.update(delta)
 * ```
 *
 * @property world ECS 世界实例
 */
class SimpleBehaviorService(override val world: World) : EntityRelationContext, Updatable {

    private val behaviorSystem by lazy {
        SimpleBehaviorSystem(world)
    }

    /**
     * 更新实体行为
     * @param delta 时间增量
     */
    override fun update(delta: Duration) {
        behaviorSystem.update(delta.inWholeSeconds.toFloat())
    }
}
