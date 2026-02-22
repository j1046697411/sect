package cn.jzl.sect.facility.systems

import cn.jzl.sect.core.facility.FacilityType
import kotlin.math.max

/**
 * 设施价值计算器
 * 评估设施的综合价值和投资回报率
 */
class FacilityValueCalculator {

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
        // 建设成本评分(成本越高评分越低，但有一个上限)
        val constructionScore = max(0, 100 - constructionCost / 100)

        // 维护成本评分(维护成本越低越好)
        val maintenanceScore = max(0, 100 - maintenanceCost / 10)

        // 产出效率评分
        val efficiencyScore = (productionEfficiency * 100).toInt()

        // 战略价值评分
        val strategicScore = calculateStrategicValue(facilityType)

        // 加权计算综合价值
        return (
            constructionScore * CONSTRUCTION_COST_WEIGHT +
            maintenanceScore * MAINTENANCE_COST_WEIGHT +
            efficiencyScore * EFFICIENCY_WEIGHT +
            strategicScore * STRATEGIC_WEIGHT
        ).toInt()
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
        if (constructionCost <= 0) return 0.0

        val netDailyProfit = dailyRevenue - dailyMaintenance
        return netDailyProfit.toDouble() / constructionCost
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
        val netDailyProfit = dailyRevenue - dailyMaintenance
        return if (netDailyProfit > 0) {
            (constructionCost / netDailyProfit).coerceAtLeast(1)
        } else {
            Int.MAX_VALUE // 无法回收
        }
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
        return ValueReport(
            facilityName = facilityName,
            valueScore = value,
            valueLevel = assessValueLevel(value),
            roi = roi,
            paybackPeriod = paybackPeriod,
            recommendation = generateRecommendation(roi, paybackPeriod)
        )
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
}
