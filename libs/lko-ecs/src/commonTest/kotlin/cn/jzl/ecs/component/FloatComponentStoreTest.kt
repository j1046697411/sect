package cn.jzl.ecs.component

import kotlin.test.Test
import kotlin.test.assertEquals

class FloatComponentStoreTest {

    @Test
    fun testSize() {
        val store = FloatComponentStore()
        assertEquals(0, store.size)
        
        store.add(10.5f)
        assertEquals(1, store.size)
    }

    @Test
    fun testAddAndGet() {
        val store = FloatComponentStore()
        store.add(100.5f)
        store.add(200.5f)
        
        assertEquals(100.5f, store.get(0))
        assertEquals(200.5f, store.get(1))
    }

    @Test
    fun testSet() {
        val store = FloatComponentStore()
        store.add(10.5f)
        store.set(0, 999.9f)
        
        assertEquals(999.9f, store.get(0))
    }

    @Test
    fun testRemoveAt() {
        val store = FloatComponentStore()
        store.add(10.5f)
        store.add(20.5f)
        store.add(30.5f)
        
        val removed = store.removeAt(1)
        
        assertEquals(20.5f, removed)
        assertEquals(2, store.size)
    }

    @Test(expected = IllegalStateException::class)
    fun testSetNegativeIndex() {
        val store = FloatComponentStore()
        store.set(-1, 100f)
    }
}
