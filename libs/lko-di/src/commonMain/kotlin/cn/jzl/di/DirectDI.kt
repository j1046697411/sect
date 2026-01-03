package cn.jzl.di

import org.kodein.type.TypeToken

interface DirectDI<C : Any> : DirectDIAware<C> {

    val di: DI

    val context: DIContext<C>

    override val directDI: DirectDI<C> get() = this

    fun <C1 : Any> on(context: DIContext<C1>): DirectDI<C1>

    operator fun <A, T : Any> get(argType: TypeToken<A>, targetType: TypeToken<T>, tag: Any? = null): DIFactory<A, T> {
        return di.container[directDI, DI.Key(context.type, argType, targetType, tag)]
    }

    fun <A, T : Any> factoryOrNull(
        argType: TypeToken<A>,
        targetType: TypeToken<T>,
        tag: Any? = null
    ): DIFactory<A, T>? = di.container.factoryOrNull(directDI, DI.Key(context.type, argType, targetType, tag))

    fun <A, T : Any> allFactories(
        argType: TypeToken<A>,
        targetType: TypeToken<T>,
        tag: Any? = null
    ): Sequence<DIFactory<A, T>> = di.container.allFactories(directDI, DI.Key(context.type, argType, targetType, tag))
}