package cn.jzl.di

import org.kodein.type.TypeToken
import org.kodein.type.erasedOf
import org.kodein.type.generic
import kotlin.reflect.KProperty

object TagAll

fun <C : Any> DirectDIAware<*>.on(context: DIContext<C>): DirectDI<C> = directDI.on(context)
fun <C : Any> DIAware.on(context: DIContext<C>): DirectDI<C> = di.on(context)
fun <A, T : Any> DIFactory<A, T>.toProvider(argProvider: DIProvider<A>): DIProvider<T> = { this(argProvider()) }

@PublishedApi
internal val unitProvider: DIProvider<Unit> = {}

fun <C : Any> module(
    contextType: TypeToken<C>,
    name: String = "DIModule",
    allowOverride: Boolean = false,
    silentOverride: Boolean = false,
    configuration: DI.Builder<C>.() -> Unit
): DIModule<C> = DIModule(contextType, name, allowOverride, silentOverride, configuration)

fun interface LazyDelegate<V> {
    operator fun provideDelegate(receiver: Any?, prop: KProperty<Any?>): Lazy<V>
}

inline fun <reified T> DIAware.delegate(noinline provider: DirectDI<*>.(String) -> T): LazyDelegate<T> {
    return LazyDelegate { receiver, prop ->
        lazy {
            val context = if (receiver != null && context === DIContext) {
                @Suppress("UNCHECKED_CAST")
                DIContext(erasedOf(receiver) as TypeToken<Any>, receiver)
            } else {
                context
            }
            on(context).provider(prop.name)
        }.apply {
            trigger.bindLazy(this)
        }
    }
}

inline fun <reified A : Any, reified T : Any> DirectDIAware<*>.factory(
    tag: Any? = null
): DIFactory<A, T> = directDI[generic<A>(), generic<T>(), tag]

inline fun <reified A : Any, reified T : Any> DirectDIAware<*>.provider(
    tag: Any? = null,
    noinline provider: DIProvider<A>
): DIProvider<T> = factory<A, T>(tag).toProvider(provider)

inline fun <reified T : Any> DirectDIAware<*>.provider(
    tag: Any? = null
): DIProvider<T> = provider(tag, unitProvider)

inline fun <reified A : Any, reified T : Any> DirectDIAware<*>.factoryOrNull(
    tag: Any? = null
): DIFactory<A, T>? = directDI.factoryOrNull(generic<A>(), generic<T>(), tag)

inline fun <reified A : Any, reified T : Any> DirectDIAware<*>.providerOrNull(
    tag: Any? = null,
    noinline provider: DIProvider<A>
): DIProvider<T>? = factoryOrNull<A, T>(tag)?.toProvider(provider)

inline fun <reified T : Any> DirectDIAware<*>.providerOrNull(
    tag: Any? = null
): DIProvider<T>? = providerOrNull(tag, unitProvider)

inline fun <reified A : Any, reified T : Any> DirectDIAware<*>.allFactories(
    tag: Any? = TagAll
): Sequence<DIFactory<A, T>> = directDI.allFactories(generic<A>(), generic<T>(), tag)

inline fun <reified A : Any, reified T : Any> DirectDIAware<*>.allProviders(
    tag: Any? = TagAll,
    noinline provider: DIProvider<A>
): Sequence<DIProvider<T>> = directDI.allFactories(generic<A>(), generic<T>(), tag).map { it.toProvider(provider) }

inline fun <reified T : Any> DirectDIAware<*>.allProviders(
    tag: Any? = TagAll
): Sequence<DIProvider<T>> = allProviders(tag, unitProvider)

inline fun <reified A : Any, reified T : Any> DirectDIAware<*>.instance(
    tag: Any? = null,
    noinline provider: DIProvider<A>
): T = factory<A, T>(tag).toProvider(provider)()

inline fun <reified T : Any> DirectDIAware<*>.instance(tag: Any? = null): T = instance(tag, unitProvider)

inline fun <reified A : Any, reified T : Any> DirectDIAware<*>.instanceOrNull(
    tag: Any? = null,
    noinline argProvider: DIProvider<A>
): T? = directDI.providerOrNull<A, T>(tag, argProvider)?.invoke()

inline fun <reified T : Any> DirectDIAware<*>.instanceOrNull(
    tag: Any? = null,
): T? = instanceOrNull(tag, unitProvider)

inline fun <reified A : Any, reified T : Any> DirectDIAware<*>.allInstance(
    tag: Any? = TagAll,
    noinline argProvider: DIProvider<A>
): Sequence<T> = directDI.allProviders<A, T>(tag, argProvider).map { it() }

