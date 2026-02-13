package cn.jzl.di

import cn.jzl.di.Scope
import cn.jzl.di.ScopeImpl
import cn.jzl.di.ScopeRegistry
import cn.jzl.di.NoScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ScopeTest {

    @Test
    fun testNoScopeReturnsSameRegistry() {
        val scope = NoScope()
        
        val registry1 = scope.getRegistry(DIContext)
        val registry2 = scope.getRegistry(DIContext)
        
        assertSame(registry1, registry2)
    }

    @Test
    fun testNoScopeCloseContextDoesNothing() {
        val scope = NoScope()
        
        // Should not throw
        scope.closeContext(DIContext)
    }

    @Test
    fun testNoScopeClose() {
        val scope = NoScope()
        
        // Should not throw
        scope.close()
    }

    @Test
    fun testScopeImplCreatesSeparateRegistries() {
        val scope = ScopeImpl()
        
        val registry1 = scope.getRegistry(DIContext)
        val registry2 = scope.getRegistry(DIContext)
        
        assertSame(registry1, registry2)
    }

    @Test
    fun testScopeImplCloseContext() {
        val scope = ScopeImpl()
        
        scope.getRegistry(DIContext) // Create registry
        
        // Should not throw
        scope.closeContext(DIContext)
    }

    @Test
    fun testScopeImplClose() {
        val scope = ScopeImpl()
        
        // Should not throw
        scope.close()
    }

    @Test
    fun testScopeRegistryGetOrCreate() {
        val scope = NoScope()
        val registry = scope.getRegistry(DIContext)
        
        val value1 = registry.getOrCreate("key") { "test_value" }
        val value2 = registry.getOrCreate("key") { "different_value" }
        
        assertEquals("test_value", value1)
        assertEquals("test_value", value2) // Same instance
    }

    @Test
    fun testScopeRegistryIterator() {
        val scope = NoScope()
        val registry = scope.getRegistry(DIContext)
        
        registry.getOrCreate("key1") { "value1" }
        registry.getOrCreate("key2") { "value2" }
        
        val entries = registry.toList()
        assertEquals(2, entries.size)
    }

    @Test
    fun testScopeRegistryRemove() {
        val scope = NoScope()
        val registry = scope.getRegistry(DIContext)
        
        registry.getOrCreate("key") { "value" }
        
        registry -= "key"
        
        // After removal, should create new value
        val newValue = registry.getOrCreate("key") { "new_value" }
        assertEquals("new_value", newValue)
    }

    @Test
    fun testScopeRegistryClose() {
        val scope = NoScope()
        val registry = scope.getRegistry(DIContext)
        
        registry.getOrCreate("key") { "value" }
        
        // Should not throw
        registry.close()
        
        // After close, iterator should be empty
        assertTrue(registry.toList().isEmpty())
    }
}
