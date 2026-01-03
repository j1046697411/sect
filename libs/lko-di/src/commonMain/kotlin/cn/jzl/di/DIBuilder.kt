package cn.jzl.di

import org.kodein.type.TypeToken

internal class DIBuilder<C : Any>(
    override val name: String,
    private val mainBuilder: DIMainBuilder,
    override val containerBuilder: DIContainer.Builder,
    override val contextType: TypeToken<C>,
    override val scope: Scope
) : DI.Builder<C> {

    override fun <C : Any> context(contextType: TypeToken<C>): DIContextBinder<C> {
        return mainBuilder.context(contextType)
    }

    override fun <C : Any> DIContextBinder<C>.scope(scope: Scope): DIContextBinder<C> = with(mainBuilder) {
        this@scope.scope(scope)
    }

    override fun <MC : Any> module(module: DIModule<MC>) {
        mainBuilder.module(module)
    }

    override fun registerContextTranslator(contextTranslator: ContextTranslator<*, *>) {
        mainBuilder.registerContextTranslator(contextTranslator)
    }

    override fun onReady(callback: DICallback) {
        mainBuilder.onReady(callback)
    }
}