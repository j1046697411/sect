/**
 * 简单行为服务
 *
 * 提供弟子行为决策功能：
 * - 根据状态决定行为（修炼/休息/工作）
 * - 执行行为效果
 * - 更新行为状态
 */
package cn.jzl.sect.cultivation.services

import cn.jzl.di.instance
import cn.jzl.ecs.World
import cn.jzl.ecs.Updatable
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.log.Logger
import cn.jzl.sect.core.ai.CurrentBehavior
import cn.jzl.sect.core.ai.BehaviorType
import cn.jzl.sect.core.vitality.Vitality
import cn.jzl.sect.core.vitality.Spirit
import cn.jzl.sect.cultivation.components.CultivationProgress
import kotlin.time.Duration
import kotlin.time.Clock

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

    private val log: Logger by world.di.instance(argProvider = { "SimpleBehaviorService" })

    /**
     * 更新实体行为
     * @param delta 时间增量
     */
    override fun update(delta: Duration) {
        log.debug { "开始更新弟子行为状态，时间增量: ${delta.inWholeSeconds}秒" }
        val dt = delta.inWholeSeconds.toFloat()
        val query = world.query { BehaviorQueryContext(this) }

        query.forEach { ctx ->
            val behavior = ctx.behavior
            val vitality = ctx.vitality
            val spirit = ctx.spirit

            // 根据状态决定行为
            val newBehavior = when {
                // 精神力 >= 30% 且生命值 >= 30% -> 修炼
                spirit.currentSpirit >= spirit.maxSpirit * 0.3 && vitality.currentHealth >= vitality.maxHealth * 0.3 -> {
                    BehaviorType.CULTIVATE
                }
                // 生命值 < 30% -> 休息
                vitality.currentHealth < vitality.maxHealth * 0.3 -> {
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
                    val newSpiritValue = (spirit.currentSpirit - 5).coerceAtLeast(0)
                    world.editor(ctx.entity) {
                        it.addComponent(spirit.copy(currentSpirit = newSpiritValue))
                    }
                }
                BehaviorType.REST -> {
                    // 休息：恢复生命值和精神力
                    val newHealth = (vitality.currentHealth + 10).coerceAtMost(vitality.maxHealth)
                    val newSpiritValue = (spirit.currentSpirit + 5).coerceAtMost(spirit.maxSpirit)
                    world.editor(ctx.entity) {
                        it.addComponent(vitality.copy(currentHealth = newHealth))
                        it.addComponent(spirit.copy(currentSpirit = newSpiritValue))
                    }
                }
                BehaviorType.WORK -> {
                    // 工作：恢复生命值，消耗少量精神力
                    val newHealth = (vitality.currentHealth + 5).coerceAtMost(vitality.maxHealth)
                    val newSpiritValue = (spirit.currentSpirit - 2).coerceAtLeast(0)
                    world.editor(ctx.entity) {
                        it.addComponent(vitality.copy(currentHealth = newHealth))
                        it.addComponent(spirit.copy(currentSpirit = newSpiritValue))
                    }
                }
                BehaviorType.SOCIAL -> {
                    // 社交：消耗精神力，增加心情（心情系统待实现）
                    val newSpiritValue = (spirit.currentSpirit - 3).coerceAtLeast(0)
                    world.editor(ctx.entity) {
                        it.addComponent(spirit.copy(currentSpirit = newSpiritValue))
                    }
                }
            }

            // 更新行为状态
            if (newBehavior != behavior.type) {
                log.debug { "弟子行为切换: ${behavior.type.displayName} → ${newBehavior.displayName}" }
                world.editor(ctx.entity) {
                    it.addComponent(
                        CurrentBehavior(
                            type = newBehavior,
                            startTime = Clock.System.now().toEpochMilliseconds(),
                            lastBehaviorTime = behavior.startTime
                        )
                    )
                }
            }
        }
        log.debug { "弟子行为状态更新完成" }
    }

    /**
     * 查询上下文 - 行为实体
     */
    class BehaviorQueryContext(world: World) : EntityQueryContext(world) {
        val behavior: CurrentBehavior by component()
        val vitality: Vitality by component()
        val spirit: Spirit by component()
        val cultivation: CultivationProgress by component()
    }
}

/**
 * 行为类型显示名称扩展
 */
private val BehaviorType.displayName: String
    get() = when (this) {
        BehaviorType.CULTIVATE -> "修炼"
        BehaviorType.REST -> "休息"
        BehaviorType.WORK -> "工作"
        BehaviorType.SOCIAL -> "社交"
    }
