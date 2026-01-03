package cn.jzl.di

typealias ScopeCloseable = AutoCloseable
typealias RegKey = Any

interface Scope : ScopeCloseable {

    fun getRegistry(context: DIContext<*>): ScopeRegistry

    fun closeContext(context: DIContext<*>)
}

interface ScopeRegistry : ScopeCloseable, Sequence<Pair<RegKey, Any?>> {

    fun <T : Any> getOrCreate(key: RegKey, sync: Boolean = false, factory: () -> T): T

    operator fun minusAssign(key: RegKey)
}

class NoScope : Scope {

    private val scopeRegistry = ScopeRegistryImpl()

    override fun getRegistry(context: DIContext<*>): ScopeRegistry {
        return scopeRegistry
    }

    override fun closeContext(context: DIContext<*>) {
    }

    override fun close() {
        scopeRegistry.close()
    }
}

class ScopeImpl : Scope {

    private val scopeRegistry = hashMapOf<DIContext<*>, ScopeRegistry>()

    override fun getRegistry(context: DIContext<*>): ScopeRegistry {
        return scopeRegistry[context] ?: run { this.scopeRegistry.getOrPut(context) { ScopeRegistryImpl() } }
    }

    override fun closeContext(context: DIContext<*>) {
        scopeRegistry.remove(context)?.close()
    }

    override fun close() {
        scopeRegistry.forEach { it.value.close() }
        scopeRegistry.clear()
    }
}

private class ScopeRegistryImpl : ScopeRegistry {

    private val scopes = hashMapOf<RegKey, Any?>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getOrCreate(key: RegKey, sync: Boolean, factory: () -> T): T {
        val value = scopes[key]
        return (value ?: run {
            scopes.getOrPut(key, factory)
        }) as T
    }

    override fun minusAssign(key: RegKey) {
        scopes -= key
    }

    override fun close() {
        scopes.clear()
    }

    override fun iterator(): Iterator<Pair<RegKey, Any?>> {
        return scopes.map { it.key to it.value }.iterator()
    }
}