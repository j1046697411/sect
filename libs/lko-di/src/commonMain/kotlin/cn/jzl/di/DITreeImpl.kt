package cn.jzl.di

import org.kodein.type.TypeToken

internal class DITreeImpl(
    bindingsMap: Sequence<Map.Entry<DI.Key<*, *, *>, List<DIDefining<*, *, *>>>>,
    registeredTranslators: Sequence<ContextTranslator<*, *>>,
) : DITree {

    private val boundTypeTree: BoundTypeTree = hashMapOf()
    private val cache = hashMapOf<DITreeKey<*, *, *, *>, DIDefinitions<*, *, *, *>>()

    override val bindings: BindingsMap
    override val registeredTranslators: List<ContextTranslator<*, *>> = registeredTranslators.toList()
    private val translators = registeredTranslators.toMutableList()

    private val translatorSequence = translators.asSequence()
    private val boundTypeTreeSequence = boundTypeTree.asSequence()

    init {
        val temTranslators = arrayListOf<ContextTranslator<*, *>>()
        while (true) {
            temTranslators.addAll(translators)
            for (form in temTranslators) {
                for (to in temTranslators) {
                    if (form === to) continue
                    if (to.contextType.isAssignableFrom(form.scopeType) && form.contextType != to.scopeType) {
                        if (translators.none { it.contextType == form.contextType && it.scopeType == to.scopeType }) {
                            @Suppress("UNCHECKED_CAST")
                            translators += CombinedContextTranslator(
                                form as ContextTranslator<Any, Any>,
                                to as ContextTranslator<Any, Any>
                            )
                        }
                    }
                }
            }
            if (temTranslators.size == translators.size) break
            temTranslators.clear()
        }
        this.bindings = bindingsMap.associate { (key, bindings) ->
            val definitions = bindings.map {
                when (it) {
                    is DIDefinition<*, *, *> -> it
                    else -> DIDefinition(this, it.binding, it.fromModule)
                }
            }
            translatorSequence.filter { key.contextType.isAssignableFrom(it.scopeType) }
                .map { key.treeKey(it.contextType, key.contextType) to it }
                .plusElement(key.treeKey(key.contextType, key.contextType) to null)
                .forEach { (treeKey, translator) ->
                    cache[treeKey] = Triple(key, definitions, translator)
                    val typeChecker =
                        if (definitions.first().binding.supportSubTypes) TypeChecker.Up(treeKey.targetType) else TypeChecker.Down(
                            treeKey.targetType
                        )
                    val contextTypeTree = boundTypeTree.getOrPut(typeChecker) { hashMapOf() }
                    val argumentTypeTree = contextTypeTree.getOrPut(TypeChecker.Up(treeKey.contextType)) { hashMapOf() }
                    val tagTree = argumentTypeTree.getOrPut(TypeChecker.Up(treeKey.argType)) { hashMapOf() }
                    val treeKeys = tagTree.getOrPut(treeKey.tag) { arrayListOf() }
                    if (treeKey !in treeKeys) {
                        treeKeys.add(treeKey)
                    }
                }
            key to definitions
        }
    }

    private fun <C : Any, A, T : Any, S : Any> DI.Key<*, A, T>.treeKey(
        contextType: TypeToken<C>,
        scopeType: TypeToken<S>
    ): DITreeKey<C, A, T, S> = DITreeKey(contextType, argType, targetType, scopeType, tag)

    override fun findBySearchSpecs(searchSpecs: SearchSpecs): Sequence<DIDefinitions<*, *, *, *>> {
        return findBySpecs(searchSpecs).mapNotNull(cache::get).distinctBy { it.first }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <C : Any, A, T : Any> find(
        key: DI.Key<C, A, T>,
        overrideLevel: Int
    ): Sequence<Triple<DI.Key<Any, A, T>, DIDefinition<Any, A, T>?, ContextTranslator<C, Any>?>> {
        return sequence {
            yieldAll(
                findBySpecs(SearchSpecs(key.contextType, key.argType, key.targetType, key.tag)).mapNotNull { treeKey ->
                    val (realKey, definitions, translator) = cache[treeKey] ?: return@mapNotNull null
                    Triple(
                        realKey as DI.Key<Any, A, T>,
                        definitions.getOrNull(overrideLevel) as? DIDefinition<Any, A, T>,
                        translator as? ContextTranslator<C, Any>
                    )
                }
            )
        }.distinctBy { it.first }
    }

    private fun findBySpecs(searchSpecs: SearchSpecs): Sequence<DITreeKey<*, *, *, *>> {
        return boundTypeTreeSequence.findByTargetType(searchSpecs.targetType)
            .findByContextType(searchSpecs.contextType)
            .findByArgType(searchSpecs.argType)
            .findByTag(searchSpecs.tag)
    }

    private fun Sequence<Map.Entry<TypeChecker, ContextTypeTree>>.findByTargetType(
        targetType: TypeToken<*>?
    ): Sequence<Map.Entry<TypeChecker, ArgumentTypeTree>> {
        val sequence = if (targetType != null) filter { it.key.check(targetType) } else this
        return sequence.flatMap { it.value.asSequence() }
    }

    private fun Sequence<Map.Entry<TypeChecker, ArgumentTypeTree>>.findByContextType(
        contextType: TypeToken<*>?
    ): Sequence<Map.Entry<TypeChecker, TagTree>> {
        val sequence = if (contextType != null) {
            filter { (typeChecker, _) -> typeChecker.check(contextType) }
        } else {
            this
        }
        return sequence.flatMap { it.value.asSequence() }
    }

    private fun Sequence<Map.Entry<TypeChecker, TagTree>>.findByArgType(
        argType: TypeToken<*>? = null
    ): Sequence<TagTree> {
        val sequence = if (argType != null) {
            filter { it.key.check(argType) }
        } else {
            this
        }
        return sequence.map { it.value }
    }

    private fun Sequence<TagTree>.findByTag(tag: Any? = null): Sequence<DITreeKey<*, *, *, *>> {
        return if (tag != TagAll) {
            sequence {
                for (tagTree in this@findByTag) {
                    tagTree[tag]?.let { yieldAll(it) }
                    tagTree[TagAll]?.let { yieldAll(it) }
                }
            }
        } else {
            flatMap { it.values.asSequence() }.flatMap { it }
        }
    }
}