package cn.jzl.sect.ai.goap

import cn.jzl.ecs.*
import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.world
import kotlin.test.*

/**
 * 动作测试
 */
class ActionTest : EntityRelationContext {
    override lateinit var world: World
    
    @BeforeTest
    fun setup() {
        world = world {}
    }
    
    @Test
    fun testCreateAction() {
        val action = Action(
            name = "测试动作",
            cost = 1.0,
            preconditions = emptySequence(),
            effects = emptySequence(),
            task = ActionTask {}
        )
        
        assertNotNull(action, "应该能创建动作")
        assertEquals("测试动作", action.name, "动作名称应该正确")
        assertEquals(1.0, action.cost, "动作成本应该正确")
    }
    
    @Test
    fun testActionWithPreconditions() {
        var preconditionChecked = false
        val precondition = Precondition { _, _ ->
            preconditionChecked = true
            true
        }
        
        val action = Action(
            name = "带前置条件的动作",
            cost = 1.0,
            preconditions = sequenceOf(precondition),
            effects = emptySequence(),
            task = ActionTask {}
        )
        
        val preconditions = action.preconditions.toList()
        assertEquals(1, preconditions.size, "应该有1个前置条件")
        
        val entity = world.entity { }
        val state = WorldStateImpl(emptyMap())
        val result = preconditions[0].satisfiesCondition(state, entity)
        
        assertTrue(result, "前置条件应该满足")
        assertTrue(preconditionChecked, "前置条件应该被检查")
    }
    
    @Test
    fun testActionWithEffects() {
        val healthKey = object : StateKey<Int> {}
        var effectApplied = false
        
        val effect = ActionEffect { stateWriter, _ ->
            effectApplied = true
            stateWriter.setValue(healthKey, 100)
        }
        
        val action = Action(
            name = "带效果的动作",
            cost = 1.0,
            preconditions = emptySequence(),
            effects = sequenceOf(effect),
            task = ActionTask {}
        )
        
        val effects = action.effects.toList()
        assertEquals(1, effects.size, "应该有1个效果")
        
        val entity = world.entity { }
        val mutableState = MutableWorldState()
        effects[0].apply(mutableState, entity)
        
        assertTrue(effectApplied, "效果应该被应用")
        assertEquals(100, mutableState.getValue(entity, healthKey), "状态应该被修改")
    }
    
    @Test
    fun testActionWithMultiplePreconditions() {
        val precondition1 = Precondition { _, _ -> true }
        val precondition2 = Precondition { _, _ -> true }
        val precondition3 = Precondition { _, _ -> true }
        
        val action = Action(
            name = "多前置条件动作",
            cost = 1.0,
            preconditions = sequenceOf(precondition1, precondition2, precondition3),
            effects = emptySequence(),
            task = ActionTask {}
        )
        
        val preconditions = action.preconditions.toList()
        assertEquals(3, preconditions.size, "应该有3个前置条件")
    }
    
    @Test
    fun testActionWithMultipleEffects() {
        val healthKey = object : StateKey<Int> {}
        val staminaKey = object : StateKey<Int> {}
        
        val effect1 = ActionEffect { stateWriter, _ -> stateWriter.setValue(healthKey, 100) }
        val effect2 = ActionEffect { stateWriter, _ -> stateWriter.setValue(staminaKey, 50) }
        
        val action = Action(
            name = "多效果动作",
            cost = 2.5,
            preconditions = emptySequence(),
            effects = sequenceOf(effect1, effect2),
            task = ActionTask {}
        )
        
        val effects = action.effects.toList()
        assertEquals(2, effects.size, "应该有2个效果")
        
        val entity = world.entity { }
        val mutableState = MutableWorldState()
        effects.forEach { it.apply(mutableState, entity) }
        
        assertEquals(100, mutableState.getValue(entity, healthKey), "生命值应该被设置")
        assertEquals(50, mutableState.getValue(entity, staminaKey), "耐力值应该被设置")
    }
    
    @Test
    fun testActionTask() {
        var taskExecuted = false
        
        val action = Action(
            name = "测试任务执行",
            cost = 1.0,
            preconditions = emptySequence(),
            effects = emptySequence(),
            task = ActionTask {
                taskExecuted = true
            }
        )
        
        action.task.execute()
        
        assertTrue(taskExecuted, "任务应该被执行")
    }
    
    @Test
    fun testActionCostComparison() {
        val cheapAction = Action(
            name = "低成本动作",
            cost = 1.0,
            preconditions = emptySequence(),
            effects = emptySequence(),
            task = ActionTask {}
        )
        
        val expensiveAction = Action(
            name = "高成本动作",
            cost = 10.0,
            preconditions = emptySequence(),
            effects = emptySequence(),
            task = ActionTask {}
        )
        
        assertTrue(cheapAction.cost < expensiveAction.cost, "低成本动作应该比高成本动作便宜")
    }
    
    private class MutableWorldState : WorldStateWriter {
        private val map = mutableMapOf<StateKey<*>, Any?>()
        
        override val stateKeys: Sequence<StateKey<*>> get() = map.keys.asSequence()
        
        @Suppress("UNCHECKED_CAST")
        override fun <K : StateKey<T>, T> getValue(agent: cn.jzl.ecs.entity.Entity, key: K): T {
            return map.getValue(key) as T
        }
        
        override fun <K : StateKey<T>, T> setValue(key: K, value: T) {
            map[key] = value
        }
    }
}
