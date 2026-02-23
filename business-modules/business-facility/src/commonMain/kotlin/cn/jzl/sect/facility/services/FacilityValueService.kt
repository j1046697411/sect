/**
 * 设施价值服务
 *
 * 提供设施价值评估和投资分析功能：
 * - 设施综合价值计算
 * - 投资回报率(ROI)计算
 * - 回收期计算
 * - 战略价值评估
 * - 价值报告生成
 *
 * 使用方式：
 * ```kotlin
 * val facilityValueService by world.di.instance<FacilityValueService>()
 *
 * // 计算设施综合价值
 * val value = facilityValueService.calculateFacilityValue(
 *     constructionCost = 1000,
 *     maintenanceCost = 50,
 *     productionEfficiency = 1.2,
 *     facilityType = FacilityType.CULTIVATION_ROOM
 * )
 *
 * // 计算投资回报率
 * val roi = facilityValueService.calculateROI(constructionCost, dailyRevenue, dailyMaintenance)
 *
 * // 生成价值报告
 * val report = facilityValueService.generateValueReport(facilityName, value, roi, paybackPeriod)
 * ```
 */
package cn.jzl.sect.facility.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.facility.systems.FacilityValueCalculator

/**
 * 设施价值服务
 *
 * 代理 [FacilityValueCalculator] 的功能，提供设施价值评估和投资分析服务
 *
 * @property world ECS 世界实例
 */
class FacilityValueService(override val world: World) : EntityRelationContext {

    private val facilityValueCalculator by lazy {
        FacilityValueCalculator()
    }

    /**
     * 计算设施综合价值
     *
     * @param constructionCost 建设成本
     * @param maintenanceCost 维护成本
     * @param productionEfficiency 产出效率(1.0为基准)
     * @param facilityType 设施类型
     * @return 综合价值评分
     */
    fun calculateFacilityValue(
        constructionCost: Int,
        maintenanceCost: Int,
        productionEfficiency: Double,
        facilityType: FacilityType = FacilityType.CULTIVATION_ROOM
    ): Int {
        return facilityValueCalculator.calculateFacilityValue(
            constructionCost,
            maintenanceCost,
            productionEfficiency,
            facilityType
        )
    }

    /**
     * 计算投资回报率(ROI)
     *
     * @param constructionCost 建设成本
     * @param dailyRevenue 日收益
     * @param dailyMaintenance 日维护成本
     * @return ROI百分比(0.1 = 10%)
     */
    fun calculateROI(
        constructionCost: Int,
        dailyRevenue: Int,
        dailyMaintenance: Int
    ): Double {
        return facilityValueCalculator.calculateROI(constructionCost, dailyRevenue, dailyMaintenance)
    }

    /**
     * 计算回收期(天数)
     *
     * @param constructionCost 建设成本
     * @param dailyRevenue 日收益
     * @param dailyMaintenance 日维护成本
     * @return 回收天数
     */
    fun calculatePaybackPeriod(
        constructionCost: Int,
        dailyRevenue: Int,
        dailyMaintenance: Int
    ): Int {
        return facilityValueCalculator.calculatePaybackPeriod(constructionCost, dailyRevenue, dailyMaintenance)
    }

    /**
     * 计算战略价值
     *
     * @param facilityType 设施类型
     * @return 战略价值评分(0-100)
     */
    fun calculateStrategicValue(facilityType: FacilityType): Int {
        return facilityValueCalculator.calculateStrategicValue(facilityType)
    }

    /**
     * 评估设施价值等级
     *
     * @param value 价值评分
     * @return 价值等级
     */
    fun assessValueLevel(value: Int): FacilityValueCalculator.FacilityValueLevel {
        return facilityValueCalculator.assessValueLevel(value)
    }

    /**
     * 比较两个设施的价值
     *
     * @param value1 设施1价值
     * @param value2 设施2价值
     * @return 正数表示设施1更好，负数表示设施2更好
     */
    fun compareFacilities(value1: Int, value2: Int): Int {
        return facilityValueCalculator.compareFacilities(value1, value2)
    }

    /**
     * 生成设施价值报告
     *
     * @param facilityName 设施名称
     * @param value 价值评分
     * @param roi 投资回报率
     * @param paybackPeriod 回收期
     * @return 价值报告
     */
    fun generateValueReport(
        facilityName: String,
        value: Int,
        roi: Double,
        paybackPeriod: Int
    ): FacilityValueCalculator.ValueReport {
        return facilityValueCalculator.generateValueReport(facilityName, value, roi, paybackPeriod)
    }
}
