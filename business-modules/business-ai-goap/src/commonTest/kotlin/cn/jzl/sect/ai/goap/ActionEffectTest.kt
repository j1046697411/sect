package cn.jzl.sect.ai.goap

import cn.jzl.ecs.*
import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.world
import kotlin.test.*

/**
 * 动作效果测试
 */
class ActionEffectTest : EntityRelationContext {
    override lateinit var world: World
    
    @BeforeTest
    fun setup() {
        world = world {}
    }
    
    @Test
    fun testCreateActionEffect() {
        val effect = ActionEffect { _, _ -> }
        
        assertNotNull(effect, "应该能创建动作效果")
    }
    
    @Test
    fun testApplyEffectToState() {
        val healthKey = object : StateKey<Int> {}
        val effect = ActionEffect { stateWriter, _ ->
            stateWriter.setValue(healthKey, 100)
        }
        
        val entity = world.entity { }
        val mutableState = MutableWorldState()
        effect.apply(mutableState, entity)
        
        assertEquals(100, mutableState.getValue(entity, healthKey), "效果应该修改状态")
    }
    
    @Test
    fun testApplyMultipleEffects() {
        val healthKey = object : StateKey<Int> {}
        val staminaKey = object : StateKey<Int> {}
        
        val effect1 = ActionEffect { stateWriter, _ ->
            stateWriter.setValue(healthKey, 100)
        }
        val effect2 = ActionEffect { stateWriter, _ ->
            stateWriter.setValue(staminaKey, 50)
        }
        
        val entity = world.entity { }
        val mutableState = MutableWorldState()
        effect1.apply(mutableState, entity)
        effect2.apply(mutableState, entity)
        
        assertEquals(100, mutableState.getValue(entity, healthKey), "生命值应该被设置")
        assertEquals(50, mutableState.getValue(entity, staminaKey), "耐力值应该被设置")
    }
    
    @Test
    fun testEffectWithStateRead() {
        val healthKey = object : StateKey<Int> {}
        val effect = ActionEffect { stateWriter, _ ->
            val currentHealth = stateWriter.getValue(world.entity { }, healthKey)
            stateWriter.setValue(healthKey, currentHealth + 10)
        }
        
        val entity = world.entity { }
        val mutableState = MutableWorldState()
        mutableState.setValue(healthKey, 90)
        
        effect.apply(mutableState, entity)
        
        assertEquals(100, mutableState.getValue(entity, healthKey), "效果应该在当前值基础上增加")
    }
    
    @Test
    fun testEffectWithEntitySpecific() {
        val healthKey = object : StateKey<Int> {}
        val effect = ActionEffect { stateWriter, agent ->
            stateWriter.setValue(healthKey, agent.id * 10)
        }
        
        val entity1 = world.entity { }
        val entity2 = world.entity { }
        
        val mutableState1 = MutableWorldState()
        val mutableState2 = MutableWorldState()
        
        effect.apply(mutableState1, entity1)
        effect.apply(mutableState2, entity2)
        
        assertEquals(entity1.id * 10, mutableState1.getValue(entity1, healthKey), "效果应该根据实体ID计算")
        assertEquals(entity2.id * 10, mutableState2.getValue(entity2, healthKey), "效果应该根据实体ID计算")
    }
    
    @Test
    fun testEffectOverwrite() {
        val healthKey = object : StateKey<Int> {}
        
        val effect1 = ActionEffect { stateWriter, _ ->
            stateWriter.setValue(healthKey, 100)
        }
        val effect2 = ActionEffect { stateWriter, _ ->
            stateWriter.setValue(healthKey, 50)
        }
        
        val entity = world.entity { }
        val mutableState = MutableWorldState()
        
        effect1.apply(mutableState, entity)
        effect2.apply(mutableState, entity)
        
        assertEquals(50, mutableState.getValue(entity, healthKey), "后应用的效果应该覆盖前一个")
    }
    
    private class MutableWorldState : WorldStateWriter {
        private val map = mutableMapOf<StateKey<*>, Any?>()
        
        override val stateKeys: Sequence<StateKey<*>> get() = map.keys.asSequence()
        
        @Suppress("UNCHECKED_CAST")
        override fun <K : StateKey<T>, T> getValue(agent: cn.jzl.ecs.entity.Entity, key: K): T {
            return (map[key] ?: 0) as T
        }
        
        override fun <K : StateKey<T>, T> setValue(key: K, value: T) {
            map[key] = value
        }
    }
}
