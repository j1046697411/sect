package cn.jzl.di

import org.kodein.type.TypeToken

interface DIContext<C : Any> {

    val type: TypeToken<C>
    val value: C

    companion object : DIContext<Any> {

        override val value: Any = Any()

        override val type: TypeToken<Any> = TypeToken.Any

        operator fun <C : Any> invoke(type: TypeToken<C>, value: C): DIContext<C> = Value(type, value)

        operator fun <C : Any> invoke(type: TypeToken<C>, provider: DIProvider<C>): DIContext<C> = Lazy(type, provider)
    }

    private data class Value<C : Any>(override val type: TypeToken<C>, override val value: C) : DIContext<C>

    private class Lazy<C : Any>(override val type: TypeToken<C>, provider: DIProvider<C>) : DIContext<C> {
        override val value: C by lazy(provider)
    }
}