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

import cn.jzl.di.instance
import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.log.Logger
import cn.jzl.sect.core.facility.FacilityType
import kotlin.math.max

/**
 * 设施价值服务
 *
 * 直接实现设施价值评估和投资分析功能
 *
 * @property world ECS 世界实例
 */
class FacilityValueService(override val world: World) : EntityRelationContext {

    private val log: Logger by world.di.instance(argProvider = { "FacilityValueService" })

    companion object {
        // 建设成本权重
        const val CONSTRUCTION_COST_WEIGHT = 0.3

        // 维护成本权重
        const val MAINTENANCE_COST_WEIGHT = 0.2

        // 产出效率权重
        const val EFFICIENCY_WEIGHT = 0.3

        // 战略价值权重
        const val STRATEGIC_WEIGHT = 0.2
    }

    /**
     * 设施价值等级枚举
     */
    enum class FacilityValueLevel(val displayName: String, val minValue: Int) {
        BASIC("基础", 0),
        STANDARD("标准", 100),
        ADVANCED("高级", 250),
        PREMIUM("顶级", 500),
        LEGENDARY("传奇", 1000);

        companion object {
            fun fromValue(value: Int): FacilityValueLevel {
                return entries.reversed().find { value >= it.minValue } ?: BASIC
            }
        }
    }

    /**
     * 价值报告
     */
    data class ValueReport(
        val facilityName: String,
        val valueScore: Int,
        val valueLevel: FacilityValueLevel,
        val roi: Double,
        val paybackPeriod: Int,
        val recommendation: String
    )

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
        log.debug { "开始计算设施综合价值: $facilityType, 建设成本=$constructionCost" }
        // 建设成本评分(成本越高评分越低，但有一个上限)
        val constructionScore = max(0, 100 - constructionCost / 100)

        // 维护成本评分(维护成本越低越好)
        val maintenanceScore = max(0, 100 - maintenanceCost / 10)

        // 产出效率评分
        val efficiencyScore = (productionEfficiency * 100).toInt()

        // 战略价值评分
        val strategicScore = calculateStrategicValue(facilityType)

        // 加权计算综合价值
        val value = (
            constructionScore * CONSTRUCTION_COST_WEIGHT +
            maintenanceScore * MAINTENANCE_COST_WEIGHT +
            efficiencyScore * EFFICIENCY_WEIGHT +
            strategicScore * STRATEGIC_WEIGHT
        ).toInt()
        log.debug { "设施综合价值计算完成: $facilityType, 价值=$value" }
        return value
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
        log.debug { "开始计算投资回报率: 建设成本=$constructionCost, 日收益=$dailyRevenue" }
        if (constructionCost <= 0) return 0.0.also {
            log.debug { "投资回报率计算完成: 建设成本无效" }
        }

        val netDailyProfit = dailyRevenue - dailyMaintenance
        val roi = netDailyProfit.toDouble() / constructionCost
        log.debug { "投资回报率计算完成: ROI=$roi" }
        return roi
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
        log.debug { "开始计算回收期: 建设成本=$constructionCost, 日收益=$dailyRevenue" }
        val netDailyProfit = dailyRevenue - dailyMaintenance
        val paybackPeriod = if (netDailyProfit > 0) {
            (constructionCost / netDailyProfit).coerceAtLeast(1)
        } else {
            Int.MAX_VALUE // 无法回收
        }
        log.debug { "回收期计算完成: $paybackPeriod 天" }
        return paybackPeriod
    }

    /**
     * 计算战略价值
     *
     * @param facilityType 设施类型
     * @return 战略价值评分(0-100)
     */
    fun calculateStrategicValue(facilityType: FacilityType): Int {
        return when (facilityType) {
            FacilityType.CULTIVATION_ROOM -> 70    // 修炼室 - 高战略价值
            FacilityType.ALCHEMY_ROOM -> 75        // 炼丹房 - 高战略价值
            FacilityType.FORGE_ROOM -> 70          // 炼器室 - 高战略价值
            FacilityType.LIBRARY -> 65             // 藏书阁 - 中高战略价值
            FacilityType.WAREHOUSE -> 50           // 仓库 - 中等战略价值
            FacilityType.DORMITORY -> 45           // 宿舍 - 中等战略价值
            FacilityType.SPIRIT_STONE_MINE -> 80   // 灵石矿 - 最高战略价值
            FacilityType.CONTRIBUTION_HALL -> 75   // 贡献堂 - 高战略价值
        }
    }

    /**
     * 评估设施价值等级
     *
     * @param value 价值评分
     * @return 价值等级
     */
    fun assessValueLevel(value: Int): FacilityValueLevel {
        return FacilityValueLevel.fromValue(value)
    }

    /**
     * 比较两个设施的价值
     *
     * @param value1 设施1价值
     * @param value2 设施2价值
     * @return 正数表示设施1更好，负数表示设施2更好
     */
    fun compareFacilities(value1: Int, value2: Int): Int {
        return value1 - value2
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
    ): ValueReport {
        log.debug { "开始生成设施价值报告: $facilityName, 价值=$value" }
        val report = ValueReport(
            facilityName = facilityName,
            valueScore = value,
            valueLevel = assessValueLevel(value),
            roi = roi,
            paybackPeriod = paybackPeriod,
            recommendation = generateRecommendation(roi, paybackPeriod)
        )
        log.debug { "设施价值报告生成完成: $facilityName, 建议=${report.recommendation}" }
        return report
    }

    /**
     * 生成投资建议
     */
    private fun generateRecommendation(roi: Double, paybackPeriod: Int): String {
        return when {
            roi > 0.1 && paybackPeriod < 30 -> "强烈推荐"
            roi > 0.05 && paybackPeriod < 60 -> "推荐"
            roi > 0.02 && paybackPeriod < 100 -> "一般"
            else -> "不推荐"
        }
    }
}
