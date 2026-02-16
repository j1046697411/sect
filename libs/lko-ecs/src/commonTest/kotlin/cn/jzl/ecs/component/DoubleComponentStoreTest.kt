package cn.jzl.ecs.component

import kotlin.test.Test
import kotlin.test.assertEquals

class DoubleComponentStoreTest {

    @Test
    fun testSize() {
        val store = DoubleComponentStore()
        assertEquals(0, store.size)
        
        store.add(10.5)
        assertEquals(1, store.size)
    }

    @Test
    fun testAddAndGet() {
        val store = DoubleComponentStore()
        store.add(100.5)
        store.add(200.5)
        
        assertEquals(100.5, store.get(0))
        assertEquals(200.5, store.get(1))
    }

    @Test
    fun testSet() {
        val store = DoubleComponentStore()
        store.add(10.5)
        store.set(0, 999.9)
        
        assertEquals(999.9, store.get(0))
    }

    @Test
    fun testRemoveAt() {
        val store = DoubleComponentStore()
        store.add(10.5)
        store.add(20.5)
        store.add(30.5)
        
        val removed = store.removeAt(1)
        
        assertEquals(20.5, removed)
        assertEquals(2, store.size)
    }

    @Test(expected = IllegalStateException::class)
    fun testSetNegativeIndex() {
        val store = DoubleComponentStore()
        store.set(-1, 100.0)
    }
}
