package cn.jzl.di

interface DIContainer {

    val tree: DITree

    operator fun <C : Any, A, T : Any> get(
        directDI: DirectDI<C>,
        key: DI.Key<C, A, T>,
        overrideLevel: Int = 0
    ): DIFactory<A, T>

    fun <C : Any, A, T : Any> factoryOrNull(
        directDI: DirectDI<C>,
        key: DI.Key<C, A, T>,
        overrideLevel: Int = 0
    ): DIFactory<A, T>?

    fun <C : Any, A, T : Any> allFactories(
        directDI: DirectDI<C>,
        key: DI.Key<C, A, T>,
        overrideLevel: Int = 0
    ): Sequence<DIFactory<A, T>>

    interface DIBinder {
        fun <C : Any, A, T : Any> bind(binding: DIBinding<C, A, T>, fromModule: String? = null, overrides: Boolean? = null)
    }

    interface Builder : DIBinder {

        val bindingsMap: Sequence<Map.Entry<DI.Key<*, *, *>, List<DIDefining<*, *, *>>>>

        val translatorRegistry: Sequence<ContextTranslator<*, *>>

        override fun <C : Any, A, T : Any> bind(binding: DIBinding<C, A, T>, fromModule: String?, overrides: Boolean?)

        fun extend(container: DIContainer, allowOverride: Boolean = false, copyKeys: List<DI.Key<*, *, *>> = emptyList())

        fun subBuilder(allowOverride: Boolean = false, silentOverride: Boolean = false): Builder

        fun registerContextTranslator(contextTranslator: ContextTranslator<*, *>)
    }

    companion object {
        operator fun invoke(builder: Builder): DIContainer {
            return DIContainerImpl(DITreeImpl(builder.bindingsMap, builder.translatorRegistry))
        }
    }
}