package cn.jzl.ecs.component

import kotlin.test.Test
import kotlin.test.assertEquals

class IntComponentStoreTest {

    @Test
    fun testSize() {
        val store = IntComponentStore()
        assertEquals(0, store.size)
        
        store.add(10)
        assertEquals(1, store.size)
    }

    @Test
    fun testAddAndGet() {
        val store = IntComponentStore()
        store.add(100)
        store.add(200)
        
        assertEquals(100, store.get(0))
        assertEquals(200, store.get(1))
    }

    @Test
    fun testSet() {
        val store = IntComponentStore()
        store.add(10)
        store.set(0, 999)
        
        assertEquals(999, store.get(0))
    }

    @Test
    fun testRemoveAt() {
        val store = IntComponentStore()
        store.add(10)
        store.add(20)
        store.add(30)
        
        val removed = store.removeAt(1)
        
        assertEquals(20, removed)
        assertEquals(2, store.size)
        assertEquals(10, store.get(0))
        assertEquals(30, store.get(1))
    }

    @Test(expected = IllegalStateException::class)
    fun testSetNegativeIndex() {
        val store = IntComponentStore()
        store.set(-1, 100)
    }
}
