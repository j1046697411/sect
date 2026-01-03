package cn.jzl.di

import org.kodein.type.TypeToken

internal class DIMainBuilderImpl(
    override val name: String = "main",
    override val context: DIContext<*> = DIContext,
    allowOverride: Boolean = true,
    silentOverride: Boolean = false,
    override val scope: Scope = NoScope()
) : DIMainBuilder {

    private val callbackRegistry = arrayListOf<DICallback>()

    override val callbacks: Sequence<DICallback> get() = callbackRegistry.asSequence()
    override val containerBuilder: DIContainer.Builder = DIContainerBuilder(allowOverride, silentOverride)

    override fun <C : Any> context(contextType: TypeToken<C>): DIContextBinder<C> {
        return DIContextBinderImpl(contextType, scope)
    }

    override fun <C : Any> DIContextBinder<C>.scope(scope: Scope): DIContextBinder<C> {
        return DIContextBinderImpl(contextType, scope)
    }

    override fun <MC : Any> module(module: DIModule<MC>) = with(module) {
        val subBuilder = containerBuilder.subBuilder(module.allowOverride, module.silentOverride)
        val diBuilder = DIBuilder(module.name, this@DIMainBuilderImpl, subBuilder, module.contextType, scope)
        diBuilder.configuration()
    }

    override fun registerContextTranslator(contextTranslator: ContextTranslator<*, *>) {
        containerBuilder.registerContextTranslator(contextTranslator)
    }

    override fun onReady(callback: DICallback) {
        this.callbackRegistry.add(callback)
    }

    private class DIContextBinderImpl<C : Any>(
        override val contextType: TypeToken<C>,
        override val scope: Scope
    ) : DIContextBinder<C>
}
