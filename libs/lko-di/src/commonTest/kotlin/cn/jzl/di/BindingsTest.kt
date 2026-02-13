package cn.jzl.di

import org.kodein.type.erased
import org.kodein.type.generic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BindingsTest {

    interface Service
    class ServiceImpl : Service
    
    interface Context
    class ContextImpl : Context

    @Test
    fun testPrototypeBinding() {
        val binding = Prototype(
            contextType = erased<Context>(),
            argType = erased<Unit>(),
            targetType = erased<Service>(),
            tag = null,
            fromModule = "test",
            factory = { ServiceImpl() }
        )
        
        assertEquals(erased<Context>(), binding.contextType)
        assertEquals(erased<Unit>(), binding.argType)
        assertEquals(erased<Service>(), binding.targetType)
        assertEquals("test", binding.fromModule)
        assertFalse(binding.supportSubTypes)
    }

    @Test
    fun testPrototypeBindingWithSupportSubTypes() {
        val binding = Prototype(
            contextType = erased<Context>(),
            argType = erased<Unit>(),
            targetType = erased<Service>(),
            tag = "tag",
            fromModule = "test",
            supportSubTypes = true,
            factory = { ServiceImpl() }
        )
        
        assertTrue(binding.supportSubTypes)
        assertEquals("tag", binding.tag)
    }

    @Test
    fun testSingletonBinding() {
        val noScope = NoScope()
        
        val binding = Singleton(
            scope = noScope,
            contextType = erased<Context>(),
            argType = erased<Unit>(),
            targetType = erased<Service>(),
            tag = null,
            fromModule = "test",
            factory = { ServiceImpl() }
        )
        
        assertEquals(erased<Context>(), binding.contextType)
        assertEquals(erased<Service>(), binding.targetType)
    }

    @Test
    fun testMultipleSingletonBinding() {
        val scope = ScopeImpl()
        
        val binding = MultipleSingleton(
            scope = scope,
            contextType = erased<Context>(),
            argType = erased<String>(),
            targetType = erased<Service>(),
            tag = null,
            fromModule = "test",
            factory = { ServiceImpl() }
        )
        
        assertEquals(erased<String>(), binding.argType)
    }

    @Test
    fun testBindingCopier() {
        val original = Prototype(
            contextType = erased<Context>(),
            argType = erased<Unit>(),
            targetType = erased<Service>(),
            tag = null,
            fromModule = "test",
            factory = { ServiceImpl() }
        )
        
        // Test copier creates a new binding
        val copied = original.copier
        
        // Copier should return a new DIBinding
        assertTrue(copied != null)
    }

    @Test
    fun testSingletonWithScopeId() {
        val noScope = NoScope()
        val scopeId = "customScopeId"
        
        val binding = Singleton(
            scope = noScope,
            contextType = erased<Context>(),
            argType = erased<Unit>(),
            targetType = erased<Service>(),
            tag = null,
            fromModule = "test",
            scopeId = scopeId,
            factory = { ServiceImpl() }
        )
        
        // Binding should be created with custom scopeId
        assertEquals(erased<Context>(), binding.contextType)
    }
}
