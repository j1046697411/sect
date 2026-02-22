package cn.jzl.sect.facility.systems

import cn.jzl.sect.core.facility.FacilityType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 设施价值计算器测试类
 */
class FacilityValueCalculatorTest {

    @Test
    fun `计算设施价值应考虑建设成本维护成本和产出效率`() {
        // Given
        val calculator = FacilityValueCalculator()
        val constructionCost = 1000
        val maintenanceCost = 100
        val productionEfficiency = 1.5

        // When
        val value = calculator.calculateFacilityValue(
            constructionCost = constructionCost,
            maintenanceCost = maintenanceCost,
            productionEfficiency = productionEfficiency
        )

        // Then
        assertTrue(value > 0)
    }

    @Test
    fun `计算投资回报率应根据成本和收益返回正确值`() {
        // Given
        val calculator = FacilityValueCalculator()

        // When
        val roi = calculator.calculateROI(
            constructionCost = 1000,
            dailyRevenue = 100,
            dailyMaintenance = 20
        )

        // Then
        // ROI = (100 - 20) / 1000 = 0.08 (8%)
        assertEquals(0.08, roi, 0.01)
    }

    @Test
    fun `评估设施等级应根据价值返回正确等级`() {
        // Given
        val calculator = FacilityValueCalculator()

        // When & Then
        assertEquals(
            FacilityValueCalculator.FacilityValueLevel.BASIC,
            calculator.assessValueLevel(50)
        )
        assertEquals(
            FacilityValueCalculator.FacilityValueLevel.STANDARD,
            calculator.assessValueLevel(150)
        )
        assertEquals(
            FacilityValueCalculator.FacilityValueLevel.ADVANCED,
            calculator.assessValueLevel(300)
        )
        assertEquals(
            FacilityValueCalculator.FacilityValueLevel.PREMIUM,
            calculator.assessValueLevel(600)
        )
    }

    @Test
    fun `计算战略价值应根据设施类型返回不同值`() {
        // Given
        val calculator = FacilityValueCalculator()

        // When & Then
        val cultivationValue = calculator.calculateStrategicValue(FacilityType.CULTIVATION_ROOM)
        val mineValue = calculator.calculateStrategicValue(FacilityType.SPIRIT_STONE_MINE)

        assertTrue(mineValue > cultivationValue) // 灵石矿战略价值更高
    }
}
