package cn.jzl.di

import org.kodein.type.TypeToken
import kotlin.jvm.JvmInline

@JvmInline
internal value class DIContainerImpl(override val tree: DITree) : DIContainer {

    override fun <C : Any, A, T : Any> get(
        directDI: DirectDI<C>,
        key: DI.Key<C, A, T>,
        overrideLevel: Int
    ): DIFactory<A, T> = factoryOrNull(directDI, key, overrideLevel) ?: throw NullPointerException("$key is null")

    override fun <C : Any, A, T : Any> factoryOrNull(
        directDI: DirectDI<C>,
        key: DI.Key<C, A, T>,
        overrideLevel: Int
    ): DIFactory<A, T>? = tree.find(key, overrideLevel).map { (realKey, definition, translator) ->
        definition?.getFactory(directDI, key, realKey, translator, overrideLevel)
    }.firstOrNull()

    override fun <C : Any, A, T : Any> allFactories(
        directDI: DirectDI<C>,
        key: DI.Key<C, A, T>,
        overrideLevel: Int
    ): Sequence<DIFactory<A, T>> = tree.find(key, overrideLevel).mapNotNull { (realKey, definition, translator) ->
        definition?.getFactory(directDI, key, realKey, translator, overrideLevel)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <C : Any, A, T : Any> DIDefinition<Any, A, T>.getFactory(
        directDI: DirectDI<C>,
        key: DI.Key<C, A, T>,
        realKey: DI.Key<Any, A, T>,
        translator: ContextTranslator<C, Any>?,
        overrideLevel: Int
    ): DIFactory<A, T> {
        directDI.check(key, overrideLevel)
        val newDirectDI = translator?.toKDirectDI(directDI) ?: directDI
        newDirectDI as DirectDI<Any>
        val bindingDI = createBindingDI(
            newDirectDI,
            (directDI as? BindingDI<*, *>)?.node,
            key as DI.Key<Any, Any?, T>,
            realKey as DI.Key<Any, Any?, T>,
            overrideLevel
        )
        return binding.getFactory(bindingDI, realKey)
    }

    private fun <C : Any, T : Any> createBindingDI(
        directDI: DirectDI<C>,
        parentNode: Node<*, *, *>?,
        key: DI.Key<C, Any?, T>,
        realKey: DI.Key<C, Any?, T>,
        overrideLevel: Int
    ): BindingDI<C, T> = BindingDIImpl(directDI, realKey, Node(parentNode, key, overrideLevel))

    private fun <C : Any, S : Any> ContextTranslator<C, S>.toKDirectDI(
        directDI: DirectDI<C>
    ): DirectDI<S>? = translate(directDI)

    private fun DirectDI<*>.check(key: DI.Key<*, *, *>, overrideLevel: Int) {
        if (this !is BindingDI<*, *>) {
            return
        }
        var node: Node<*, *, *>? = this.node
        while (node != null) {
            if (node.key == key && node.overrideLevel == overrideLevel) {
                throw DependencyLoopException("$key loop dependency")
            }
            node = node.parentNode
        }
    }

    private class BindingDIImpl<C : Any, T : Any>(
        directDI: DirectDI<C>,
        override val key: DI.Key<C, Any?, T>,
        override val node: Node<C, Any?, T>
    ) : BindingDI<C, T>, DirectDI<C> by directDI {
        override fun overriddenFactory(): DIFactory<Any?, T>? {
            return directDI.di.container.factoryOrNull(this, node.key, node.overrideLevel + 1)
        }

        override fun <A, T : Any> get(argType: TypeToken<A>, targetType: TypeToken<T>, tag: Any?): DIFactory<A, T> {
            return di.container[this, DI.Key(context.type, argType, targetType, tag)]
        }

        override fun <A, T : Any> factoryOrNull(
            argType: TypeToken<A>,
            targetType: TypeToken<T>,
            tag: Any?
        ): DIFactory<A, T>? = di.container.factoryOrNull(this, DI.Key(context.type, argType, targetType, tag))

        override fun <A, T : Any> allFactories(
            argType: TypeToken<A>,
            targetType: TypeToken<T>,
            tag: Any?
        ): Sequence<DIFactory<A, T>> = di.container.allFactories(this, DI.Key(context.type, argType, targetType, tag))
    }
}