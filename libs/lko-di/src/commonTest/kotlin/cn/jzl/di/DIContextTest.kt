package cn.jzl.di

import org.kodein.type.erased
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DIContextTest {

    interface MyContext
    class MyContextImpl : MyContext

    @Test
    fun testDIContextValueCreation() {
        val contextType = erased<MyContext>()
        val contextValue = MyContextImpl()
        
        val context = DIContext(contextType, contextValue)
        
        assertEquals(contextType, context.type)
        assertEquals(contextValue, context.value)
    }

    @Test
    fun testDIContextLazyCreation() {
        val contextType = erased<MyContext>()
        var createCount = 0
        val provider: DIProvider<MyContext> = { 
            createCount++
            MyContextImpl()
        }
        
        val context = DIContext(contextType, provider)
        
        // First access should create the value
        val value1 = context.value
        assertEquals(1, createCount)
        
        // Second access should return cached value
        val value2 = context.value
        assertEquals(1, createCount)
        
        assertSame(value1, value2)
    }

    @Test
    fun testDIContextAny() {
        // DIContext.Any should be assignable to any type
        val anyContext = DIContext
        
        assertTrue(anyContext.type.isAssignableFrom(erased<String>()))
        assertTrue(anyContext.type.isAssignableFrom(erased<Int>()))
    }

    @Test
    fun testDIContextInvokeOperator() {
        val contextType = erased<MyContext>()
        val contextValue = MyContextImpl()
        
        // Using invoke operator
        val context = DIContext(contextType, contextValue)
        
        assertEquals(contextValue, context.value)
    }

    @Test
    fun testDIContextEquals() {
        val contextType = erased<MyContext>()
        val contextValue = MyContextImpl()
        
        val context1 = DIContext(contextType, contextValue)
        val context2 = DIContext(contextType, contextValue)
        
        // Should be equal if type and value are same
        assertEquals(context1, context2)
    }

    @Test
    fun testDIContextWithDifferentValues() {
        val contextType = erased<MyContext>()
        
        val context1 = DIContext(contextType, MyContextImpl())
        val context2 = DIContext(contextType, MyContextImpl())
        
        // Different instances should not be same
        assertNotSame(context1.value, context2.value)
    }
}
