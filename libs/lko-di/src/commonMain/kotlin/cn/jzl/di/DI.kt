package cn.jzl.di

import org.kodein.type.TypeToken

interface DI : DIAware {

    val container: DIContainer

    override val di: DI get() = this

    fun <C : Any> on(context: DIContext<C>): DirectDI<C>

    data class Key<C : Any, A, T : Any>(
        val contextType: TypeToken<C>,
        val argType: TypeToken<A>,
        val targetType: TypeToken<T>,
        val tag: Any?
    )

    companion object {

        operator fun invoke(
            name: String = "DIMain",
            context: DIContext<*> = DIContext,
            allowOverride: Boolean = false,
            silentOverride: Boolean = true,
            scope: Scope = NoScope(),
            configuration: DIMainBuilder.() -> Unit
        ): DI {
            val mainBuilder = DIMainBuilderImpl(name, context, allowOverride, silentOverride, scope)
            mainBuilder.configuration()
            return this(mainBuilder)
        }

        operator fun invoke(mainBuilder: DIMainBuilder): DI {
            val di = DIImpl(mainBuilder.context, DIContainer(mainBuilder.containerBuilder))
            mainBuilder.callbacks.forEach { di.it() }
            return di
        }
    }

    interface Builder<C : Any> : BindBuilder, DIContextBinder<C> {

        val name: String

        val containerBuilder: DIContainer.Builder

        fun <C : Any> context(contextType: TypeToken<C>): DIContextBinder<C>

        fun <MC : Any> module(module: DIModule<MC>)

        override fun <C : Any> DIContextBinder<C>.bind(factory: DIBindDefiningFactory<C, *, *>) {
            val binding = factory(this)
            containerBuilder.bind(binding.binding, name, binding.overrides)
        }

        fun registerContextTranslator(contextTranslator: ContextTranslator<*, *>)

        fun onReady(callback: DICallback)
    }
}

