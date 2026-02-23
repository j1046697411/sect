/**
 * 资源系统 Addon
 *
 * 提供宗门资源管理功能，包括：
 * - 资源生产（灵脉、矿脉等产出）
 * - 资源消耗（俸禄发放、设施维护）
 * - 资源统计与查询
 *
 * 使用方式：
 * ```kotlin
 * world.install(resourceAddon)
 * val productionService by world.di.instance<ResourceProductionService>()
 * val consumptionService by world.di.instance<ResourceConsumptionService>()
 *
 * // 执行每日资源产出
 * val records = productionService.dailyProduction()
 *
 * // 执行月度资源消耗结算
 * val result = consumptionService.monthlyConsumption()
 * ```
 */
package cn.jzl.sect.resource

import cn.jzl.di.new
import cn.jzl.di.singleton
import cn.jzl.ecs.addon.Phase
import cn.jzl.ecs.addon.createAddon
import cn.jzl.sect.resource.services.ResourceConsumptionService
import cn.jzl.sect.resource.services.ResourceProductionService

/**
 * 资源系统 Addon
 *
 * 负责注册资源系统相关组件和服务：
 * - [ResourceProductionService] 服务：处理资源产出
 * - [ResourceConsumptionService] 服务：处理资源消耗
 *
 * 示例：
 * ```kotlin
 * world.install(resourceAddon)
 * ```
 */
val resourceAddon = createAddon("resourceAddon") {
    // 注册服务
    injects {
        this bind singleton { new(::ResourceProductionService) }
        this bind singleton { new(::ResourceConsumptionService) }
    }

    // 生命周期回调 - 模块启用时
    on(Phase.ENABLE) {
        // 资源系统初始化逻辑（如需）
    }
}
