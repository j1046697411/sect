package cn.jzl.sect.core.quest

import cn.jzl.ecs.*
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.*
import kotlin.test.*

class PolicyComponentTest : EntityRelationContext {
    override lateinit var world: World

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<PolicyComponent>()
        }
    }

    @BeforeTest
    fun setup() {
        world = world { install(testAddon) }
    }

    @Test
    fun testPolicyComponentCreation() {
        val entity = world.entity {
            it.addComponent(PolicyComponent(
                selectionCycleYears = 5,
                selectionRatio = 0.05f,
                resourceAllocationRatio = 1.0f,
                resourceAllocation = ResourceAllocation(
                    cultivation = 40,
                    facility = 30,
                    reserve = 30
                )
            ))
        }

        val policy = entity.getComponent<PolicyComponent>()
        assertEquals(5, policy.selectionCycleYears)
        assertEquals(0.05f, policy.selectionRatio)
        assertEquals(1.0f, policy.resourceAllocationRatio)
        assertEquals(40, policy.resourceAllocation.cultivation)
        assertEquals(30, policy.resourceAllocation.facility)
        assertEquals(30, policy.resourceAllocation.reserve)
    }

    @Test
    fun testIsValidWithValidPolicy() {
        val policy = PolicyComponent(
            selectionCycleYears = 5,
            selectionRatio = 0.05f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 40,
                facility = 30,
                reserve = 30
            )
        )

        assertTrue(policy.isValid())
    }

    @Test
    fun testIsValidWithInvalidSelectionCycle() {
        val policy = PolicyComponent(
            selectionCycleYears = 2,  // 小于3年，无效
            selectionRatio = 0.05f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 40,
                facility = 30,
                reserve = 30
            )
        )

        assertFalse(policy.isValid())
    }

    @Test
    fun testIsValidWithInvalidSelectionRatioTooHigh() {
        val policy = PolicyComponent(
            selectionCycleYears = 5,
            selectionRatio = 0.15f,  // 大于10%，无效
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 40,
                facility = 30,
                reserve = 30
            )
        )

        assertFalse(policy.isValid())
    }

    @Test
    fun testIsValidWithInvalidSelectionRatioTooLow() {
        val policy = PolicyComponent(
            selectionCycleYears = 5,
            selectionRatio = 0.01f,  // 小于3%，无效
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 40,
                facility = 30,
                reserve = 30
            )
        )

        assertFalse(policy.isValid())
    }

    @Test
    fun testIsValidWithInvalidResourceAllocation() {
        val policy = PolicyComponent(
            selectionCycleYears = 5,
            selectionRatio = 0.05f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 50,
                facility = 30,
                reserve = 30  // 总和110%，无效
            )
        )

        assertFalse(policy.isValid())
    }

    @Test
    fun testIsValidWithBoundaryValues() {
        val policyMin = PolicyComponent(
            selectionCycleYears = 3,  // 最小值
            selectionRatio = 0.03f,   // 最小值3%
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 100,
                facility = 0,
                reserve = 0
            )
        )
        assertTrue(policyMin.isValid())

        val policyMax = PolicyComponent(
            selectionCycleYears = 10, // 最大值
            selectionRatio = 0.10f,   // 最大值10%
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 0,
                facility = 0,
                reserve = 100
            )
        )
        assertTrue(policyMax.isValid())
    }

    @Test
    fun testCalculateSelectionCount() {
        val policy = PolicyComponent(
            selectionCycleYears = 5,
            selectionRatio = 0.05f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 40,
                facility = 30,
                reserve = 30
            )
        )

        assertEquals(1, policy.calculateSelectionCount(10))  // 10 * 0.05 = 0.5 -> 至少1
        assertEquals(2, policy.calculateSelectionCount(40))  // 40 * 0.05 = 2
        assertEquals(5, policy.calculateSelectionCount(100)) // 100 * 0.05 = 5
    }

    @Test
    fun testCalculateSelectionCountWithZeroTotal() {
        val policy = PolicyComponent(
            selectionCycleYears = 5,
            selectionRatio = 0.05f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = 40,
                facility = 30,
                reserve = 30
            )
        )

        // 即使0个弟子，也至少返回1（但实际业务逻辑应该处理这种情况）
        assertEquals(1, policy.calculateSelectionCount(0))
    }

    @Test
    fun testCalculateResourceAllocation() {
        val policy = PolicyComponent(
            selectionCycleYears = 5,
            selectionRatio = 0.05f,
            resourceAllocationRatio = 0.5f,
            resourceAllocation = ResourceAllocation(
                cultivation = 40,
                facility = 30,
                reserve = 30
            )
        )

        assertEquals(500L, policy.calculateResourceAllocation(1000L))
        assertEquals(250L, policy.calculateResourceAllocation(500L))
        assertEquals(0L, policy.calculateResourceAllocation(0L))
    }

    @Test
    fun testDefaultPolicy() {
        val policy = defaultPolicy()

        assertEquals(5, policy.selectionCycleYears)
        assertEquals(0.05f, policy.selectionRatio)
        assertEquals(1.0f, policy.resourceAllocationRatio)
        assertEquals(40, policy.resourceAllocation.cultivation)
        assertEquals(30, policy.resourceAllocation.facility)
        assertEquals(30, policy.resourceAllocation.reserve)
        assertTrue(policy.isValid())
    }

    @Test
    fun testDefaultPolicySelectionCount() {
        val policy = defaultPolicy()

        assertEquals(5, policy.calculateSelectionCount(100))  // 100 * 0.05 = 5
        assertEquals(1, policy.calculateSelectionCount(10))   // 10 * 0.05 = 0.5 -> 1
    }

    @Test
    fun testResourceAllocationValidation() {
        val validAllocation = ResourceAllocation(40, 30, 30)
        assertTrue(validAllocation.isValid())

        val invalidAllocation = ResourceAllocation(50, 30, 30)  // 总和110
        assertFalse(invalidAllocation.isValid())

        val negativeAllocation = ResourceAllocation(-10, 50, 60)  // 有负数
        assertFalse(negativeAllocation.isValid())
    }

    @Test
    fun testValidatePolicyWithErrors() {
        val invalidPolicy = PolicyComponent(
            selectionCycleYears = 2,
            selectionRatio = 0.15f,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(50, 30, 40)  // 总和120
        )

        val result = validatePolicy(invalidPolicy)
        assertTrue(result is PolicyValidationResult.Invalid)
        
        val errors = (result as PolicyValidationResult.Invalid).reasons
        assertTrue(errors.isNotEmpty())
    }

    @Test
    fun testValidatePolicyValid() {
        val validPolicy = defaultPolicy()
        
        val result = validatePolicy(validPolicy)
        assertEquals(PolicyValidationResult.Valid, result)
    }
}
