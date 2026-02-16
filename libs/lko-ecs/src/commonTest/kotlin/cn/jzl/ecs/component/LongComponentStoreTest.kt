package cn.jzl.ecs.component

import kotlin.test.Test
import kotlin.test.assertEquals

class LongComponentStoreTest {

    @Test
    fun testSize() {
        val store = LongComponentStore()
        assertEquals(0, store.size)
        
        store.add(100L)
        assertEquals(1, store.size)
    }

    @Test
    fun testAddAndGet() {
        val store = LongComponentStore()
        store.add(1000L)
        store.add(2000L)
        
        assertEquals(1000L, store.get(0))
        assertEquals(2000L, store.get(1))
    }

    @Test
    fun testSet() {
        val store = LongComponentStore()
        store.add(10L)
        store.set(0, 999L)
        
        assertEquals(999L, store.get(0))
    }

    @Test
    fun testRemoveAt() {
        val store = LongComponentStore()
        store.add(10L)
        store.add(20L)
        store.add(30L)
        
        val removed = store.removeAt(1)
        
        assertEquals(20L, removed)
        assertEquals(2, store.size)
    }

    @Test(expected = IllegalStateException::class)
    fun testSetNegativeIndex() {
        val store = LongComponentStore()
        store.set(-1, 100L)
    }
}
