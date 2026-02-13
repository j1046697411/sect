package cn.jzl.core

import cn.jzl.core.list.CharFastList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class CharFastListTest {

    @Test
    fun testEmptyList() {
        val list = CharFastList()
        assertEquals(0, list.size)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testInsertLast() {
        val list = CharFastList()
        list.insertLast('a')
        list.insertLast('b')
        list.insertLast('c')
        
        assertEquals(3, list.size)
        assertEquals('a', list[0])
        assertEquals('b', list[1])
        assertEquals('c', list[2])
    }

    @Test
    fun testInsertLastMultiple() {
        val list = CharFastList()
        list.insertLast('a', 'b', 'c', 'd')
        
        assertEquals(4, list.size)
    }

    @Test
    fun testAdd() {
        val list = CharFastList()
        list.add(0, 'x')
        list.add(1, 'y')
        
        assertEquals(2, list.size)
    }

    @Test
    fun testSet() {
        val list = CharFastList()
        list.insertLast('a', 'b', 'c')
        
        val old = list.set(1, 'z')
        
        assertEquals('b', old)
        assertEquals('z', list[1])
    }

    @Test
    fun testRemoveAt() {
        val list = CharFastList()
        list.insertLast('a', 'b', 'c')
        
        val removed = list.removeAt(1)
        
        assertEquals('b', removed)
        assertEquals(2, list.size)
    }

    @Test
    fun testClear() {
        val list = CharFastList()
        list.insertLast('a', 'b')
        
        list.clear()
        
        assertEquals(0, list.size)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testContains() {
        val list = CharFastList()
        list.insertLast('a', 'b', 'c')
        
        assertTrue(list.contains('b'))
        assertFalse(list.contains('z'))
    }

    @Test
    fun testIterator() {
        val list = CharFastList()
        list.insertLast('a', 'b', 'c')
        
        val elements = mutableListOf<Char>()
        for (element in list) {
            elements.add(element)
        }
        
        assertEquals(3, elements.size)
    }

    @Test
    fun testEnsureCapacity() {
        val list = CharFastList()
        list.insertLast('a', 'b')
        
        list.ensureCapacity(10, '0')
        
        assertEquals(10, list.size)
    }

    @Test
    fun testFill() {
        val list = CharFastList()
        list.insertLast('a', 'b', 'c', 'd', 'e')
        
        list.fill('x', 1, 4)
        
        assertEquals('a', list[0])
        assertEquals('x', list[1])
        assertEquals('x', list[2])
        assertEquals('x', list[3])
        assertEquals('e', list[4])
    }

    @Test
    fun testSafeInsert() {
        val list = CharFastList()
        list.insertLast('a', 'd')
        
        list.safeInsert(1, 2) {
            unsafeInsert('b')
            unsafeInsert('c')
        }
        
        assertEquals(4, list.size)
    }

    @Test
    fun testSafeInsertLast() {
        val list = CharFastList()
        
        list.safeInsertLast(3) {
            unsafeInsert('a')
            unsafeInsert('b')
            unsafeInsert('c')
        }
        
        assertEquals(3, list.size)
    }
}
