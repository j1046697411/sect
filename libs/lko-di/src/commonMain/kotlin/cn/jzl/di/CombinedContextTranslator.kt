package cn.jzl.di

import org.kodein.type.TypeToken

internal class CombinedContextTranslator<C : Any, T : Any, S : Any>(
    internal val form: ContextTranslator<C, T>,
    internal val to: ContextTranslator<T, S>
) : ContextTranslator<C, S> {
    override val contextType: TypeToken<C> get() = form.contextType
    override val scopeType: TypeToken<S> get() = to.scopeType
    override fun translate(directDI: DirectDI<C>): DirectDI<S>? = form.translate(directDI)?.let { to.translate(it) }
    override fun toString(): String = description
}

class SimpleContextTranslator<C : Any, S : Any>(
    override val contextType: TypeToken<C>,
    override val scopeType: TypeToken<S>,
    private val factory: DirectDI<C>.(TypeToken<S>)-> DIProvider<S>?
): ContextTranslator<C, S> {
    override fun translate(directDI: DirectDI<C>): DirectDI<S>? {
        return directDI.factory(scopeType)?.let { directDI.on(DIContext(scopeType, it)) }
    }

    override fun toString(): String = description
}

val ContextTranslator<*, *>.description: String
    get() = buildString {
        append(contextType.simpleDispString())
        val translators = arrayListOf(this@description)
        while (translators.isNotEmpty()) {
            val current = translators.removeLast()
            if (current is CombinedContextTranslator<*, *, *>) {
                translators.add(current.to)
                translators.add(current.form)
            } else {
                append(" => ").append(current.scopeType.simpleDispString())
            }
        }
    }