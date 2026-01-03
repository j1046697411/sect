package cn.jzl.di

interface DITree {

    val bindings: BindingsMap

    val registeredTranslators: List<ContextTranslator<*, *>>

    fun findBySearchSpecs(searchSpecs: SearchSpecs): Sequence<DIDefinitions<*, *, *, *>>

    fun <C : Any, A, T : Any> find(
        key: DI.Key<C, A, T>,
        overrideLevel: Int = 0
    ): Sequence<Triple<DI.Key<Any, A, T>, DIDefinition<Any, A, T>?, ContextTranslator<C, Any>?>>
}