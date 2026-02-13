package cn.jzl.di

import org.kodein.type.erased
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DITreeKeyTest {

    @Test
    fun testDITreeKeyCreation() {
        val contextType = erased<String>()
        val argType = erased<Int>()
        val targetType = erased<Double>()
        val scopeType = erased<Any>()
        val tag = "testTag"
        
        val key = DITreeKey(contextType, argType, targetType, scopeType, tag)
        
        assertEquals(contextType, key.contextType)
        assertEquals(argType, key.argType)
        assertEquals(targetType, key.targetType)
        assertEquals(scopeType, key.scopeType)
        assertEquals(tag, key.tag)
    }

    @Test
    fun testDITreeKeyEquality() {
        val key1 = DITreeKey(
            erased<String>(),
            erased<Int>(),
            erased<Double>(),
            erased<Any>(),
            "tag"
        )
        
        val key2 = DITreeKey(
            erased<String>(),
            erased<Int>(),
            erased<Double>(),
            erased<Any>(),
            "tag"
        )
        
        assertEquals(key1, key2)
    }

    @Test
    fun testDITreeKeyInequality() {
        val key1 = DITreeKey(
            erased<String>(),
            erased<Int>(),
            erased<Double>(),
            erased<Any>(),
            "tag1"
        )
        
        val key2 = DITreeKey(
            erased<String>(),
            erased<Int>(),
            erased<Double>(),
            erased<Any>(),
            "tag2"
        )
        
        assertNotEquals(key1, key2)
    }

    @Test
    fun testDITreeKeyWithNullTag() {
        val key = DITreeKey(
            erased<String>(),
            erased<Int>(),
            erased<Double>(),
            erased<Any>(),
            null
        )
        
        assertEquals(null, key.tag)
    }

    @Test
    fun testDITreeKeyDataClass() {
        val key = DITreeKey(
            erased<String>(),
            erased<Int>(),
            erased<Double>(),
            erased<Any>(),
            "tag"
        )
        
        // Test copy
        val copied = key.copy(tag = "newTag")
        assertEquals("newTag", copied.tag)
        assertEquals(key.contextType, copied.contextType)
    }
}
