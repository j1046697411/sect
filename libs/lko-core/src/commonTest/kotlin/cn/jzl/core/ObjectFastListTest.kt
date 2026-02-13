package cn.jzl.core

import cn.jzl.core.list.ObjectFastList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ObjectFastListTest {

    @Test
    fun testEmptyList() {
        val list = ObjectFastList<String>()
        assertEquals(0, list.size)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testInsertLast() {
        val list = ObjectFastList<String>()
        list.insertLast("a")
        list.insertLast("b")
        list.insertLast("c")
        
        assertEquals(3, list.size)
        assertEquals("a", list[0])
        assertEquals("b", list[1])
        assertEquals("c", list[2])
    }

    @Test
    fun testInsertLastMultiple() {
        val list = ObjectFastList<String>()
        list.insertLast("a", "b", "c", "d", "e", "f")
        
        assertEquals(6, list.size)
        assertEquals("a", list[0])
        assertEquals("f", list[5])
    }

    @Test
    fun testAdd() {
        val list = ObjectFastList<String>()
        list.add(0, "x")
        list.add(1, "y")
        list.add(0, "z")
        
        assertEquals(3, list.size)
        assertEquals("z", list[0])
        assertEquals("x", list[1])
        assertEquals("y", list[2])
    }

    @Test
    fun testSet() {
        val list = ObjectFastList<String>()
        list.insertLast("a", "b", "c")
        
        val old = list.set(1, "z")
        
        assertEquals("b", old)
        assertEquals("z", list[1])
    }

    @Test
    fun testRemoveAt() {
        val list = ObjectFastList<String>()
        list.insertLast("a", "b", "c")
        
        val removed = list.removeAt(1)
        
        assertEquals("b", removed)
        assertEquals(2, list.size)
        assertEquals("a", list[0])
        assertEquals("c", list[1])
    }

    @Test
    fun testContains() {
        val list = ObjectFastList<String>()
        list.insertLast("a", "b", "c")
        
        assertTrue(list.contains("b"))
        assertFalse(list.contains("z"))
    }

    @Test
    fun testIndexOf() {
        val list = ObjectFastList<String>()
        list.insertLast("a", "b", "c", "b")
        
        assertEquals(1, list.indexOf("b"))
        assertEquals(-1, list.indexOf("z"))
    }

    @Test
    fun testIterator() {
        val list = ObjectFastList<String>()
        list.insertLast("a", "b", "c")
        
        val elements = mutableListOf<String>()
        for (element in list) {
            elements.add(element)
        }
        
        assertEquals(listOf("a", "b", "c"), elements)
    }

    @Test
    fun testClear() {
        val list = ObjectFastList<String>()
        list.insertLast("a", "b", "c")
        
        list.clear()
        
        assertEquals(0, list.size)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testToArray() {
        val list = ObjectFastList<String>()
        list.insertLast("a", "b", "c")
        
        val array = list.toTypedArray()
        
        assertEquals(3, array.size)
        assertEquals("a", array[0])
        assertEquals("b", array[1])
        assertEquals("c", array[2])
    }

    @Test
    fun testSafeInsert() {
        val list = ObjectFastList<String>()
        list.insertLast("a", "d")
        
        list.safeInsert(1, 2) {
            unsafeInsert("b")
            unsafeInsert("c")
        }
        
        assertEquals(4, list.size)
        assertEquals("a", list[0])
        assertEquals("b", list[1])
        assertEquals("c", list[2])
        assertEquals("d", list[3])
    }

    @Test
    fun testSafeInsertLast() {
        val list = ObjectFastList<String>()
        
        list.safeInsertLast(3) {
            unsafeInsert("a")
            unsafeInsert("b")
            unsafeInsert("c")
        }
        
        assertEquals(3, list.size)
        assertEquals("a", list[0])
        assertEquals("b", list[1])
        assertEquals("c", list[2])
    }

    @Test
    fun testInsertAll() {
        val list = ObjectFastList<String>()
        list.insertLast("a", "e")
        
        list.insertAll(1, listOf("b", "c", "d"))
        
        assertEquals(5, list.size)
    }

    @Test
    fun testUnorderedMode() {
        val list = ObjectFastList<String>(order = false)
        list.insertLast("a", "b", "c")
        
        list.removeAt(1)
        
        assertEquals(2, list.size)
        assertEquals("a", list[0])
        assertEquals("c", list[1])
    }

    @Test
    fun testWithNullElements() {
        val list = ObjectFastList<String?>()
        list.insertLast("a", null, "c")
        
        assertEquals(3, list.size)
        assertEquals("a", list[0])
        assertNull(list[1])
        assertEquals("c", list[2])
    }

    @Test
    fun testRemove() {
        val list = ObjectFastList<String>()
        list.insertLast("a", "b", "c")
        
        val result = list.remove("b")
        
        assertTrue(result)
        assertEquals(2, list.size)
        assertEquals("a", list[0])
        assertEquals("c", list[1])
    }

    @Test
    fun testRemoveAll() {
        val list = ObjectFastList<String>()
        list.insertLast("a", "b", "c", "d")
        
        list.removeAll(listOf("b", "d"))
        
        assertEquals(2, list.size)
        assertEquals("a", list[0])
        assertEquals("c", list[1])
    }

    @Test
    fun testRetainAll() {
        val list = ObjectFastList<String>()
        list.insertLast("a", "b", "c", "d")
        
        list.retainAll(listOf("b", "d"))
        
        assertEquals(2, list.size)
        assertEquals("b", list[0])
        assertEquals("d", list[1])
    }
}
