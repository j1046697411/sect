package cn.jzl.core

import cn.jzl.core.list.DoubleFastList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class DoubleFastListTest {

    @Test
    fun testEmptyList() {
        val list = DoubleFastList()
        assertEquals(0, list.size)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testInsertLast() {
        val list = DoubleFastList()
        list.insertLast(1.0)
        list.insertLast(2.0)
        list.insertLast(3.0)
        
        assertEquals(3, list.size)
        assertEquals(1.0, list[0])
        assertEquals(2.0, list[1])
        assertEquals(3.0, list[2])
    }

    @Test
    fun testInsertLastMultiple() {
        val list = DoubleFastList()
        list.insertLast(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
        
        assertEquals(6, list.size)
    }

    @Test
    fun testAdd() {
        val list = DoubleFastList()
        list.add(0, 10.0)
        list.add(1, 20.0)
        list.add(0, 5.0)
        
        assertEquals(3, list.size)
        assertEquals(5.0, list[0])
        assertEquals(10.0, list[1])
        assertEquals(20.0, list[2])
    }

    @Test
    fun testSet() {
        val list = DoubleFastList()
        list.insertLast(1.0, 2.0, 3.0)
        
        val old = list.set(1, 99.0)
        
        assertEquals(2.0, old)
        assertEquals(99.0, list[1])
    }

    @Test
    fun testRemoveAt() {
        val list = DoubleFastList()
        list.insertLast(1.0, 2.0, 3.0)
        
        val removed = list.removeAt(1)
        
        assertEquals(2.0, removed)
        assertEquals(2, list.size)
    }

    @Test
    fun testContains() {
        val list = DoubleFastList()
        list.insertLast(1.0, 2.0, 3.0)
        
        assertTrue(list.contains(2.0))
        assertFalse(list.contains(99.0))
    }

    @Test
    fun testClear() {
        val list = DoubleFastList()
        list.insertLast(1.0, 2.0, 3.0)
        
        list.clear()
        
        assertEquals(0, list.size)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testEnsureCapacity() {
        val list = DoubleFastList()
        list.insertLast(1.0, 2.0)
        
        list.ensureCapacity(10, 0.0)
        
        assertEquals(10, list.size)
    }

    @Test
    fun testFill() {
        val list = DoubleFastList()
        list.insertLast(1.0, 2.0, 3.0, 4.0, 5.0)
        
        list.fill(9.0, 1, 4)
        
        assertEquals(1.0, list[0])
        assertEquals(9.0, list[1])
        assertEquals(9.0, list[2])
        assertEquals(9.0, list[3])
        assertEquals(5.0, list[4])
    }

    @Test
    fun testSafeInsert() {
        val list = DoubleFastList()
        list.insertLast(1.0, 4.0)
        
        list.safeInsert(1, 2) {
            unsafeInsert(2.0)
            unsafeInsert(3.0)
        }
        
        assertEquals(4, list.size)
    }

    @Test
    fun testSafeInsertLast() {
        val list = DoubleFastList()
        
        list.safeInsertLast(3) {
            unsafeInsert(1.0)
            unsafeInsert(2.0)
            unsafeInsert(3.0)
        }
        
        assertEquals(3, list.size)
    }

    @Test
    fun testIterator() {
        val list = DoubleFastList()
        list.insertLast(1.0, 2.0, 3.0)
        
        val elements = mutableListOf<Double>()
        for (element in list) {
            elements.add(element)
        }
        
        assertEquals(3, elements.size)
    }

    @Test
    fun testUnorderedMode() {
        val list = DoubleFastList(order = false)
        list.insertLast(1.0, 2.0, 3.0)
        
        list.removeAt(1)
        
        assertEquals(2, list.size)
    }
}
