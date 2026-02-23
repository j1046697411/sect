/**
 * 倒计时系统模块
 *
 * 提供实体倒计时功能，包括：
 * - 为实体设置倒计时
 * - 倒计时完成时触发事件
 * - 基于 ECS 的倒计时组件
 *
 * 使用方式：
 * ```kotlin
 * world.install(countdownAddon)
 * val countdownService by world.di.instance<CountdownService>()
 *
 * // 设置 5 秒倒计时
 * countdownService.countdown(entity, 5.seconds)
 *
 * // 监听倒计时完成事件
 * entity.observe<OnCountdownComplete>().exec {
 *     println("倒计时完成！")
 * }
 * ```
 */
package cn.jzl.sect.common.countdown

import cn.jzl.di.instance
import cn.jzl.di.new
import cn.jzl.di.singleton
import cn.jzl.ecs.Updatable
import cn.jzl.ecs.World
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.component.tag
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.observer.emit
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.filter
import cn.jzl.ecs.query.forEach
import cn.jzl.ecs.relation.component
import cn.jzl.ecs.relation.relations
import cn.jzl.sect.common.time.TimeService
import cn.jzl.sect.common.time.timeAddon
import kotlin.time.Duration

/**
 * 倒计时系统 Addon
 *
 * 负责注册倒计时相关组件和服务：
 * - [Countdown] 组件：存储倒计时数据
 * - [OnCountdownComplete] 标签：倒计时完成事件
 * - [CountdownService] 服务：提供倒计时管理功能
 *
 * 依赖 [timeAddon] 提供时间服务
 */
val countdownAddon = createAddon("countdownAddon") {
    install(timeAddon)
    injects { this bind singleton { new(::CountdownService) } }

    components {
        world.componentId<Countdown>()
        world.componentId<OnCountdownComplete> { it.tag() }
    }
}

/**
 * 倒计时组件
 *
 * 存储实体的倒计时信息
 *
 * @property startTime 倒计时开始时间（游戏时间）
 * @property interval 倒计时时长
 */
private data class Countdown(val startTime: Duration, val interval: Duration)

/**
 * 倒计时完成事件标签
 *
 * 当倒计时结束时，实体会触发此事件。
 * 可通过观察者模式监听：
 * ```kotlin
 * entity.observe<OnCountdownComplete>().exec {
 *     // 处理倒计时完成逻辑
 * }
 * ```
 */
sealed class OnCountdownComplete

/**
 * 倒计时服务
 *
 * 提供倒计时管理功能的核心服务：
 * - 为实体创建倒计时
 * - 每帧检查并触发完成的倒计时
 *
 * 使用方式：
 * ```kotlin
 * val countdownService by world.di.instance<CountdownService>()
 *
 * // 为实体设置 3 秒倒计时
 * countdownService.countdown(entity, 3.seconds)
 * ```
 *
 * @property world ECS 世界实例
 */
class CountdownService(override val world: World) : EntityRelationContext, Updatable {

    private val countdowns = world.query { CountdownContext(this) }
    private val timeService by world.di.instance<TimeService>()

    /**
     * 为实体设置倒计时
     *
     * @param entity 目标实体
     * @param interval 倒计时时长
     */
    fun countdown(entity: Entity, interval: Duration): Unit = entity.editor {
        it.addComponent(Countdown(timeService.getCurrentGameTime(), interval))
    }

    /**
     * 更新倒计时状态
     *
     * 每帧调用，检查所有倒计时：
     * - 如果倒计时完成，移除组件并触发 [OnCountdownComplete] 事件
     *
     * @param delta 帧间隔时间（未使用，倒计时基于游戏时间计算）
     */
    override fun update(delta: Duration) {
        val time = timeService.getCurrentGameTime()
        countdowns.filter { it.countdown.startTime + it.countdown.interval <= time }.forEach {
            it.removeRelation(relations.component<Countdown>())
            it.entity.emit<OnCountdownComplete>()
        }
    }

    private class CountdownContext(world: World) : EntityQueryContext(world) {
        val countdown: Countdown by component()
    }
}
