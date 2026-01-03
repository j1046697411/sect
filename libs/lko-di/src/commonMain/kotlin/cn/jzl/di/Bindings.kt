package cn.jzl.di

import org.kodein.type.TypeToken
import org.kodein.type.generic

private data class ScopeKey<A>(val scopeId: Any, val arg: A)

data class Singleton<C : Any, A, T : Any>(
    private val scope: Scope,
    override val contextType: TypeToken<C>,
    override val argType: TypeToken<A>,
    override val targetType: TypeToken<T>,
    override val tag: Any?,
    override val fromModule: String,
    override val supportSubTypes: Boolean = false,
    private val scopeId: Any = Any(),
    private val sync: Boolean = true,
    private val factory: BindingDI<C, T>.(A) -> T
) : DIBinding<C, A, T> {

    private val scopeKey = ScopeKey(scopeId, Unit)

    override val copier: DIBinding.Copier<C, A, T> = DIBinding.Copier {
        Singleton(scope, contextType, argType, targetType, tag, fromModule, supportSubTypes, scopeId, sync, factory)
    }

    override fun getFactory(bindingDI: BindingDI<C, T>, key: DI.Key<C, A, T>): DIFactory<A, T> {
        val scopeRegistry = scope.getRegistry(DIContext)
        return { scopeRegistry.getOrCreate(scopeKey, sync) { bindingDI.factory(it) } }
    }
}

data class MultipleSingleton<C : Any, A, T : Any>(
    private val scope: Scope,
    override val contextType: TypeToken<C>,
    override val argType: TypeToken<A>,
    override val targetType: TypeToken<T>,
    override val tag: Any?,
    override val fromModule: String,
    override val supportSubTypes: Boolean = false,
    private val scopeId: Any = Any(),
    private val sync: Boolean = true,
    private val factory: BindingDI<C, T>.(A) -> T
) : DIBinding<C, A, T> {
    override val copier: DIBinding.Copier<C, A, T> = DIBinding.Copier {
        MultipleSingleton(scope, contextType, argType, targetType, tag, fromModule, supportSubTypes, scopeId, sync, factory)
    }

    override fun getFactory(bindingDI: BindingDI<C, T>, key: DI.Key<C, A, T>): DIFactory<A, T> {
        val scopeRegistry = scope.getRegistry(bindingDI.context)
        return { scopeRegistry.getOrCreate(ScopeKey(scopeId, it), sync) { bindingDI.factory(it) } }
    }
}

data class Prototype<C : Any, A, T : Any>(
    override val contextType: TypeToken<C>,
    override val argType: TypeToken<A>,
    override val targetType: TypeToken<T>,
    override val tag: Any?,
    override val fromModule: String,
    override val supportSubTypes: Boolean = false,
    private val factory: BindingDI<C, T>.(A) -> T
) : DIBinding<C, A, T> {

    override val copier: DIBinding.Copier<C, A, T> = DIBinding.Copier {
        Prototype(contextType, argType, targetType, tag, fromModule, supportSubTypes, factory)
    }

    override fun getFactory(bindingDI: BindingDI<C, T>, key: DI.Key<C, A, T>): DIFactory<A, T> {
        return { bindingDI.factory(it) }
    }
}


inline fun <reified C : Any, reified A : Any, reified T : Any> DI.Builder<*>.argPrototype(
    tag: Any? = null,
    overrides: Boolean? = null,
    supportSubTypes: Boolean = false,
    noinline factory: BindingDI<C, T>.(A) -> T
): DIBindDefiningFactory<C, A, T> = {
    DIBindDefining(
        Prototype(
            contextType = it.contextType,
            argType = generic<A>(),
            targetType = generic<T>(),
            tag = tag,
            fromModule = name,
            supportSubTypes = supportSubTypes,
            factory
        ),
        overrides
    )
}

inline fun <reified C : Any, reified T : Any> DI.Builder<*>.prototype(
    tag: Any? = null,
    overrides: Boolean? = null,
    supportSubTypes: Boolean = false,
    noinline factory: BindingDI<C, T>.() -> T
): DIBindDefiningFactory<C, Unit, T> = argPrototype(tag, overrides, supportSubTypes) { factory() }

inline fun <reified C : Any, reified A : Any, reified T : Any> DI.Builder<*>.argMultipleSingleton(
    tag: Any? = null,
    overrides: Boolean? = null,
    supportSubTypes: Boolean = false,
    scopeId: Any = Any(),
    sync: Boolean = true,
    noinline factory: BindingDI<C, T>.(A) -> T
): DIBindDefiningFactory<C, A, T> = {
    DIBindDefining(
        MultipleSingleton(
            scope = it.scope,
            contextType = it.contextType,
            argType = generic<A>(),
            targetType = generic<T>(),
            tag = tag,
            fromModule = name,
            supportSubTypes = supportSubTypes,
            scopeId = scopeId,
            sync = sync,
            factory = factory
        ),
        overrides
    )
}

inline fun <reified C : Any, reified T : Any> DI.Builder<*>.multipleSingleton(
    tag: Any? = null,
    overrides: Boolean? = null,
    supportSubTypes: Boolean = false,
    scopeId: Any = Any(),
    sync: Boolean = true,
    noinline factory: BindingDI<C, T>.() -> T
): DIBindDefiningFactory<C, Unit, T> = argMultipleSingleton(tag, overrides, supportSubTypes, scopeId, sync) { factory() }

inline fun <reified C : Any, reified A : Any, reified T : Any> DI.Builder<*>.argSingleton(
    tag: Any? = null,
    overrides: Boolean? = null,
    supportSubTypes: Boolean = false,
    scopeId: Any = Any(),
    sync: Boolean = true,
    noinline factory: BindingDI<C, T>.(A) -> T
): DIBindDefiningFactory<C, A, T> = {
    DIBindDefining(
        Singleton(
            it.scope,
            it.contextType,
            argType = generic<A>(),
            targetType = generic<T>(),
            tag = tag,
            name,
            supportSubTypes = supportSubTypes,
            scopeId = scopeId,
            sync = sync,
            factory = factory
        ), overrides
    )
}

inline fun <reified C : Any, reified T : Any> DI.Builder<*>.singleton(
    tag: Any? = null,
    overrides: Boolean? = null,
    supportSubTypes: Boolean = false,
    scopeId: Any = Any(),
    sync: Boolean = true,
    noinline factory: BindingDI<C, T>.() -> T
): DIBindDefiningFactory<C, Unit, T> = argSingleton(tag, overrides, supportSubTypes, scopeId, sync) { factory() }
