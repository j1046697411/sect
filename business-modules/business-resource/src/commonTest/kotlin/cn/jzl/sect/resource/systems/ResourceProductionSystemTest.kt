package cn.jzl.sect.resource.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.family.component
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.resource.ResourceProduction
import cn.jzl.sect.core.resource.ResourceType
import cn.jzl.sect.core.sect.SectTreasury
import cn.jzl.sect.engine.SectWorld
import kotlin.test.*

/**
 * 资源生产系统测试
 */
class ResourceProductionSystemTest : EntityRelationContext {
    override lateinit var world: World

    @BeforeTest
    fun setup() {
        // 使用 SectWorld 创建世界，确保所有组件已注册
        world = SectWorld.create("测试宗门")
    }

    @Test
    fun testSystemInitialization() {
        val system = ResourceProductionSystem(world)
        assertNotNull(system)
    }

    @Test
    fun testProductionRecordCreation() {
        // Given: 创建一个实体用于测试
        val entity = world.entity {
            it.addComponent(SectTreasury(spiritStones = 0L, contributionPoints = 0L))
        }

        // When: 创建产出记录
        val record = ProductionRecord(
            entity = entity,
            resourceType = ResourceType.SPIRIT_STONE,
            amount = 100L,
            efficiency = 1.0f
        )

        // Then: 验证记录属性
        assertEquals(ResourceType.SPIRIT_STONE, record.resourceType)
        assertEquals(100L, record.amount)
        assertEquals(1.0f, record.efficiency)
    }

    @Test
    fun testProductionRecordDisplayString() {
        val entity = world.entity {
            it.addComponent(SectTreasury(spiritStones = 0L, contributionPoints = 0L))
        }

        val record = ProductionRecord(
            entity = entity,
            resourceType = ResourceType.SPIRIT_STONE,
            amount = 100L,
            efficiency = 0.85f
        )

        val display = record.toDisplayString()
        assertTrue(display.contains("灵石"), "应该包含资源名称")
        assertTrue(display.contains("+100"), "应该包含产出数量")
        assertTrue(display.contains("85%"), "应该包含效率百分比")
    }

    @Test
    fun testMonthlyProductionSummaryCreation() {
        // Given: 创建月度产出统计
        val summary = MonthlyProductionSummary(
            spiritStones = 1000L,
            herbs = 500L,
            ores = 300L,
            food = 800L,
            totalDays = 30
        )

        // Then: 验证统计属性
        assertEquals(1000L, summary.spiritStones)
        assertEquals(500L, summary.herbs)
        assertEquals(300L, summary.ores)
        assertEquals(800L, summary.food)
        assertEquals(30, summary.totalDays)
    }

    @Test
    fun testMonthlyProductionSummaryDisplayString() {
        val summary = MonthlyProductionSummary(
            spiritStones = 1000L,
            herbs = 500L,
            ores = 300L,
            food = 800L,
            totalDays = 30
        )

        val display = summary.toDisplayString()
        assertTrue(display.contains("月度资源产出统计"), "应该包含标题")
        assertTrue(display.contains("灵石: +1000"), "应该包含灵石统计")
        assertTrue(display.contains("草药: +500"), "应该包含草药统计")
        assertTrue(display.contains("矿石: +300"), "应该包含矿石统计")
        assertTrue(display.contains("粮食: +800"), "应该包含粮食统计")
        assertTrue(display.contains("30天"), "应该包含天数")
    }

    @Test
    fun testResourceTypeDisplayName() {
        // 验证资源类型显示名称
        val spiritStoneRecord = ProductionRecord(
            entity = world.entity { it.addComponent(SectTreasury()) },
            resourceType = ResourceType.SPIRIT_STONE,
            amount = 100L,
            efficiency = 1.0f
        )
        assertTrue(spiritStoneRecord.toDisplayString().contains("灵石"))

        val herbRecord = ProductionRecord(
            entity = world.entity { it.addComponent(SectTreasury()) },
            resourceType = ResourceType.HERB,
            amount = 50L,
            efficiency = 1.0f
        )
        assertTrue(herbRecord.toDisplayString().contains("草药"))

        val oreRecord = ProductionRecord(
            entity = world.entity { it.addComponent(SectTreasury()) },
            resourceType = ResourceType.ORE,
            amount = 75L,
            efficiency = 1.0f
        )
        assertTrue(oreRecord.toDisplayString().contains("矿石"))

        val foodRecord = ProductionRecord(
            entity = world.entity { it.addComponent(SectTreasury()) },
            resourceType = ResourceType.FOOD,
            amount = 200L,
            efficiency = 1.0f
        )
        assertTrue(foodRecord.toDisplayString().contains("粮食"))
    }

    @Test
    fun testResourceProductionComponent() {
        // Given: 创建资源生产组件
        val production = ResourceProduction(
            type = ResourceType.SPIRIT_STONE,
            baseOutput = 100L,
            efficiency = 1.5f,
            isActive = true
        )

        // Then: 验证组件属性
        assertEquals(ResourceType.SPIRIT_STONE, production.type)
        assertEquals(100L, production.baseOutput)
        assertEquals(1.5f, production.efficiency)
        assertTrue(production.isActive)
    }

}
