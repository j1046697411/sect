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
                selectionCycleYears = 2,
                selectionRatio = 0.25f,
                resourceAllocationRatio = 0.4f
            ))
        }

        val policy = entity.getComponent<PolicyComponent>()
        assertEquals(2, policy.selectionCycleYears)
        assertEquals(0.25f, policy.selectionRatio)
        assertEquals(0.4f, policy.resourceAllocationRatio)
    }

    @Test
    fun testIsValidWithValidPolicy() {
        val policy = PolicyComponent(
            selectionCycleYears = 1,
            selectionRatio = 0.5f,
            resourceAllocationRatio = 0.5f
        )

        assertTrue(policy.isValid())
    }

    @Test
    fun testIsValidWithInvalidSelectionCycle() {
        val policy = PolicyComponent(
            selectionCycleYears = 0,
            selectionRatio = 0.5f,
            resourceAllocationRatio = 0.5f
        )

        assertFalse(policy.isValid())
    }

    @Test
    fun testIsValidWithInvalidSelectionRatioTooHigh() {
        val policy = PolicyComponent(
            selectionCycleYears = 1,
            selectionRatio = 1.5f,
            resourceAllocationRatio = 0.5f
        )

        assertFalse(policy.isValid())
    }

    @Test
    fun testIsValidWithInvalidSelectionRatioNegative() {
        val policy = PolicyComponent(
            selectionCycleYears = 1,
            selectionRatio = -0.1f,
            resourceAllocationRatio = 0.5f
        )

        assertFalse(policy.isValid())
    }

    @Test
    fun testIsValidWithInvalidResourceRatioTooHigh() {
        val policy = PolicyComponent(
            selectionCycleYears = 1,
            selectionRatio = 0.5f,
            resourceAllocationRatio = 1.5f
        )

        assertFalse(policy.isValid())
    }

    @Test
    fun testIsValidWithInvalidResourceRatioNegative() {
        val policy = PolicyComponent(
            selectionCycleYears = 1,
            selectionRatio = 0.5f,
            resourceAllocationRatio = -0.1f
        )

        assertFalse(policy.isValid())
    }

    @Test
    fun testIsValidWithBoundaryValues() {
        val policyZero = PolicyComponent(
            selectionCycleYears = 1,
            selectionRatio = 0.0f,
            resourceAllocationRatio = 0.0f
        )
        assertTrue(policyZero.isValid())

        val policyOne = PolicyComponent(
            selectionCycleYears = 1,
            selectionRatio = 1.0f,
            resourceAllocationRatio = 1.0f
        )
        assertTrue(policyOne.isValid())
    }

    @Test
    fun testCalculateSelectionCount() {
        val policy = PolicyComponent(
            selectionCycleYears = 1,
            selectionRatio = 0.2f,
            resourceAllocationRatio = 0.3f
        )

        assertEquals(2, policy.calculateSelectionCount(10))
        assertEquals(5, policy.calculateSelectionCount(25))
        assertEquals(20, policy.calculateSelectionCount(100))
    }

    @Test
    fun testCalculateSelectionCountWithZeroTotal() {
        val policy = PolicyComponent(
            selectionCycleYears = 1,
            selectionRatio = 0.2f,
            resourceAllocationRatio = 0.3f
        )

        assertEquals(1, policy.calculateSelectionCount(0))
    }

    @Test
    fun testCalculateSelectionCountWithSmallTotal() {
        val policy = PolicyComponent(
            selectionCycleYears = 1,
            selectionRatio = 0.01f,  // 1%
            resourceAllocationRatio = 0.3f
        )

        // 即使只有1个弟子，也至少选拔1个
        assertEquals(1, policy.calculateSelectionCount(1))
        assertEquals(1, policy.calculateSelectionCount(50))
    }

    @Test
    fun testCalculateResourceAllocation() {
        val policy = PolicyComponent(
            selectionCycleYears = 1,
            selectionRatio = 0.2f,
            resourceAllocationRatio = 0.3f
        )

        assertEquals(300L, policy.calculateResourceAllocation(1000L))
        assertEquals(150L, policy.calculateResourceAllocation(500L))
        assertEquals(0L, policy.calculateResourceAllocation(0L))
    }

    @Test
    fun testDefaultPolicy() {
        val policy = defaultPolicy()

        assertEquals(1, policy.selectionCycleYears)
        assertEquals(0.2f, policy.selectionRatio)
        assertEquals(0.3f, policy.resourceAllocationRatio)
        assertTrue(policy.isValid())
    }

    @Test
    fun testDefaultPolicySelectionCount() {
        val policy = defaultPolicy()

        assertEquals(20, policy.calculateSelectionCount(100))
        assertEquals(2, policy.calculateSelectionCount(10))
    }

    @Test
    fun testDefaultPolicyResourceAllocation() {
        val policy = defaultPolicy()

        assertEquals(300L, policy.calculateResourceAllocation(1000L))
        assertEquals(30L, policy.calculateResourceAllocation(100L))
    }
}
