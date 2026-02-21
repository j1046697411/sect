package cn.jzl.sect.resource.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.family.component
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.config.GameConfig
import cn.jzl.sect.core.disciple.Loyalty
import cn.jzl.sect.core.facility.Facility
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.core.sect.Position
import cn.jzl.sect.core.sect.SectPosition
import cn.jzl.sect.core.sect.SectResource
import cn.jzl.sect.engine.SectWorld
import kotlin.test.*

/**
 * 资源消耗系统测试
 */
class ResourceConsumptionSystemTest : EntityRelationContext {
    override lateinit var world: World

    @BeforeTest
    fun setup() {
        GameConfig.resetInstance()
        // 使用 SectWorld 创建世界，确保所有组件已注册
        world = SectWorld.create("测试宗门")
    }

    @Test
    fun testSystemInitialization() {
        val system = ResourceConsumptionSystem(world)
        assertNotNull(system)
    }

    @Test
    fun testConsumptionResultCreation() {
        // Given: 创建消耗结算结果
        val result = ConsumptionResult(
            success = true,
            totalCost = 500L,
            salaryPaid = 400L,
            maintenancePaid = 100L,
            paymentRecords = emptyList()
        )

        // Then: 验证结果属性
        assertTrue(result.success)
        assertEquals(500L, result.totalCost)
        assertEquals(400L, result.salaryPaid)
        assertEquals(100L, result.maintenancePaid)
        assertTrue(result.paymentRecords.isEmpty())
    }

    @Test
    fun testConsumptionResultDisplayStringSuccess() {
        val result = ConsumptionResult(
            success = true,
            totalCost = 500L,
            salaryPaid = 400L,
            maintenancePaid = 100L,
            paymentRecords = emptyList()
        )

        val display = result.toDisplayString()
        assertTrue(display.contains("✓ 正常"), "成功状态应该显示正常标记")
        assertTrue(display.contains("总支出: 500"), "应该显示总支出")
        assertTrue(display.contains("俸禄支出: 400"), "应该显示俸禄支出")
        assertTrue(display.contains("维护支出: 100"), "应该显示维护支出")
    }

    @Test
    fun testConsumptionResultDisplayStringFailed() {
        val result = ConsumptionResult(
            success = false,
            totalCost = 1000L,
            salaryPaid = 300L,
            maintenancePaid = 100L,
            paymentRecords = emptyList()
        )

        val display = result.toDisplayString()
        assertTrue(display.contains("✗ 资金不足"), "失败状态应该显示资金不足标记")
    }

    @Test
    fun testPaymentRecordCreation() {
        // Given: 创建支付记录
        val entity = world.entity {
            it.addComponent(Position(position = SectPosition.ELDER))
            it.addComponent(Loyalty(value = 90))
        }

        val record = PaymentRecord(
            entity = entity,
            position = SectPosition.ELDER,
            expectedAmount = 300L,
            actualAmount = 300L,
            paid = true
        )

        // Then: 验证记录属性
        assertEquals(SectPosition.ELDER, record.position)
        assertEquals(300L, record.expectedAmount)
        assertEquals(300L, record.actualAmount)
        assertTrue(record.paid)
    }

    @Test
    fun testPaymentRecordDisplayStringPaid() {
        val entity = world.entity {
            it.addComponent(Position(position = SectPosition.ELDER))
            it.addComponent(Loyalty(value = 90))
        }

        val record = PaymentRecord(
            entity = entity,
            position = SectPosition.ELDER,
            expectedAmount = 300L,
            actualAmount = 300L,
            paid = true
        )

        val display = record.toDisplayString()
        assertTrue(display.contains("✓"), "已支付应该显示对勾")
        assertTrue(display.contains("长老"), "应该显示职位名称")
        assertTrue(display.contains("300/300"), "应该显示支付金额")
        assertTrue(display.contains("灵石"), "应该显示单位")
    }

    @Test
    fun testPaymentRecordDisplayStringUnpaid() {
        val entity = world.entity {
            it.addComponent(Position(position = SectPosition.DISCIPLE_OUTER))
            it.addComponent(Loyalty(value = 80))
        }

        val record = PaymentRecord(
            entity = entity,
            position = SectPosition.DISCIPLE_OUTER,
            expectedAmount = 30L,
            actualAmount = 10L,
            paid = false
        )

        val display = record.toDisplayString()
        assertTrue(display.contains("✗"), "未支付应该显示叉号")
        assertTrue(display.contains("外门弟子"), "应该显示职位名称")
        assertTrue(display.contains("10/30"), "应该显示部分支付金额")
    }

