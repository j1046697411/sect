/**
 * 设施使用服务
 *
 * 提供设施使用管理功能：
 * - 设施使用效果查询
 * - 使用成本计算
 * - 使用权限检查
 * - 设施功能描述
 * - 预期收益计算
 *
 * 使用方式：
 * ```kotlin
 * val facilityUsageService by world.di.instance<FacilityUsageService>()
 *
 * // 获取设施使用效果
 * val effect = facilityUsageService.getFacilityEffect(FacilityType.CULTIVATION_ROOM)
 *
 * // 计算使用成本
 * val cost = facilityUsageService.calculateUsageCost(FacilityType.ALCHEMY_ROOM, duration = 2)
 *
 * // 检查是否可以使用
 * val canUse = facilityUsageService.canUseFacility(FacilityType.LIBRARY, contributionPoints = 100)
 * ```
 */
package cn.jzl.sect.facility.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.facility.systems.FacilityUsageSystem

/**
 * 设施使用服务
 *
 * 代理 [FacilityUsageSystem] 的功能，提供设施使用管理服务
 *
 * @property world ECS 世界实例
 */
class FacilityUsageService(override val world: World) : EntityRelationContext {

    private val facilityUsageSystem by lazy {
        FacilityUsageSystem()
    }

    /**
     * 获取设施使用效果
     *
     * @param facilityType 设施类型
     * @return 设施效果
     */
    fun getFacilityEffect(facilityType: FacilityType): FacilityUsageSystem.FacilityEffect {
        return facilityUsageSystem.getFacilityEffect(facilityType)
    }

    /**
     * 计算使用成本
     *
     * @param facilityType 设施类型
     * @param duration 使用时长(小时)
     * @return 使用成本(贡献点)
     */
    fun calculateUsageCost(facilityType: FacilityType, duration: Int): Int {
        return facilityUsageSystem.calculateUsageCost(facilityType, duration)
    }

    /**
     * 检查是否可以使用设施
     *
     * @param facilityType 设施类型
     * @param contributionPoints 拥有的贡献点
     * @param duration 使用时长
     * @return 是否可以使用
     */
    fun canUseFacility(
        facilityType: FacilityType,
        contributionPoints: Int,
        duration: Int = 1
    ): Boolean {
        return facilityUsageSystem.canUseFacility(facilityType, contributionPoints, duration)
    }

    /**
     * 获取设施功能描述
     *
     * @param facilityType 设施类型
     * @return 功能描述
     */
    fun getFacilityFunctionDescription(facilityType: FacilityType): String {
        return facilityUsageSystem.getFacilityFunctionDescription(facilityType)
    }

    /**
     * 获取设施使用提示
     *
     * @param facilityType 设施类型
     * @return 使用提示
     */
    fun getUsageTip(facilityType: FacilityType): String {
        return facilityUsageSystem.getUsageTip(facilityType)
    }

    /**
     * 计算设施使用收益
     *
     * @param facilityType 设施类型
     * @param userLevel 使用者等级
     * @param duration 使用时长
     * @return 预期收益
     */
    fun calculateExpectedBenefit(
        facilityType: FacilityType,
        userLevel: Int,
        duration: Int
    ): Int {
        return facilityUsageSystem.calculateExpectedBenefit(facilityType, userLevel, duration)
    }
}
