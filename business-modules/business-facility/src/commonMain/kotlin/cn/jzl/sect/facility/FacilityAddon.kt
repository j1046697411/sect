/**
 * 设施系统 Addon
 *
 * 提供宗门设施管理功能，包括：
 * - 宗门状态检测（正常/警告/危急/解散）
 * - 设施价值评估（建设成本、维护成本、产出效率、战略价值）
 * - 设施使用管理（使用效果、使用成本、功能描述）
 *
 * 使用方式：
 * ```kotlin
 * world.install(facilityAddon)
 * val sectStatusService by world.di.instance<SectStatusService>()
 * val facilityValueService by world.di.instance<FacilityValueService>()
 * val facilityUsageService by world.di.instance<FacilityUsageService>()
 *
 * // 检查宗门状态
 * val status = sectStatusService.checkSectStatus()
 *
 * // 获取财务摘要
 * val summary = sectStatusService.getFinancialSummary()
 *
 * // 计算设施价值
 * val value = facilityValueService.calculateFacilityValue(constructionCost, maintenanceCost, efficiency, type)
 *
 * // 获取设施效果
 * val effect = facilityUsageService.getFacilityEffect(facilityType)
 * ```
 */
package cn.jzl.sect.facility

import cn.jzl.di.new
import cn.jzl.di.singleton
import cn.jzl.ecs.addon.Phase
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.sect.facility.components.Facility
import cn.jzl.sect.facility.components.FacilityStatus
import cn.jzl.sect.facility.services.SectStatusService
import cn.jzl.sect.facility.services.FacilityValueService
import cn.jzl.sect.facility.services.FacilityUsageService

/**
 * 设施系统 Addon
 *
 * 负责注册设施系统相关组件和服务：
 * - [Facility] 组件：设施基础信息
 * - [FacilityStatus] 组件：设施状态
 * - [SectStatusService] 服务：宗门状态检测和财务摘要
 * - [FacilityValueService] 服务：设施价值评估和ROI计算
 * - [FacilityUsageService] 服务：设施使用效果和成本管理
 *
 * 示例：
 * ```kotlin
 * world.install(facilityAddon)
 * ```
 */
val facilityAddon = createAddon("facilityAddon") {
    // 依赖资源系统
    install(cn.jzl.sect.resource.resourceAddon)

    // 注册组件
    components {
        world.componentId<Facility>()
        world.componentId<FacilityStatus>()
    }

    // 注册服务
    injects {
        this bind singleton { new(::SectStatusService) }
        this bind singleton { new(::FacilityValueService) }
        this bind singleton { new(::FacilityUsageService) }
    }

    // 生命周期回调 - 模块启用时
    on(Phase.ENABLE) {
        // 设施系统初始化逻辑（如需）
    }
}
