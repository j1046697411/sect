package cn.jzl.di

internal class DIContainerBuilder(
    allowOverride: Boolean,
    silentOverride: Boolean,
    private val mutableBindingsMap: MutableBindingsMap = hashMapOf(),
    private val contextTranslatorRegistry: ContextTranslatorRegistry = arrayListOf(),
) : DIContainer.Builder {
    override val bindingsMap: Sequence<Map.Entry<DI.Key<*, *, *>, List<DIDefining<*, *, *>>>> get() = mutableBindingsMap.asSequence()
    override val translatorRegistry: Sequence<ContextTranslator<*, *>> get() = contextTranslatorRegistry.asSequence()

    private val overrideMode = OverrideMode(allowOverride, silentOverride)

    private sealed interface OverrideMode {

        val isAllowed: Boolean

        fun must(overrides: Boolean?): Boolean?

        data object Forbid : OverrideMode {

            override val isAllowed: Boolean = false

            override fun must(overrides: Boolean?): Boolean {
                if (overrides == true) {
                    throw OverridingException("Overriding has been forbidden")
                }
                return false
            }
        }

        data object AllowSilent : OverrideMode {

            override val isAllowed: Boolean = true

            override fun must(overrides: Boolean?): Boolean? = overrides
        }

        data object AllowExplicit : OverrideMode {

            override val isAllowed: Boolean = true

            override fun must(overrides: Boolean?): Boolean = overrides ?: true
        }

        companion object {
            operator fun invoke(allow: Boolean, silent: Boolean): OverrideMode {
                return when {
                    !allow -> Forbid
                    allow && silent -> AllowSilent
                    else -> AllowExplicit
                }
            }
        }
    }

    private fun checkOverrides(key: DI.Key<*, *, *>, overrides: Boolean?) {
        val newOverrides = overrideMode.must(overrides)
        if (newOverrides != null) {
            when {
                newOverrides && key !in mutableBindingsMap -> throw OverridingException("Binding $key must override an existing binding.")
                !newOverrides && key in mutableBindingsMap -> throw OverridingException("Binding $key must not override an existing binding.")
            }
        }
    }

    private fun checkMatch(allowOverride: Boolean) {
        if (!overrideMode.isAllowed && allowOverride)
            throw OverridingException("Overriding has been forbidden")
    }

    override fun <C : Any, A, T : Any> bind(binding: DIBinding<C, A, T>, fromModule: String?, overrides: Boolean?) {
        val key = binding.key
        checkOverrides(key, overrides)
        val bindings = getBindings(key)
        bindings.add(0, DIDefining(binding, fromModule))
    }

    private fun getBindings(key: DI.Key<*, *, *>): MutableList<DIDefining<*, *, *>> {
        return mutableBindingsMap.getOrPut(key) { arrayListOf() }
    }

    override fun extend(container: DIContainer, allowOverride: Boolean, copyKeys: List<DI.Key<*, *, *>>) {
        checkMatch(allowOverride)
        container.tree.bindings.forEach { (key, bindings) ->
            checkOverrides(key, allowOverride)
            val newBindings = if (key in copyKeys) {
                bindings.map { DIDefining(it.binding.copier?.copy(this) ?: it.binding, it.fromModule) }
            } else {
                bindings
            }
            val oldBindings = getBindings(key)
            oldBindings.addAll(0, newBindings.toList())
        }
    }

    override fun subBuilder(allowOverride: Boolean, silentOverride: Boolean): DIContainer.Builder {
        checkMatch(allowOverride)
        return DIContainerBuilder(allowOverride, silentOverride, mutableBindingsMap, contextTranslatorRegistry)
    }

    override fun registerContextTranslator(contextTranslator: ContextTranslator<*, *>) {
        this.contextTranslatorRegistry.add(contextTranslator)
    }

    private val <C : Any, A, T : Any> DIBinding<C, A, T>.key: DI.Key<C, A, T> get() = DI.Key(contextType, argType, targetType, tag)
}