package cn.jzl.core

import cn.jzl.core.list.ByteFastList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ByteFastListTest {

    @Test
    fun testEmptyList() {
        val list = ByteFastList()
        assertEquals(0, list.size)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testInsertLast() {
        val list = ByteFastList()
        list.insertLast(1.toByte())
        list.insertLast(2.toByte())
        
        assertEquals(2, list.size)
        assertEquals(1.toByte(), list[0])
        assertEquals(2.toByte(), list[1])
    }

    @Test
    fun testInsertLastMultiple() {
        val list = ByteFastList()
        list.insertLast(1.toByte(), 2.toByte(), 3.toByte())
        
        assertEquals(3, list.size)
    }

    @Test
    fun testAdd() {
        val list = ByteFastList()
        list.add(0, 10.toByte())
        
        assertEquals(1, list.size)
    }

    @Test
    fun testSet() {
        val list = ByteFastList()
        list.insertLast(1.toByte(), 2.toByte())
        
        val old = list.set(0, 99.toByte())
        
        assertEquals(1.toByte(), old)
        assertEquals(99.toByte(), list[0])
    }

    @Test
    fun testRemoveAt() {
        val list = ByteFastList()
        list.insertLast(1.toByte(), 2.toByte())
        
        val removed = list.removeAt(0)
        
        assertEquals(1.toByte(), removed)
        assertEquals(1, list.size)
    }

    @Test
    fun testClear() {
        val list = ByteFastList()
        list.insertLast(1.toByte())
        
        list.clear()
        
        assertEquals(0, list.size)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testContains() {
        val list = ByteFastList()
        list.insertLast(1.toByte(), 2.toByte())
        
        assertTrue(list.contains(1.toByte()))
        assertFalse(list.contains(99.toByte()))
    }

    @Test
    fun testIterator() {
        val list = ByteFastList()
        list.insertLast(1.toByte(), 2.toByte())
        
        var count = 0
        for (element in list) {
            count++
        }
        assertEquals(2, count)
    }
}
