/**
 * 建筑系统 Addon
 *
 * 提供宗门设施建造、产出和升级管理功能，包括：
 * - 设施建造管理
 * - 设施产出计算
 * - 设施升级管理
 *
 * 使用方式：
 * ```kotlin
 * world.install(buildingAddon)
 * val constructionService by world.di.instance<FacilityConstructionService>()
 * val productionService by world.di.instance<FacilityProductionService>()
 * val upgradeService by world.di.instance<FacilityUpgradeService>()
 *
 * // 建造设施
 * val result = constructionService.build("灵脉", FacilityType.SPIRIT_VEIN)
 *
 * // 计算总产出
 * val productions = productionService.calculateTotalProduction()
 *
 * // 升级设施
 * val upgradeResult = upgradeService.upgrade(facilityEntity)
 * ```
 */
package cn.jzl.sect.building

import cn.jzl.di.new
import cn.jzl.di.singleton
import cn.jzl.ecs.addon.Phase
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.sect.building.components.FacilityBuildProgress
import cn.jzl.sect.building.services.FacilityConstructionService
import cn.jzl.sect.building.services.FacilityProductionService
import cn.jzl.sect.building.services.FacilityUpgradeService

/**
 * 建筑系统 Addon
 *
 * 负责注册建筑系统相关组件和服务：
 * - [FacilityConstructionService] 服务：处理设施建造
 * - [FacilityProductionService] 服务：处理设施产出
 * - [FacilityUpgradeService] 服务：处理设施升级
 *
 * 示例：
 * ```kotlin
 * world.install(buildingAddon)
 * ```
 */
val buildingAddon = createAddon("buildingAddon") {
    // 依赖设施系统
    install(cn.jzl.sect.facility.facilityAddon)

    // 注册建筑系统相关组件
    components {
        world.componentId<FacilityBuildProgress>()
    }

    // 注册服务
    injects {
        this bind singleton { new(::FacilityConstructionService) }
        this bind singleton { new(::FacilityProductionService) }
        this bind singleton { new(::FacilityUpgradeService) }
    }

    // 生命周期回调 - 模块启用时
    on(Phase.ENABLE) {
        // 建筑系统初始化逻辑（如需）
    }
}
