package cn.jzl.core

import cn.jzl.core.list.LongFastList
import cn.jzl.core.list.MutableFastList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LongFastListTest {

    @Test
    fun testEmptyList() {
        val list = LongFastList()
        assertEquals(0, list.size)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testInsertLast() {
        val list = LongFastList()
        list.insertLast(1L)
        list.insertLast(2L)
        list.insertLast(3L)
        
        assertEquals(3, list.size)
        assertEquals(1L, list[0])
        assertEquals(2L, list[1])
        assertEquals(3L, list[2])
    }

    @Test
    fun testInsertLastMultiple() {
        val list = LongFastList()
        list.insertLast(1L, 2L, 3L, 4L, 5L, 6L)
        
        assertEquals(6, list.size)
        for (i in 0..5) {
            assertEquals((i + 1).toLong(), list[i])
        }
    }

    @Test
    fun testAdd() {
        val list = LongFastList()
        list.add(0, 10L)
        list.add(1, 20L)
        list.add(0, 5L)
        
        assertEquals(3, list.size)
        assertEquals(5L, list[0])
        assertEquals(10L, list[1])
        assertEquals(20L, list[2])
    }

    @Test
    fun testSet() {
        val list = LongFastList()
        list.insertLast(1L, 2L, 3L)
        
        val old = list.set(1, 99L)
        
        assertEquals(2L, old)
        assertEquals(99L, list[1])
    }

    @Test
    fun testRemoveAt() {
        val list = LongFastList()
        list.insertLast(1L, 2L, 3L)
        
        val removed = list.removeAt(1)
        
        assertEquals(2L, removed)
        assertEquals(2, list.size)
        assertEquals(1L, list[0])
        assertEquals(3L, list[1])
    }

    @Test
    fun testRemoveAtLastIndex() {
        val list = LongFastList()
        list.insertLast(1L, 2L, 3L)
        
        val removed = list.removeAt(2)
        
        assertEquals(3L, removed)
        assertEquals(2, list.size)
    }

    @Test
    fun testContains() {
        val list = LongFastList()
        list.insertLast(1L, 2L, 3L)
        
        assertTrue(list.contains(2L))
        assertFalse(list.contains(99L))
    }

    @Test
    fun testIndexOf() {
        val list = LongFastList()
        list.insertLast(1L, 2L, 3L, 2L)
        
        assertEquals(1, list.indexOf(2L))
        assertEquals(-1, list.indexOf(99L))
    }

    @Test
    fun testLastIndexOf() {
        val list = LongFastList()
        list.insertLast(1L, 2L, 3L, 2L)
        
        assertEquals(3, list.lastIndexOf(2L))
    }

    @Test
    fun testIterator() {
        val list = LongFastList()
        list.insertLast(1L, 2L, 3L)
        
        val elements = mutableListOf<Long>()
        for (element in list) {
            elements.add(element)
        }
        
        assertEquals(listOf(1L, 2L, 3L), elements)
    }

    @Test
    fun testClear() {
        val list = LongFastList()
        list.insertLast(1L, 2L, 3L)
        
        list.clear()
        
        assertEquals(0, list.size)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testToArray() {
        val list = LongFastList()
        list.insertLast(1L, 2L, 3L)
        
        val array = list.toLongArray()
        
        assertEquals(3, array.size)
        assertEquals(1L, array[0])
        assertEquals(2L, array[1])
        assertEquals(3L, array[2])
    }

    @Test
    fun testEnsureCapacity() {
        val list = LongFastList()
        list.insertLast(1L, 2L)
        
        list.ensureCapacity(10, 0L)
        
        assertEquals(10, list.size)
        assertEquals(1L, list[0])
        assertEquals(2L, list[1])
        // 新增元素应该被填充为 0L
        assertEquals(0L, list[2])
    }

    @Test
    fun testFill() {
        val list = LongFastList()
        list.insertLast(1L, 2L, 3L, 4L, 5L)
        
        list.fill(9L, 1, 4)
        
        assertEquals(1L, list[0])
        assertEquals(9L, list[1])
        assertEquals(9L, list[2])
        assertEquals(9L, list[3])
        assertEquals(5L, list[4])
    }

    @Test
    fun testSafeInsert() {
        val list = LongFastList()
        list.insertLast(1L, 4L)
        
        list.safeInsert(1, 2) {
            unsafeInsert(2L)
            unsafeInsert(3L)
        }
        
        assertEquals(4, list.size)
        assertEquals(1L, list[0])
        assertEquals(2L, list[1])
        assertEquals(3L, list[2])
        assertEquals(4L, list[3])
    }

    @Test
    fun testSafeInsertLast() {
        val list = LongFastList()
        
        list.safeInsertLast(3) {
            unsafeInsert(1L)
            unsafeInsert(2L)
            unsafeInsert(3L)
        }
        
        assertEquals(3, list.size)
        assertEquals(1L, list[0])
        assertEquals(2L, list[1])
        assertEquals(3L, list[2])
    }

    @Test
    fun testInsertAll() {
        val list = LongFastList()
        list.insertLast(1L, 5L)
        
        list.insertAll(1, listOf(2L, 3L, 4L))
        
        assertEquals(5, list.size)
        assertEquals(1L, list[0])
        assertEquals(2L, list[1])
        assertEquals(3L, list[2])
        assertEquals(4L, list[3])
        assertEquals(5L, list[4])
    }

    @Test
    fun testInsertLastAll() {
        val list = LongFastList()
        list.insertLast(1L)
        list.insertLastAll(listOf(2L, 3L, 4L))
        
        assertEquals(4, list.size)
    }

    @Test
    fun testRemove() {
        val list = LongFastList()
        list.insertLast(1L, 2L, 3L)
        
        val result = list.remove(2L)
        
        assertTrue(result)
        assertEquals(2, list.size)
        assertEquals(1L, list[0])
        assertEquals(3L, list[1])
    }

    @Test
    fun testRemoveAll() {
        val list = LongFastList()
        list.insertLast(1L, 2L, 3L, 4L)
        
        list.removeAll(listOf(2L, 4L))
        
        assertEquals(2, list.size)
        assertEquals(1L, list[0])
        assertEquals(3L, list[1])
    }

    @Test
    fun testRetainAll() {
        val list = LongFastList()
        list.insertLast(1L, 2L, 3L, 4L)
        
        list.retainAll(listOf(2L, 4L))
        
        assertEquals(2, list.size)
        assertEquals(2L, list[0])
        assertEquals(4L, list[1])
    }

    @Test
    fun testAddAll() {
        val list = LongFastList()
        list.addAll(listOf(1L, 2L, 3L))
        
        assertEquals(3, list.size)
    }

    @Test
    fun testWithInitialCapacity() {
        val list = LongFastList(100)
        
        list.insertLast(1L)
        
        assertEquals(1, list.size)
    }

    @Test
    fun testUnorderedMode() {
        val list = LongFastList(order = false)
        list.insertLast(1L, 2L, 3L)
        
        list.removeAt(1)
        
        // 在 unordered 模式下，被删除的元素会被最后一个元素替代
        assertEquals(2, list.size)
        assertEquals(1L, list[0])
        assertEquals(3L, list[1])
    }
}
