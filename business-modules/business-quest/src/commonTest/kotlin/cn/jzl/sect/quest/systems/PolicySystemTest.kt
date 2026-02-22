package cn.jzl.sect.quest.systems

import cn.jzl.ecs.ECSDsl
import cn.jzl.ecs.World
import cn.jzl.ecs.addon.Addon
import cn.jzl.ecs.addon.WorldSetup
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.ecs.world
import cn.jzl.sect.core.quest.PolicyComponent
import cn.jzl.sect.core.quest.PolicyValidationResult
import cn.jzl.sect.core.quest.ResourceAllocation
import cn.jzl.sect.core.quest.defaultPolicy
import kotlin.test.*

/**
 * 政策系统测试
 */
class PolicySystemTest : EntityRelationContext {
    override lateinit var world: World
    private lateinit var system: PolicySystem

    @OptIn(ECSDsl::class)
    private fun createTestWorld(): World {
        val testWorld = world {
            WorldSetupInstallHelper.install(this, createAddon<Unit>("test") {
                components {
                    world.componentId<PolicyComponent>()
                }
            })
        }
        return testWorld
    }

    @BeforeTest
    fun setup() {
        world = createTestWorld()
        system = PolicySystem(world)
    }

    // ==================== 测试默认配置读取 ====================

    @Test
    fun testGetCurrentPolicy_ReturnsDefaultWhenNoPolicyExists() {
        // Given: 没有配置政策

        // When: 获取当前政策
        val policy = system.getCurrentPolicy(world)

        // Then: 应该返回默认配置
        assertEquals(5, policy.selectionCycleYears, "默认选拔周期应该是5年")
        assertEquals(0.05f, policy.selectionRatio, "默认选拔比例应该是5%")
        assertEquals(40, policy.resourceAllocation.cultivation, "默认修炼分配应该是40%")
        assertEquals(30, policy.resourceAllocation.facility, "默认设施分配应该是30%")
        assertEquals(30, policy.resourceAllocation.reserve, "默认储备分配应该是30%")
    }

    @Test
    fun testDefaultPolicy_HasCorrectValues() {
        // Given: 获取默认政策
        val default = defaultPolicy()

        // Then: 验证默认值
        assertEquals(5, default.selectionCycleYears, "默认选拔周期应该是5年")
        assertEquals(0.05f, default.selectionRatio, "默认选拔比例应该是5%")
        assertEquals(1.0f, default.resourceAllocationRatio, "默认资源分配比例应该是100%")
        assertEquals(40, default.resourceAllocation.cultivation, "默认修炼分配应该是40%")
        assertEquals(30, default.resourceAllocation.facility, "默认设施分配应该是30%")
        assertEquals(30, default.resourceAllocation.reserve, "默认储备分配应该是30%")
    }

    @Test
    fun testGetCurrentPolicy_ReturnsExistingPolicy() {
        // Given: 创建自定义政策实体
        val customPolicy = PolicyComponent(
            selectionCycleYears = 7,
            selectionRatio = 0.08f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 50,
                facility = 30,
                reserve = 20
            )
        )
        world.entity {
            it.addComponent(customPolicy)
        }

        // When: 获取当前政策
        val policy = system.getCurrentPolicy(world)

