package cn.jzl.di

import org.kodein.type.erased
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class SearchSpecsTest {

    @Test
    fun testSearchSpecsDefaultCreation() {
        val specs = SearchSpecs()
        
        assertNull(specs.contextType)
        assertNull(specs.argType)
        assertNull(specs.targetType)
        assertNull(specs.tag)
    }

    @Test
    fun testSearchSpecsWithAllParameters() {
        val contextType = erased<String>()
        val argType = erased<Int>()
        val targetType = erased<Double>()
        val tag = "testTag"
        
        val specs = SearchSpecs(contextType, argType, targetType, tag)
        
        assertEquals(contextType, specs.contextType)
        assertEquals(argType, specs.argType)
        assertEquals(targetType, specs.targetType)
        assertEquals(tag, specs.tag)
    }

    @Test
    fun testSearchSpecsPartialParameters() {
        val contextType = erased<String>()
        val targetType = erased<Double>()
        
        val specs = SearchSpecs(contextType = contextType, targetType = targetType)
        
        assertEquals(contextType, specs.contextType)
        assertNull(specs.argType)
        assertEquals(targetType, specs.targetType)
        assertNull(specs.tag)
    }

    @Test
    fun testSearchSpecsEquality() {
        val specs1 = SearchSpecs(
            erased<String>(),
            erased<Int>(),
            erased<Double>(),
            "tag"
        )
        
        val specs2 = SearchSpecs(
            erased<String>(),
            erased<Int>(),
            erased<Double>(),
            "tag"
        )
        
        assertEquals(specs1, specs2)
    }

    @Test
    fun testSearchSpecsInequality() {
        val specs1 = SearchSpecs(
            erased<String>(),
            erased<Int>(),
            erased<Double>(),
            "tag1"
        )
        
        val specs2 = SearchSpecs(
            erased<String>(),
            erased<Int>(),
            erased<Double>(),
            "tag2"
        )
        
        assertNotEquals(specs1, specs2)
    }

    @Test
    fun testSearchSpecsWithNullTag() {
        val specs = SearchSpecs(
            contextType = erased<String>(),
            tag = null
        )
        
        assertNull(specs.tag)
    }
}
