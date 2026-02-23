package cn.jzl.sect.ai.goap

import cn.jzl.ecs.*
import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.world
import kotlin.test.*

/**
 * 前置条件测试
 */
class PreconditionTest : EntityRelationContext {
    override lateinit var world: World
    
    @BeforeTest
    fun setup() {
        world = world {}
    }
    
    @Test
    fun testCreatePrecondition() {
        val precondition = Precondition { _, _ -> true }
        
        assertNotNull(precondition, "应该能创建前置条件")
    }
    
    @Test
    fun testSatisfiesConditionTrue() {
        val precondition = Precondition { _, _ -> true }
        
        val entity = world.entity { }
        val state = WorldStateImpl(emptyMap())
        val result = precondition.satisfiesCondition(state, entity)
        
        assertTrue(result, "条件应该满足")
    }
    
    @Test
    fun testSatisfiesConditionFalse() {
        val precondition = Precondition { _, _ -> false }
        
        val entity = world.entity { }
        val state = WorldStateImpl(emptyMap())
        val result = precondition.satisfiesCondition(state, entity)
        
        assertFalse(result, "条件不应该满足")
    }
    
    @Test
    fun testPreconditionWithStateCheck() {
        val healthKey = object : StateKey<Int> {}
        val state = WorldStateImpl(mapOf(healthKey to 100))
        
        val precondition = Precondition { stateReader, agent ->
            stateReader.getValue(agent, healthKey) >= 50
        }
        
        val entity = world.entity { }
        val result = precondition.satisfiesCondition(state, entity)
        
        assertTrue(result, "生命值100应该满足>=50的条件")
    }
    
    @Test
    fun testPreconditionWithComplexLogic() {
        val healthKey = object : StateKey<Int> {}
        val staminaKey = object : StateKey<Int> {}
        
        val state = WorldStateImpl(mapOf(
            healthKey to 80,
            staminaKey to 60
        ))
        
        val precondition = Precondition { stateReader, agent ->
            val health = stateReader.getValue(agent, healthKey)
            val stamina = stateReader.getValue(agent, staminaKey)
            health > 50 && stamina > 50
        }
        
        val entity = world.entity { }
        val result = precondition.satisfiesCondition(state, entity)
        
        assertTrue(result, "两个条件都应该满足")
    }
}
