package cn.jzl.di

import org.kodein.type.TypeToken

interface DIBinding<C : Any, A, T : Any> : Binding<C, A, T> {
    val contextType: TypeToken<C>
    val argType: TypeToken<A>
    val targetType: TypeToken<T>
    val tag: Any? get() = null
    val fromModule: String
    val copier: Copier<C, A, T>? get() = null
    val supportSubTypes: Boolean get() = false

    fun interface Copier<C : Any, A, T : Any> {
        fun copy(builder: DIContainer.Builder): DIBinding<C, A, T>
    }
}