    @Test
    fun testAllPositionTypes() {
        // 测试所有职位类型的显示名称
        val leaderEntity = world.entity { it.addComponent(Position(SectPosition.LEADER)) }
        val elderEntity = world.entity { it.addComponent(Position(SectPosition.ELDER)) }
        val innerEntity = world.entity { it.addComponent(Position(SectPosition.DISCIPLE_INNER)) }
        val outerEntity = world.entity { it.addComponent(Position(SectPosition.DISCIPLE_OUTER)) }

        val leaderRecord = PaymentRecord(leaderEntity, SectPosition.LEADER, 500L, 500L, true)
        val elderRecord = PaymentRecord(elderEntity, SectPosition.ELDER, 300L, 300L, true)
        val innerRecord = PaymentRecord(innerEntity, SectPosition.DISCIPLE_INNER, 80L, 80L, true)
        val outerRecord = PaymentRecord(outerEntity, SectPosition.DISCIPLE_OUTER, 30L, 30L, true)

        assertTrue(leaderRecord.toDisplayString().contains("掌门"))
        assertTrue(elderRecord.toDisplayString().contains("长老"))
        assertTrue(innerRecord.toDisplayString().contains("内门弟子"))
        assertTrue(outerRecord.toDisplayString().contains("外门弟子"))
    }

    @Test
    fun testGameConfigSalary() {
        // Given: 获取游戏配置
        val config = GameConfig.getInstance()

        // Then: 验证各职位俸禄
        assertEquals(500L, config.salary.getMonthlySalary(SectPosition.LEADER))
        assertEquals(300L, config.salary.getMonthlySalary(SectPosition.ELDER))
        assertEquals(80L, config.salary.getMonthlySalary(SectPosition.DISCIPLE_INNER))
        assertEquals(30L, config.salary.getMonthlySalary(SectPosition.DISCIPLE_OUTER))
    }

    @Test
    fun testGameConfigFacilityMaintenance() {
        // Given: 获取游戏配置
        val config = GameConfig.getInstance()

        // Then: 验证设施维护费计算
        // level 1, efficiency 1.0f -> 1 * 10 * 1.0 = 10
        assertEquals(10L, config.facility.calculateMaintenanceCost(1, 1.0f))
        // level 2, efficiency 1.2f -> 2 * 10 * 1.2 = 24
        assertEquals(24L, config.facility.calculateMaintenanceCost(2, 1.2f))
        // level 5, efficiency 1.5f -> 5 * 10 * 1.5 = 75
        assertEquals(75L, config.facility.calculateMaintenanceCost(5, 1.5f))
    }

    @Test
    fun testGameConfigLoyalty() {
        // Given: 获取游戏配置
        val config = GameConfig.getInstance()

        // Then: 验证忠诚度配置
        assertEquals(1, config.loyalty.loyaltyIncreaseOnPayment)
        assertEquals(10, config.loyalty.loyaltyDecreaseOnUnpaid)
        assertEquals(6, config.loyalty.maxConsecutiveUnpaidMonths)
    }

    @Test
    fun testSectResourceComponent() {
        // Given: 创建宗门资源组件
        val resource1 = SectResource()
        val resource2 = SectResource(spiritStones = 5000L, contributionPoints = 1000L)

        // Then: 验证默认值和自定义值
        assertEquals(1000L, resource1.spiritStones)
        assertEquals(0L, resource1.contributionPoints)

        assertEquals(5000L, resource2.spiritStones)
        assertEquals(1000L, resource2.contributionPoints)
    }

    @Test
    fun testFacilityComponent() {
        // Given: 创建设施组件
        val facility1 = Facility(type = FacilityType.CULTIVATION_ROOM)
        val facility2 = Facility(
            type = FacilityType.DORMITORY,
            level = 3,
            capacity = 50,
            efficiency = 1.5f
        )

        // Then: 验证设施属性
        assertEquals(FacilityType.CULTIVATION_ROOM, facility1.type)
        assertEquals(1, facility1.level)
        assertEquals(0, facility1.capacity)
        assertEquals(1.0f, facility1.efficiency)

        assertEquals(FacilityType.DORMITORY, facility2.type)
        assertEquals(3, facility2.level)
        assertEquals(50, facility2.capacity)
        assertEquals(1.5f, facility2.efficiency)
    }
}
