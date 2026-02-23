/**
 * 时间系统模块
 *
 * 提供游戏时间管理功能，包括：
 * - 游戏时间追踪与累加
 * - 全局时间服务访问接口
 * - 基于 ECS 的时间组件
 *
 * 使用方式：
 * ```kotlin
 * world.install(timeAddon)
 * val timeService by world.di.instance<TimeService>()
 * val currentTime = timeService.getCurrentGameTime()
 * ```
 */
package cn.jzl.sect.common.time

import cn.jzl.di.instance
import cn.jzl.di.new
import cn.jzl.di.singleton
import cn.jzl.ecs.Updatable
import cn.jzl.ecs.World
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.addon.entities
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.firstOrNull
import cn.jzl.ecs.query.forEach
import cn.jzl.ecs.query.map
import kotlin.jvm.JvmInline
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 时间系统 Addon
 *
 * 负责注册时间相关组件和服务：
 * - [Timer] 组件：存储游戏时间
 * - [TimeService] 服务：提供时间访问接口
 *
 * 初始化时自动创建一个全局计时器实体
 */
val timeAddon = createAddon("timeAddon") {
    injects {
        this bind singleton { new(::TimeService) }
    }
    components {
        world.componentId<Timer>()
    }
    entities {
        world.entity {
            it.addComponent(Timer(0.seconds))
        }
    }
}

/**
 * 计时器组件
 *
 * 存储游戏运行时间的值类型组件。
 * 使用 [JvmInline] 避免对象分配开销。
 *
 * @property duration 游戏运行时长
 */
@JvmInline
value class Timer(val duration: Duration)

/**
 * 时间服务
 *
 * 提供游戏时间管理功能的核心服务：
 * - 获取当前游戏时间
 * - 每帧更新游戏时间
 *
 * 使用方式：
 * ```kotlin
 * val timeService by world.di.instance<TimeService>()
 * val currentTime = timeService.getCurrentGameTime()
 * ```
 *
 * @property world ECS 世界实例
 */
class TimeService(override val world: World) : EntityRelationContext, Updatable {

    private val timers = world.query { TimeContext(this) }

    /**
     * 获取当前游戏时间
     *
     * @return 当前游戏运行时长
     * @throws NullPointerException 如果计时器实体不存在
     */
    fun getCurrentGameTime(): Duration {
        return timers.map { it.timer }.firstOrNull()?.duration ?: throw NullPointerException("Timer entity not found")
    }

    /**
     * 更新游戏时间
     *
     * 每帧调用，累加帧间隔时间到游戏总时长
     *
     * @param delta 帧间隔时间
     */
    override fun update(delta: Duration) {
        timers.forEach { it.timer = Timer(it.timer.duration + delta) }
    }

    private class TimeContext(world: World) : EntityQueryContext(world) {
        var timer: Timer by component()
    }
}
