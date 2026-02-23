package cn.jzl.sect.ai.goap

import kotlin.test.*

/**
 * 计划测试
 */
class PlanTest {
    
    @Test
    fun testCreatePlan() {
        val goal = TestGoal("测试目标", 1.0)
        val actions = listOf(
            TestAction("动作1", 1.0),
            TestAction("动作2", 2.0)
        )
        
        val plan = Plan(goal, actions, 3.0)
        
        assertNotNull(plan, "应该能创建计划")
        assertEquals("测试目标", plan.goal.name, "目标名称应该正确")
        assertEquals(2, plan.actions.size, "应该有2个动作")
        assertEquals(3.0, plan.cost, "总成本应该是3.0")
    }
    
    @Test
    fun testEmptyPlan() {
        val goal = TestGoal("空计划目标", 1.0)
        
        val plan = Plan(goal, emptyList(), 0.0)
        
        assertEquals(0, plan.actions.size, "空计划应该没有动作")
        assertEquals(0.0, plan.cost, "空计划成本应该是0")
    }
    
    @Test
    fun testPlanEquality() {
        val goal = TestGoal("相同目标", 1.0)
        val actions = listOf(TestAction("动作", 1.0))
        
        val plan1 = Plan(goal, actions, 1.0)
        val plan2 = Plan(goal, actions, 1.0)
        
        assertEquals(plan1, plan2, "相同的计划应该相等")
    }
    
    @Test
    fun testPlanCopy() {
        val goal = TestGoal("目标", 1.0)
        val actions = listOf(TestAction("动作", 1.0))
        
        val original = Plan(goal, actions, 1.0)
        val copied = original.copy()
        
        assertEquals(original, copied, "复制的计划应该相等")
    }
    
    private class TestGoal(
        override val name: String,
        override val priority: Double
    ) : GOAPGoal {
        override fun isSatisfied(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Boolean = false
        override fun calculateDesirability(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Double = priority
        override fun calculateHeuristic(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Double = 1.0
    }
    
    private class TestAction(
        override val name: String,
        override val cost: Double
    ) : GOAPAction {
        override val preconditions: Sequence<Precondition> = emptySequence()
        override val effects: Sequence<ActionEffect> = emptySequence()
        override val task: ActionTask = ActionTask {}
    }
}
