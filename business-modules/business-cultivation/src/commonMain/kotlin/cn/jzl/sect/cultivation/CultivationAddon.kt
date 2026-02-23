/**
 * 修炼系统 Addon
 *
 * 提供弟子修炼管理功能，包括：
 * - 修为增长计算
 * - 境界突破处理
 * - 简单行为决策（修炼/休息/工作）
 *
 * 使用方式：
 * ```kotlin
 * world.install(cultivationAddon)
 * val cultivationService by world.di.instance<CultivationService>()
 * val behaviorService by world.di.instance<SimpleBehaviorService>()
 *
 * // 更新修炼状态
 * val breakthroughs = cultivationService.update(hours)
 *
 * // 更新行为状态
 * behaviorService.update(dt)
 * ```
 */
package cn.jzl.sect.cultivation

import cn.jzl.di.new
import cn.jzl.di.singleton
import cn.jzl.ecs.Updatable
import cn.jzl.ecs.addon.Phase
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.sect.cultivation.components.CultivationProgress
import cn.jzl.sect.cultivation.components.Talent
import cn.jzl.sect.cultivation.events.BreakthroughFailedEvent
import cn.jzl.sect.cultivation.events.BreakthroughSuccessEvent
import cn.jzl.sect.cultivation.services.CultivationService
import cn.jzl.sect.cultivation.services.SimpleBehaviorService

/**
 * 修炼系统 Addon
 *
 * 负责注册修炼系统相关组件和服务：
 * - [CultivationProgress] 组件：修炼进度
 * - [Talent] 组件：弟子天赋
 * - [CultivationService] 服务：处理修为增长和境界突破
 * - [SimpleBehaviorService] 服务：处理弟子行为决策
 *
 * 示例：
 * ```kotlin
 * world.install(cultivationAddon)
 * ```
 */
val cultivationAddon = createAddon("cultivationAddon") {
    // 依赖资源系统
    install(cn.jzl.sect.resource.resourceAddon)

    // 注册组件
    components {
        world.componentId<CultivationProgress>()
        world.componentId<Talent>()
        world.componentId<BreakthroughSuccessEvent>()
        world.componentId<BreakthroughFailedEvent>()
    }

    // 注册服务
    injects {
        this bind singleton { new(::CultivationService) }
        this bind singleton { new(::SimpleBehaviorService) }
    }

    // 生命周期回调 - 模块启用时
    on(Phase.ENABLE) {
        // 修炼系统初始化逻辑（如需）
    }
}
