package cn.jzl.sect.cultivation.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.ai.BehaviorState
import cn.jzl.sect.core.ai.BehaviorType
import cn.jzl.sect.core.cultivation.Cultivation
import cn.jzl.sect.core.disciple.Attribute

/**
 * 简单行为系统 - 根据状态决定弟子行为（修炼/休息/工作）
 */
class SimpleBehaviorSystem(private val world: World) {

    /**
     * 更新实体行为
     * @param dt 时间增量
     */
    fun update(dt: Float) {
        val query = world.query { BehaviorQueryContext(this) }

        query.forEach { ctx ->
            val behavior = ctx.behaviorState
            val attr = ctx.attribute
            val cult = ctx.cultivation

            // 根据状态决定行为
            val newBehavior = when {
                // 精神力 >= 30% 且生命值 >= 30% -> 修炼
                attr.spirit >= attr.maxSpirit * 0.3 && attr.health >= attr.maxHealth * 0.3 -> {
                    BehaviorType.CULTIVATE
                }
                // 生命值 < 30% -> 休息
                attr.health < attr.maxHealth * 0.3 -> {
                    BehaviorType.REST
                }
                // 其他情况 -> 工作
                else -> {
                    BehaviorType.WORK
                }
            }

            // 执行行为效果
            when (newBehavior) {
                BehaviorType.CULTIVATE -> {
                    // 修炼：消耗精神力，增加修为（由 CultivationSystem 处理）
                    val newSpirit = (attr.spirit - 5).coerceAtLeast(0)
                    world.editor(ctx.entity) {
                        it.addComponent(attr.copy(spirit = newSpirit))
                    }
                }
                BehaviorType.REST -> {
                    // 休息：恢复生命值和精神力
                    val newHealth = (attr.health + 10).coerceAtMost(attr.maxHealth)
                    val newSpirit = (attr.spirit + 5).coerceAtMost(attr.maxSpirit)
                    world.editor(ctx.entity) {
                        it.addComponent(attr.copy(health = newHealth, spirit = newSpirit))
                    }
                }
                BehaviorType.WORK -> {
                    // 工作：恢复生命值，消耗少量精神力
                    val newHealth = (attr.health + 5).coerceAtMost(attr.maxHealth)
                    val newSpirit = (attr.spirit - 2).coerceAtLeast(0)
                    world.editor(ctx.entity) {
                        it.addComponent(attr.copy(health = newHealth, spirit = newSpirit))
                    }
                }
                BehaviorType.SOCIAL -> {
                    // 社交：消耗精神力，增加心情（心情系统待实现）
                    val newSpirit = (attr.spirit - 3).coerceAtLeast(0)
                    world.editor(ctx.entity) {
                        it.addComponent(attr.copy(spirit = newSpirit))
                    }
                }
            }

            // 更新行为状态
            if (newBehavior != behavior.currentBehavior) {
                world.editor(ctx.entity) {
                    it.addComponent(
                        BehaviorState(
                            currentBehavior = newBehavior,
                            behaviorStartTime = System.currentTimeMillis(),
                            lastBehaviorTime = behavior.behaviorStartTime
                        )
                    )
                }
            }
        }
    }

    /**
     * 查询上下文 - 行为实体
     */
    class BehaviorQueryContext(world: World) : EntityQueryContext(world) {
        val behaviorState: BehaviorState by component()
        val attribute: Attribute by component()
        val cultivation: Cultivation by component()
    }
}
