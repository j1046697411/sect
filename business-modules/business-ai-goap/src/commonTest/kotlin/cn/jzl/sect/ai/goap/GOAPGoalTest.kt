package cn.jzl.sect.ai.goap

import cn.jzl.ecs.*
import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.world
import kotlin.test.*

/**
 * GOAP 目标测试
 */
class GOAPGoalTest : EntityRelationContext {
    override lateinit var world: World
    
    @BeforeTest
    fun setup() {
        world = world {}
    }
    
    @Test
    fun testGoalPriority() {
        val lowPriorityGoal = TestGoal("低优先级", 1.0)
        val highPriorityGoal = TestGoal("高优先级", 10.0)
        
        assertTrue(highPriorityGoal.priority > lowPriorityGoal.priority, "高优先级目标应该有更大的优先级值")
    }
    
    @Test
    fun testGoalSatisfied() {
        val alwaysSatisfiedGoal = object : GOAPGoal {
            override val name: String = "总是满足的目标"
            override val priority: Double = 1.0
            
            override fun isSatisfied(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Boolean = true
            override fun calculateDesirability(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Double = 1.0
            override fun calculateHeuristic(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Double = 0.0
        }
        
        val entity = world.entity { }
        val state = WorldStateImpl(emptyMap())
        
        assertTrue(alwaysSatisfiedGoal.isSatisfied(state, entity), "目标应该被满足")
        assertEquals(0.0, alwaysSatisfiedGoal.calculateHeuristic(state, entity), "满足的目标启发式值应该是0")
    }
    
    @Test
    fun testGoalNotSatisfied() {
        val neverSatisfiedGoal = object : GOAPGoal {
            override val name: String = "永不满足的目标"
            override val priority: Double = 1.0
            
            override fun isSatisfied(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Boolean = false
            override fun calculateDesirability(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Double = 1.0
            override fun calculateHeuristic(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Double = 10.0
        }
        
        val entity = world.entity { }
        val state = WorldStateImpl(emptyMap())
        
        assertFalse(neverSatisfiedGoal.isSatisfied(state, entity), "目标不应该被满足")
        assertEquals(10.0, neverSatisfiedGoal.calculateHeuristic(state, entity), "未满足的目标应该有启发式值")
    }
    
    @Test
    fun testGoalDesirability() {
        val goal = object : GOAPGoal {
            override val name: String = "测试目标"
            override val priority: Double = 5.0
            
            override fun isSatisfied(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Boolean = false
            override fun calculateDesirability(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Double = priority * 2.0
            override fun calculateHeuristic(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Double = 1.0
        }
        
        val entity = world.entity { }
        val state = WorldStateImpl(emptyMap())
        val desirability = goal.calculateDesirability(state, entity)
        
        assertEquals(10.0, desirability, "期望度应该是优先级的2倍")
    }
    
    @Test
    fun testGoalWithStateCheck() {
        val healthKey = object : StateKey<Int> {}
        
        val goal = object : GOAPGoal {
            override val name: String = "生命值大于50"
            override val priority: Double = 1.0
            
            override fun isSatisfied(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Boolean {
                val health = worldState.getValue(agent, healthKey)
                return health > 50
            }
            
            override fun calculateDesirability(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Double {
                val health = worldState.getValue(agent, healthKey)
                return if (health < 50) 10.0 else 0.0
            }
            
            override fun calculateHeuristic(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Double {
                val health = worldState.getValue(agent, healthKey)
                return (50 - health).toDouble().coerceAtLeast(0.0)
            }
        }
        
        val entity = world.entity { }
        
        val lowHealthState = WorldStateImpl(mapOf(healthKey to 30))
        assertFalse(goal.isSatisfied(lowHealthState, entity), "生命值30不应该满足>50的条件")
        assertEquals(10.0, goal.calculateDesirability(lowHealthState, entity), "低生命值应该有高期望度")
        assertEquals(20.0, goal.calculateHeuristic(lowHealthState, entity), "启发式值应该是20")
        
        val highHealthState = WorldStateImpl(mapOf(healthKey to 80))
        assertTrue(goal.isSatisfied(highHealthState, entity), "生命值80应该满足>50的条件")
        assertEquals(0.0, goal.calculateDesirability(highHealthState, entity), "高生命值应该有低期望度")
        assertEquals(0.0, goal.calculateHeuristic(highHealthState, entity), "满足条件时启发式值应该是0")
    }
    
    private class TestGoal(
        override val name: String,
        override val priority: Double
    ) : GOAPGoal {
        override fun isSatisfied(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Boolean = false
        override fun calculateDesirability(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Double = priority
        override fun calculateHeuristic(worldState: WorldStateReader, agent: cn.jzl.ecs.entity.Entity): Double = 1.0
    }
}
