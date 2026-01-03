package cn.jzl.di

import org.kodein.type.TypeToken

internal data class DITreeKey<C : Any, A, T : Any, S : Any>(
    val contextType: TypeToken<C>,
    val argType: TypeToken<A>,
    val targetType: TypeToken<T>,
    val scopeType: TypeToken<S>,
    val tag: Any?
)