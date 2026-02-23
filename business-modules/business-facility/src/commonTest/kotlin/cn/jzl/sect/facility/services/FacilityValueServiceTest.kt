package cn.jzl.sect.facility.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.engine.SectWorld
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 设施价值服务测试类
 */
class FacilityValueServiceTest : EntityRelationContext {
    override lateinit var world: World

    @BeforeTest
    fun setup() {
        world = SectWorld.create("测试宗门")
    }

    @Test
    fun `计算设施价值应考虑建设成本维护成本和产出效率`() {
        // Given
        val service = FacilityValueService(world)
        val constructionCost = 1000
        val maintenanceCost = 100
        val productionEfficiency = 1.5

        // When
        val value = service.calculateFacilityValue(
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
        val service = FacilityValueService(world)

        // When
        val roi = service.calculateROI(
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
        val service = FacilityValueService(world)

        // When & Then
        assertEquals(
            FacilityValueService.FacilityValueLevel.BASIC,
            service.assessValueLevel(50)
        )
        assertEquals(
            FacilityValueService.FacilityValueLevel.STANDARD,
            service.assessValueLevel(150)
        )
        assertEquals(
            FacilityValueService.FacilityValueLevel.ADVANCED,
            service.assessValueLevel(300)
        )
        assertEquals(
            FacilityValueService.FacilityValueLevel.PREMIUM,
            service.assessValueLevel(600)
        )
    }

    @Test
    fun `计算战略价值应根据设施类型返回不同值`() {
        // Given
        val service = FacilityValueService(world)

        // When & Then
        val cultivationValue = service.calculateStrategicValue(FacilityType.CULTIVATION_ROOM)
        val mineValue = service.calculateStrategicValue(FacilityType.SPIRIT_STONE_MINE)

        assertTrue(mineValue > cultivationValue) // 灵石矿战略价值更高
    }
}
