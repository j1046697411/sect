package cn.jzl.sect.ai.goap

import cn.jzl.ecs.*
import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.world
import kotlin.test.*

/**
 * 世界状态实现测试
 */
class WorldStateImplTest : EntityRelationContext {
    override lateinit var world: World
    
    @BeforeTest
    fun setup() {
        world = world {}
    }
    
    @Test
    fun testCreateWorldState() {
        val state = WorldStateImpl(emptyMap())
        
        assertNotNull(state, "应该能创建世界状态")
    }
    
    @Test
    fun testGetStateKeys() {
        val key1 = object : StateKey<Int> {}
        val key2 = object : StateKey<String> {}
        
        val state = WorldStateImpl(mapOf(
            key1 to 100,
            key2 to "test"
        ))
        
        val keys = state.stateKeys.toList()
        
        assertEquals(2, keys.size, "应该有2个状态键")
        assertTrue(keys.contains(key1), "应该包含第一个键")
        assertTrue(keys.contains(key2), "应该包含第二个键")
    }
    
    @Test
    fun testGetStateValue() {
        val healthKey = object : StateKey<Int> {}
        val state = WorldStateImpl(mapOf(healthKey to 100))
        
        val entity = world.entity { }
        val value = state.getValue(entity, healthKey)
        
        assertEquals(100, value, "应该能获取状态值")
    }
    
    @Test
    fun testGetStateValueWithDifferentTypes() {
        val intKey = object : StateKey<Int> {}
        val stringKey = object : StateKey<String> {}
        val boolKey = object : StateKey<Boolean> {}
        
        val state = WorldStateImpl(mapOf(
            intKey to 42,
            stringKey to "hello",
            boolKey to true
        ))
        
        val entity = world.entity { }
        
        assertEquals(42, state.getValue(entity, intKey), "应该能获取整数值")
        assertEquals("hello", state.getValue(entity, stringKey), "应该能获取字符串值")
        assertEquals(true, state.getValue(entity, boolKey), "应该能获取布尔值")
    }
    
    @Test
    fun testEmptyStateKeys() {
        val state = WorldStateImpl(emptyMap())
        
        val keys = state.stateKeys.toList()
        
        assertTrue(keys.isEmpty(), "空状态应该没有键")
    }
    
    @Test
    fun testGetNonExistentKey() {
        val state = WorldStateImpl(emptyMap())
        val entity = world.entity { }
        val key = object : StateKey<Int> {}
        
        assertFailsWith<NoSuchElementException> {
            state.getValue(entity, key)
        }
    }
    
    @Test
    fun testStateKeysIteration() {
        val key1 = object : StateKey<Int> {}
        val key2 = object : StateKey<Int> {}
        val key3 = object : StateKey<Int> {}
        
        val state = WorldStateImpl(mapOf(
            key1 to 1,
            key2 to 2,
            key3 to 3
        ))
        
        var count = 0
        state.stateKeys.forEach { count++ }
        
        assertEquals(3, count, "应该迭代3个状态键")
    }
}
