package cn.jzl.core

import cn.jzl.core.list.ShortFastList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ShortFastListTest {

    @Test
    fun testEmptyList() {
        val list = ShortFastList()
        assertEquals(0, list.size)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testInsertLast() {
        val list = ShortFastList()
        list.insertLast(1.toShort())
        list.insertLast(2.toShort())
        list.insertLast(3.toShort())
        
        assertEquals(3, list.size)
        assertEquals(1.toShort(), list[0])
        assertEquals(2.toShort(), list[1])
        assertEquals(3.toShort(), list[2])
    }

    @Test
    fun testInsertLastMultiple() {
        val list = ShortFastList()
        list.insertLast(1.toShort(), 2.toShort(), 3.toShort())
        
        assertEquals(3, list.size)
    }

    @Test
    fun testAdd() {
        val list = ShortFastList()
        list.add(0, 10.toShort())
        list.add(1, 20.toShort())
        
        assertEquals(2, list.size)
    }

    @Test
    fun testSet() {
        val list = ShortFastList()
        list.insertLast(1.toShort(), 2.toShort(), 3.toShort())
        
        val old = list.set(1, 99.toShort())
        
        assertEquals(2.toShort(), old)
        assertEquals(99.toShort(), list[1])
    }

    @Test
    fun testRemoveAt() {
        val list = ShortFastList()
        list.insertLast(1.toShort(), 2.toShort(), 3.toShort())
        
        val removed = list.removeAt(1)
        
        assertEquals(2.toShort(), removed)
        assertEquals(2, list.size)
    }

    @Test
    fun testClear() {
        val list = ShortFastList()
        list.insertLast(1.toShort(), 2.toShort())
        
        list.clear()
        
        assertEquals(0, list.size)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testIterator() {
        val list = ShortFastList()
        list.insertLast(1.toShort(), 2.toShort())
        
        val elements = mutableListOf<Short>()
        for (element in list) {
            elements.add(element)
        }
        
        assertEquals(2, elements.size)
    }

    @Test
    fun testContains() {
        val list = ShortFastList()
        list.insertLast(1.toShort(), 2.toShort())
        
        assertTrue(list.contains(1.toShort()))
        assertFalse(list.contains(99.toShort()))
    }
}
