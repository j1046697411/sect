package cn.jzl.core

import cn.jzl.core.list.FloatFastList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FloatFastListTest {

    @Test
    fun testEmptyList() {
        val list = FloatFastList()
        assertEquals(0, list.size)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testInsertLast() {
        val list = FloatFastList()
        list.insertLast(1.0f)
        list.insertLast(2.0f)
        list.insertLast(3.0f)
        
        assertEquals(3, list.size)
        assertEquals(1.0f, list[0])
        assertEquals(2.0f, list[1])
        assertEquals(3.0f, list[2])
    }

    @Test
    fun testInsertLastMultiple() {
        val list = FloatFastList()
        list.insertLast(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f)
        
        assertEquals(6, list.size)
    }

    @Test
    fun testAdd() {
        val list = FloatFastList()
        list.add(0, 10.0f)
        list.add(1, 20.0f)
        list.add(0, 5.0f)
        
        assertEquals(3, list.size)
        assertEquals(5.0f, list[0])
        assertEquals(10.0f, list[1])
        assertEquals(20.0f, list[2])
    }

    @Test
    fun testSet() {
        val list = FloatFastList()
        list.insertLast(1.0f, 2.0f, 3.0f)
        
        val old = list.set(1, 99.0f)
        
        assertEquals(2.0f, old)
        assertEquals(99.0f, list[1])
    }

    @Test
    fun testRemoveAt() {
        val list = FloatFastList()
        list.insertLast(1.0f, 2.0f, 3.0f)
        
        val removed = list.removeAt(1)
        
        assertEquals(2.0f, removed)
        assertEquals(2, list.size)
    }

    @Test
    fun testContains() {
        val list = FloatFastList()
        list.insertLast(1.0f, 2.0f, 3.0f)
        
        assertTrue(list.contains(2.0f))
        assertFalse(list.contains(99.0f))
    }

    @Test
    fun testClear() {
        val list = FloatFastList()
        list.insertLast(1.0f, 2.0f, 3.0f)
        
        list.clear()
        
        assertEquals(0, list.size)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testEnsureCapacity() {
        val list = FloatFastList()
        list.insertLast(1.0f, 2.0f)
        
        list.ensureCapacity(10, 0.0f)
        
        assertEquals(10, list.size)
    }

    @Test
    fun testFill() {
        val list = FloatFastList()
        list.insertLast(1.0f, 2.0f, 3.0f, 4.0f, 5.0f)
        
        list.fill(9.0f, 1, 4)
        
        assertEquals(1.0f, list[0])
        assertEquals(9.0f, list[1])
        assertEquals(9.0f, list[2])
        assertEquals(9.0f, list[3])
        assertEquals(5.0f, list[4])
    }

    @Test
    fun testSafeInsert() {
        val list = FloatFastList()
        list.insertLast(1.0f, 4.0f)
        
        list.safeInsert(1, 2) {
            unsafeInsert(2.0f)
            unsafeInsert(3.0f)
        }
        
        assertEquals(4, list.size)
    }

    @Test
    fun testSafeInsertLast() {
        val list = FloatFastList()
        
        list.safeInsertLast(3) {
            unsafeInsert(1.0f)
            unsafeInsert(2.0f)
            unsafeInsert(3.0f)
        }
        
        assertEquals(3, list.size)
    }

    @Test
    fun testIterator() {
        val list = FloatFastList()
        list.insertLast(1.0f, 2.0f, 3.0f)
        
        val elements = mutableListOf<Float>()
        for (element in list) {
            elements.add(element)
        }
        
        assertEquals(3, elements.size)
    }
}
