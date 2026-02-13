package cn.jzl.core

import cn.jzl.core.list.IntFastList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IntFastListTest {

    @Test
    fun testEmptyList() {
        val list = IntFastList()
        assertEquals(0, list.size)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testInsertLast() {
        val list = IntFastList()
        list.insertLast(1)
        list.insertLast(2)
        list.insertLast(3)
        
        assertEquals(3, list.size)
        assertEquals(1, list[0])
        assertEquals(2, list[1])
        assertEquals(3, list[2])
    }

    @Test
    fun testInsertLastMultiple() {
        val list = IntFastList()
        list.insertLast(1, 2, 3, 4, 5, 6)
        
        assertEquals(6, list.size)
        for (i in 0..5) {
            assertEquals(i + 1, list[i])
        }
    }

    @Test
    fun testAdd() {
        val list = IntFastList()
        list.add(0, 10)
        list.add(1, 20)
        list.add(0, 5)
        
        assertEquals(3, list.size)
        assertEquals(5, list[0])
        assertEquals(10, list[1])
        assertEquals(20, list[2])
    }

    @Test
    fun testSet() {
        val list = IntFastList()
        list.insertLast(1, 2, 3)
        
        val old = list.set(1, 99)
        
        assertEquals(2, old)
        assertEquals(99, list[1])
    }

    @Test
    fun testRemoveAt() {
        val list = IntFastList()
        list.insertLast(1, 2, 3)
        
        val removed = list.removeAt(1)
        
        assertEquals(2, removed)
        assertEquals(2, list.size)
        assertEquals(1, list[0])
        assertEquals(3, list[1])
    }

    @Test
    fun testContains() {
        val list = IntFastList()
        list.insertLast(1, 2, 3)
        
        assertTrue(list.contains(2))
        assertFalse(list.contains(99))
    }

    @Test
    fun testIndexOf() {
        val list = IntFastList()
        list.insertLast(1, 2, 3, 2)
        
        assertEquals(1, list.indexOf(2))
        assertEquals(-1, list.indexOf(99))
    }

    @Test
    fun testIterator() {
        val list = IntFastList()
        list.insertLast(1, 2, 3)
        
        val elements = mutableListOf<Int>()
        for (element in list) {
            elements.add(element)
        }
        
        assertEquals(listOf(1, 2, 3), elements)
    }

    @Test
    fun testClear() {
        val list = IntFastList()
        list.insertLast(1, 2, 3)
        
        list.clear()
        
        assertEquals(0, list.size)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testEnsureCapacity() {
        val list = IntFastList()
        list.insertLast(1, 2)
        
        list.ensureCapacity(10, 0)
        
        assertEquals(10, list.size)
        assertEquals(1, list[0])
        assertEquals(2, list[1])
        assertEquals(0, list[2])
    }

    @Test
    fun testFill() {
        val list = IntFastList()
        list.insertLast(1, 2, 3, 4, 5)
        
        list.fill(9, 1, 4)
        
        assertEquals(1, list[0])
        assertEquals(9, list[1])
        assertEquals(9, list[2])
        assertEquals(9, list[3])
        assertEquals(5, list[4])
    }

    @Test
    fun testToArray() {
        val list = IntFastList()
        list.insertLast(1, 2, 3)
        
        val array = list.toIntArray()
        
        assertEquals(3, array.size)
        assertEquals(1, array[0])
        assertEquals(2, array[1])
        assertEquals(3, array[2])
    }

    @Test
    fun testSafeInsert() {
        val list = IntFastList()
        list.insertLast(1, 4)
        
        list.safeInsert(1, 2) {
            unsafeInsert(2)
            unsafeInsert(3)
        }
        
        assertEquals(4, list.size)
        assertEquals(1, list[0])
        assertEquals(2, list[1])
        assertEquals(3, list[2])
        assertEquals(4, list[3])
    }

    @Test
    fun testSafeInsertLast() {
        val list = IntFastList()
        
        list.safeInsertLast(3) {
            unsafeInsert(1)
            unsafeInsert(2)
            unsafeInsert(3)
        }
        
        assertEquals(3, list.size)
        assertEquals(1, list[0])
        assertEquals(2, list[1])
        assertEquals(3, list[2])
    }

    @Test
    fun testInsertAll() {
        val list = IntFastList()
        list.insertLast(1, 5)
        
        list.insertAll(1, listOf(2, 3, 4))
        
        assertEquals(5, list.size)
    }

    @Test
    fun testUnorderedMode() {
        val list = IntFastList(order = false)
        list.insertLast(1, 2, 3)
        
        list.removeAt(1)
        
        assertEquals(2, list.size)
        assertEquals(1, list[0])
        assertEquals(3, list[1])
    }
}