        // Then: 应该返回已存在的配置
        assertEquals(7, policy.selectionCycleYears, "应该返回已配置的选拔周期")
        assertEquals(0.08f, policy.selectionRatio, "应该返回已配置的选拔比例")
        assertEquals(50, policy.resourceAllocation.cultivation, "应该返回已配置的修炼分配")
    }

    // ==================== 测试配置更新 ====================

    @Test
    fun testUpdatePolicy_CreatesNewPolicyWhenNoneExists() {
        // Given: 新政策配置
        val newPolicy = PolicyComponent(
            selectionCycleYears = 6,
            selectionRatio = 0.07f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 45,
                facility = 35,
                reserve = 20
            )
        )

        // When: 更新政策
        val result = system.updatePolicy(world, newPolicy)

        // Then: 应该成功创建
        assertTrue(result, "应该成功更新政策")

        val currentPolicy = system.getCurrentPolicy(world)
        assertEquals(6, currentPolicy.selectionCycleYears, "选拔周期应该更新为6年")
        assertEquals(0.07f, currentPolicy.selectionRatio, "选拔比例应该更新为7%")
        assertEquals(45, currentPolicy.resourceAllocation.cultivation, "修炼分配应该更新为45%")
    }

    @Test
    fun testUpdatePolicy_UpdatesExistingPolicy() {
        // Given: 先创建初始政策
        val initialPolicy = defaultPolicy()
        system.updatePolicy(world, initialPolicy)

        // When: 更新为新政策
        val newPolicy = PolicyComponent(
            selectionCycleYears = 8,
            selectionRatio = 0.06f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 35,
                facility = 40,
                reserve = 25
            )
        )
        val result = system.updatePolicy(world, newPolicy)

        // Then: 应该成功更新
        assertTrue(result, "应该成功更新政策")

        val currentPolicy = system.getCurrentPolicy(world)
        assertEquals(8, currentPolicy.selectionCycleYears, "选拔周期应该更新为8年")
        assertEquals(0.06f, currentPolicy.selectionRatio, "选拔比例应该更新为6%")
        assertEquals(35, currentPolicy.resourceAllocation.cultivation, "修炼分配应该更新为35%")
        assertEquals(40, currentPolicy.resourceAllocation.facility, "设施分配应该更新为40%")
        assertEquals(25, currentPolicy.resourceAllocation.reserve, "储备分配应该更新为25%")
    }

    @Test
    fun testUpdatePolicyWithResult_ReturnsSuccessForValidPolicy() {
        // Given: 有效的新政策
        val newPolicy = PolicyComponent(
            selectionCycleYears = 5,
            selectionRatio = 0.05f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 40,
                facility = 30,
                reserve = 30
            )
        )

        // When: 使用结果更新政策
        val result = system.updatePolicyWithResult(world, newPolicy)

        // Then: 应该返回成功
        assertTrue(result is PolicyUpdateResult.Success, "应该返回成功结果")
        assertEquals(newPolicy, (result as PolicyUpdateResult.Success).policy, "应该返回更新后的政策")
    }

    @Test
    fun testUpdatePolicyWithResult_ReturnsFailureForInvalidPolicy() {
        // Given: 无效的新政策（选拔周期太短）
        val invalidPolicy = PolicyComponent(
            selectionCycleYears = 2,  // 无效：小于3
            selectionRatio = 0.05f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 40,
                facility = 30,
                reserve = 30
            )
        )

        // When: 使用结果更新政策
        val result = system.updatePolicyWithResult(world, invalidPolicy)

        // Then: 应该返回失败
        assertTrue(result is PolicyUpdateResult.Failure, "应该返回失败结果")
        val failure = result as PolicyUpdateResult.Failure
        assertTrue(failure.reasons.isNotEmpty(), "应该包含错误原因")
    }

    // ==================== 测试配置验证（有效配置） ====================

    @Test
    fun testValidatePolicy_ValidPolicy_ReturnsValid() {
        // Given: 有效配置
        val validPolicy = PolicyComponent(
            selectionCycleYears = 5,
            selectionRatio = 0.05f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 40,
                facility = 30,
                reserve = 30
            )
        )

        // When: 验证政策
        val result = system.validatePolicy(validPolicy)

        // Then: 应该返回有效
        assertTrue(result is PolicyValidationResult.Valid, "有效配置应该通过验证")
    }

    @Test
    fun testValidatePolicy_BoundaryValues_ReturnsValid() {
        // Given: 边界值配置（最小值）
        val minPolicy = PolicyComponent(
            selectionCycleYears = 3,  // 最小周期
            selectionRatio = 0.03f,   // 最小比例
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 0,      // 最小分配
                facility = 0,
                reserve = 100
            )
        )

        // When: 验证政策
        val result = system.validatePolicy(minPolicy)

        // Then: 应该返回有效
        assertTrue(result is PolicyValidationResult.Valid, "边界值配置应该通过验证")
    }

    @Test
    fun testValidatePolicy_MaxBoundaryValues_ReturnsValid() {
        // Given: 边界值配置（最大值）
        val maxPolicy = PolicyComponent(
            selectionCycleYears = 10,  // 最大周期
            selectionRatio = 0.10f,    // 最大比例
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 100,     // 最大分配
                facility = 0,
                reserve = 0
            )
        )

        // When: 验证政策
        val result = system.validatePolicy(maxPolicy)

        // Then: 应该返回有效
        assertTrue(result is PolicyValidationResult.Valid, "最大边界值配置应该通过验证")
    }

    // ==================== 测试配置验证（无效配置） ====================

    @Test
    fun testValidatePolicy_InvalidCycleTooShort_ReturnsInvalid() {
        // Given: 选拔周期太短
        val invalidPolicy = PolicyComponent(
            selectionCycleYears = 2,  // 无效：小于3
            selectionRatio = 0.05f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 40,
                facility = 30,
                reserve = 30
            )
        )

        // When: 验证政策
        val result = system.validatePolicy(invalidPolicy)

        // Then: 应该返回无效
        assertTrue(result is PolicyValidationResult.Invalid, "选拔周期太短应该验证失败")
        val invalid = result as PolicyValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("选拔周期") }, "错误原因应该包含选拔周期")
    }

    @Test
    fun testValidatePolicy_InvalidCycleTooLong_ReturnsInvalid() {
        // Given: 选拔周期太长
        val invalidPolicy = PolicyComponent(
            selectionCycleYears = 11,  // 无效：大于10
            selectionRatio = 0.05f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 40,
                facility = 30,
                reserve = 30
            )
        )

        // When: 验证政策
        val result = system.validatePolicy(invalidPolicy)

        // Then: 应该返回无效
        assertTrue(result is PolicyValidationResult.Invalid, "选拔周期太长应该验证失败")
    }

    @Test
    fun testValidatePolicy_InvalidRatioTooLow_ReturnsInvalid() {
        // Given: 选拔比例太低
        val invalidPolicy = PolicyComponent(
            selectionCycleYears = 5,
            selectionRatio = 0.02f,  // 无效：小于3%
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 40,
                facility = 30,
                reserve = 30
            )
        )

        // When: 验证政策
        val result = system.validatePolicy(invalidPolicy)

        // Then: 应该返回无效
        assertTrue(result is PolicyValidationResult.Invalid, "选拔比例太低应该验证失败")
        val invalid = result as PolicyValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("选拔比例") }, "错误原因应该包含选拔比例")
    }

    @Test
    fun testValidatePolicy_InvalidRatioTooHigh_ReturnsInvalid() {
        // Given: 选拔比例太高
        val invalidPolicy = PolicyComponent(
            selectionCycleYears = 5,
            selectionRatio = 0.15f,  // 无效：大于10%
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 40,
                facility = 30,
                reserve = 30
            )
        )

        // When: 验证政策
        val result = system.validatePolicy(invalidPolicy)

        // Then: 应该返回无效
        assertTrue(result is PolicyValidationResult.Invalid, "选拔比例太高应该验证失败")
    }

    @Test
    fun testValidatePolicy_InvalidResourceAllocationSum_ReturnsInvalid() {
        // Given: 资源分配总和不等于100%
        val invalidPolicy = PolicyComponent(
            selectionCycleYears = 5,
            selectionRatio = 0.05f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 40,
                facility = 30,
                reserve = 20  // 总和90%，不等于100%
            )
        )

        // When: 验证政策
        val result = system.validatePolicy(invalidPolicy)

        // Then: 应该返回无效
        assertTrue(result is PolicyValidationResult.Invalid, "资源分配总和不等于100%应该验证失败")
        val invalid = result as PolicyValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("资源分配") }, "错误原因应该包含资源分配")
    }

    @Test
    fun testValidatePolicy_InvalidResourceAllocationNegative_ReturnsInvalid() {
        // Given: 资源分配包含负值
        val invalidPolicy = PolicyComponent(
            selectionCycleYears = 5,
            selectionRatio = 0.05f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = -10,  // 无效：负值
                facility = 60,
                reserve = 50
            )
        )

        // When: 验证政策
        val result = system.validatePolicy(invalidPolicy)

        // Then: 应该返回无效
        assertTrue(result is PolicyValidationResult.Invalid, "资源分配包含负值应该验证失败")
    }

    @Test
    fun testUpdatePolicy_InvalidPolicy_ReturnsFalse() {
        // Given: 无效配置
        val invalidPolicy = PolicyComponent(
            selectionCycleYears = 2,  // 无效
            selectionRatio = 0.05f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 40,
                facility = 30,
                reserve = 30
            )
        )

        // When: 尝试更新无效政策
        val result = system.updatePolicy(world, invalidPolicy)

        // Then: 应该返回失败
        assertFalse(result, "无效政策不应该被更新")
    }

    // ==================== 测试重置默认配置 ====================

    @Test
    fun testResetToDefault_ReturnsDefaultPolicy() {
        // Given: 先设置一个自定义政策
        val customPolicy = PolicyComponent(
            selectionCycleYears = 8,
            selectionRatio = 0.09f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 50,
                facility = 25,
                reserve = 25
            )
        )
        system.updatePolicy(world, customPolicy)

        // When: 重置为默认配置
        val resetPolicy = system.resetToDefault(world)

        // Then: 应该返回默认配置
        assertEquals(5, resetPolicy.selectionCycleYears, "重置后选拔周期应该是5年")
        assertEquals(0.05f, resetPolicy.selectionRatio, "重置后选拔比例应该是5%")
        assertEquals(40, resetPolicy.resourceAllocation.cultivation, "重置后修炼分配应该是40%")
        assertEquals(30, resetPolicy.resourceAllocation.facility, "重置后设施分配应该是30%")
        assertEquals(30, resetPolicy.resourceAllocation.reserve, "重置后储备分配应该是30%")
    }

    @Test
    fun testResetToDefault_UpdatesWorldPolicy() {
        // Given: 先设置一个自定义政策
        val customPolicy = PolicyComponent(
            selectionCycleYears = 9,
            selectionRatio = 0.08f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 60,
                facility = 20,
                reserve = 20
            )
        )
        system.updatePolicy(world, customPolicy)

        // When: 重置为默认配置
        system.resetToDefault(world)

        // Then: 世界中应该存储默认配置
        val currentPolicy = system.getCurrentPolicy(world)
        assertEquals(5, currentPolicy.selectionCycleYears, "世界中应该存储默认选拔周期")
        assertEquals(0.05f, currentPolicy.selectionRatio, "世界中应该存储默认选拔比例")
    }

    @Test
    fun testResetToDefault_CreatesPolicyIfNoneExists() {
        // Given: 没有现有政策

        // When: 重置为默认配置
        val resetPolicy = system.resetToDefault(world)

        // Then: 应该创建默认政策
        assertNotNull(resetPolicy, "应该返回默认政策")
        assertEquals(5, resetPolicy.selectionCycleYears, "应该创建默认选拔周期")

        // 验证世界中确实存在该政策
        val currentPolicy = system.getCurrentPolicy(world)
        assertEquals(0.05f, currentPolicy.selectionRatio, "世界中应该存在默认政策")
    }

    // ==================== 测试辅助方法 ====================

    @Test
    fun testInitialize_CreatesDefaultWhenNoPolicy() {
        // Given: 没有现有政策

        // When: 初始化政策系统
        val policy = system.initialize(world)

        // Then: 应该创建并返回默认政策
        assertEquals(5, policy.selectionCycleYears, "应该创建默认选拔周期")
        assertTrue(system.isPolicyConfigured(world), "政策应该被标记为已配置")
    }

    @Test
    fun testInitialize_ReturnsExistingPolicy() {
        // Given: 已有自定义政策
        val customPolicy = PolicyComponent(
            selectionCycleYears = 7,
            selectionRatio = 0.06f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 45,
                facility = 35,
                reserve = 20
            )
        )
        system.updatePolicy(world, customPolicy)

        // When: 初始化政策系统
        val policy = system.initialize(world)

        // Then: 应该返回现有政策
        assertEquals(7, policy.selectionCycleYears, "应该返回现有选拔周期")
    }

    @Test
    fun testIsPolicyConfigured_ReturnsFalseWhenNoPolicy() {
        // Given: 没有配置政策

        // When: 检查是否已配置
        val configured = system.isPolicyConfigured(world)

        // Then: 应该返回false
        assertFalse(configured, "没有政策时应该返回false")
    }

    @Test
    fun testIsPolicyConfigured_ReturnsTrueWhenPolicyExists() {
        // Given: 已配置政策
        system.initialize(world)

        // When: 检查是否已配置
        val configured = system.isPolicyConfigured(world)

        // Then: 应该返回true
        assertTrue(configured, "有政策时应该返回true")
    }

    @Test
    fun testGetResourceAllocation_ReturnsCorrectAllocation() {
        // Given: 配置自定义资源分配
        val customPolicy = PolicyComponent(
            selectionCycleYears = 5,
            selectionRatio = 0.05f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 50,
                facility = 30,
                reserve = 20
            )
        )
        system.updatePolicy(world, customPolicy)

        // When: 获取资源分配
        val allocation = system.getResourceAllocation(world)

        // Then: 应该返回正确的分配
        assertEquals(50, allocation.cultivation, "修炼分配应该是50%")
        assertEquals(30, allocation.facility, "设施分配应该是30%")
        assertEquals(20, allocation.reserve, "储备分配应该是20%")
    }

    @Test
    fun testCreatePolicy_CreatesCorrectComponent() {
        // When: 使用工厂方法创建政策
        val policy = system.createPolicy(
            selectionCycleYears = 6,
            selectionRatio = 0.07f,
            cultivationRatio = 45,
            facilityRatio = 35,
            reserveRatio = 20
        )

        // Then: 应该创建正确的政策组件
        assertEquals(6, policy.selectionCycleYears, "选拔周期应该是6年")
        assertEquals(0.07f, policy.selectionRatio, "选拔比例应该是7%")
        assertEquals(45, policy.resourceAllocation.cultivation, "修炼分配应该是45%")
        assertEquals(35, policy.resourceAllocation.facility, "设施分配应该是35%")
        assertEquals(20, policy.resourceAllocation.reserve, "储备分配应该是20%")
    }

    @Test
    fun testGetPolicyEntity_ReturnsNullWhenNoPolicy() {
        // Given: 没有配置政策

        // When: 获取政策实体
        val entity = system.getPolicyEntity(world)

        // Then: 应该返回null
        assertNull(entity, "没有政策时应该返回null")
    }

    @Test
    fun testGetPolicyEntity_ReturnsEntityWhenPolicyExists() {
        // Given: 已配置政策
        system.initialize(world)

        // When: 获取政策实体
        val entity = system.getPolicyEntity(world)

        // Then: 应该返回实体
        assertNotNull(entity, "有政策时应该返回实体")
    }

    // ==================== 测试 ResourceAllocation ====================

    @Test
    fun testResourceAllocationIsValid_ReturnsTrueForValidAllocation() {
        // Given: 有效的资源分配
        val allocation = ResourceAllocation(
            cultivation = 40,
            facility = 30,
            reserve = 30
        )

        // Then: 应该返回true
        assertTrue(allocation.isValid(), "总和为100%的分配应该有效")
    }

    @Test
    fun testResourceAllocationIsValid_ReturnsFalseForInvalidSum() {
        // Given: 总和不为100%的资源分配
        val allocation = ResourceAllocation(
            cultivation = 40,
            facility = 30,
            reserve = 25  // 总和95%
        )

        // Then: 应该返回false
        assertFalse(allocation.isValid(), "总和不为100%的分配应该无效")
    }

    @Test
    fun testResourceAllocationIsValid_ReturnsFalseForNegativeValue() {
        // Given: 包含负值的资源分配
        val allocation = ResourceAllocation(
            cultivation = -10,
            facility = 60,
            reserve = 50
        )

        // Then: 应该返回false
        assertFalse(allocation.isValid(), "包含负值的分配应该无效")
    }

    /**
     * 查询上下文 - 政策配置
     */
    class PolicyQueryContext(world: World) : EntityQueryContext(world) {
        val policy: PolicyComponent by component()
    }

    private object WorldSetupInstallHelper {
        @Suppress("UNCHECKED_CAST")
        fun install(ws: WorldSetup, addon: Addon<*, *>) {
            ws.install(addon as Addon<Any, Any>) {}
        }
    }
}
