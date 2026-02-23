package cn.jzl.sect.facility.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.engine.SectWorld
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 设施使用服务测试类
 */
class FacilityUsageServiceTest : EntityRelationContext {
    override lateinit var world: World

    @BeforeTest
    fun setup() {
        world = SectWorld.create("测试宗门")
    }

    @Test
    fun `使用修炼室应增加修炼效率`() {
        // Given
        val service = FacilityUsageService(world)

        // When
        val effect = service.getFacilityEffect(FacilityType.CULTIVATION_ROOM)

        // Then
        assertTrue(effect.efficiencyBonus > 0)
        assertEquals("cultivation", effect.targetActivity)
    }

    @Test
    fun `使用炼丹房应增加炼丹成功率`() {
        // Given
        val service = FacilityUsageService(world)

        // When
        val effect = service.getFacilityEffect(FacilityType.ALCHEMY_ROOM)

        // Then
        assertTrue(effect.successRateBonus > 0)
        assertEquals("alchemy", effect.targetActivity)
    }

    @Test
    fun `计算使用成本应根据设施类型返回正确值`() {
        // Given
        val service = FacilityUsageService(world)

        // When
        val cost1 = service.calculateUsageCost(FacilityType.CULTIVATION_ROOM, 1)
        val cost2 = service.calculateUsageCost(FacilityType.CULTIVATION_ROOM, 3)

        // Then
        assertTrue(cost2 > cost1) // 使用时间越长成本越高
    }

    @Test
    fun `检查是否可使用应根据贡献点判断`() {
        // Given
        val service = FacilityUsageService(world)

        // When & Then
        assertTrue(service.canUseFacility(FacilityType.CULTIVATION_ROOM, contributionPoints = 100))
        assertFalse(service.canUseFacility(FacilityType.CULTIVATION_ROOM, contributionPoints = 0))
    }

    @Test
    fun `获取设施功能描述应返回正确描述`() {
        // Given
        val service = FacilityUsageService(world)

        // When
        val description = service.getFacilityFunctionDescription(FacilityType.CULTIVATION_ROOM)

        // Then
        assertTrue(description.isNotEmpty())
        assertTrue(description.contains("修炼"))
    }
}
