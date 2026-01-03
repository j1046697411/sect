package cn.jzl.di

import org.kodein.type.TypeToken
import org.kodein.type.generic

inline fun <reified C : Any, reified S : Any> contextTranslator(
    noinline factory: DirectDI<C>.(TypeToken<S>) -> DIProvider<S>?
): ContextTranslator<C, S> = SimpleContextTranslator(generic<C>(), generic<S>(), factory)

inline fun <reified S : Any> contextFinder(
    noinline factory: DirectDI<Any>.(TypeToken<S>) -> DIProvider<S>?
): ContextTranslator<Any, S> = contextTranslator(factory)

inline fun <reified C : Any, reified S : Any> DI.Builder<*>.registerContextTranslator(
    noinline factory: DirectDI<C>.(TypeToken<S>) -> DIProvider<S>?
) = registerContextTranslator(contextTranslator(factory))

inline fun <reified S : Any> DI.Builder<*>.registerContextFinder(
    noinline factory: DirectDI<Any>.(TypeToken<S>) -> DIProvider<S>?
) = registerContextTranslator(contextTranslator(factory))