inline fun <reified T : Any> DirectDIAware<*>.allInstance(
    tag: Any? = TagAll,
): Sequence<T> = directDI.allInstance(tag, unitProvider)


inline fun <reified A : Any, reified T : Any> DIAware.factory(
    tag: Any? = null
): LazyDelegate<DIFactory<A, T>> = delegate { factory<A, T>(tag) }

inline fun <reified A : Any, reified T : Any> DIAware.provider(
    tag: Any? = null,
    noinline provider: DIProvider<A>
): LazyDelegate<DIProvider<T>> = delegate { factory<A, T>(tag).toProvider(provider) }

inline fun <reified T : Any> DIAware.provider(
    tag: Any? = null,
): LazyDelegate<DIProvider<T>> = delegate { provider<Unit, T>(tag, unitProvider) }

inline fun <reified A : Any, reified T : Any> DIAware.instance(
    tag: Any? = null,
    noinline argProvider: DIProvider<A>
): LazyDelegate<T> = delegate { provider<A, T>(tag, argProvider)() }

inline fun <reified T : Any> DIAware.instance(
    tag: Any? = null,
): LazyDelegate<T> = delegate { instance(tag, unitProvider) }

inline fun <reified A : Any, reified T : Any> DIAware.instanceOrNull(
    tag: Any? = null,
    noinline argProvider: DIProvider<A>
): LazyDelegate<T?> = delegate { providerOrNull<A, T>(tag, argProvider)?.invoke() }

inline fun <reified T : Any> DIAware.instanceOrNull(
    tag: Any? = null,
): LazyDelegate<T?> = delegate { instanceOrNull(tag, unitProvider) }

inline fun <reified T : Any> DIAware.allInstance(
    tag: Any? = TagAll
): LazyDelegate<Sequence<T>> = allInstance(tag, unitProvider)

inline fun <reified A : Any, reified T : Any> DIAware.allInstance(
    tag: Any? = TagAll,
    noinline argProvider: DIProvider<A>
): LazyDelegate<Sequence<T>> = delegate { allInstance(tag, argProvider) }

inline fun <reified T> DirectDIAware<*>.new(constructor: () -> T): T = constructor()

inline fun <reified A : Any, T> DirectDIAware<*>.new(constructor: (A) -> T): T = constructor(instance())

inline fun <reified A1 : Any, reified A2 : Any, T> DirectDIAware<*>.new(
    constructor: (A1, A2) -> T
): T = constructor(instance(), instance())

inline fun <reified A1 : Any, reified A2 : Any, reified A3 : Any, T> DirectDIAware<*>.new(
    constructor: (A1, A2, A3) -> T
): T = constructor(instance(), instance(), instance())

inline fun <reified A1 : Any, reified A2 : Any, reified A3 : Any, reified A4 : Any, T> DirectDIAware<*>.new(
    constructor: (A1, A2, A3, A4) -> T
): T = constructor(instance(), instance(), instance(), instance())

inline fun <reified A1 : Any, reified A2 : Any, reified A3 : Any, reified A4 : Any, reified A5 : Any, T> DirectDIAware<*>.new(
    constructor: (A1, A2, A3, A4, A5) -> T
): T = constructor(instance(), instance(), instance(), instance(), instance())

inline fun <reified A1 : Any, reified A2 : Any, reified A3 : Any, reified A4 : Any, reified A5 : Any, reified A6 : Any, T> DirectDIAware<*>.new(
    constructor: (A1, A2, A3, A4, A5, A6) -> T
): T = constructor(instance(), instance(), instance(), instance(), instance(), instance())

inline fun <reified A1 : Any, reified A2 : Any, reified A3 : Any, reified A4 : Any, reified A5 : Any, reified A6 : Any, reified A7 : Any, T> DirectDIAware<*>.new(
    constructor: (A1, A2, A3, A4, A5, A6, A7) -> T
): T = constructor(instance(), instance(), instance(), instance(), instance(), instance(), instance())

inline fun <reified A1 : Any, reified A2 : Any, reified A3 : Any, reified A4 : Any, reified A5 : Any, reified A6 : Any, reified A7, reified A8, T> DirectDIAware<*>.new(
    constructor: (A1, A2, A3, A4, A5, A6, A7, A8) -> T
): T = constructor(instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance())

inline fun <reified A1 : Any, reified A2 : Any, reified A3 : Any, reified A4 : Any, reified A5 : Any, reified A6 : Any, reified A7, reified A8, reified A9, T> DirectDIAware<*>.new(
    constructor: (A1, A2, A3, A4, A5, A6, A7, A8, A9) -> T
): T = constructor(instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance())