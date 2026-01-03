package cn.jzl.di

import cn.jzl.di.DI.Key

typealias DIFactory<A, T> = (A) -> T
typealias DIProvider<T> = () -> T
typealias DICallback = DI.() -> Unit
typealias BindingsMap = Map<Key<*, *, *>, List<DIDefinition<*, *, *>>>
typealias DIDefinitions<C, A, T, S> = Triple<Key<C, A, T>, List<DIDefinition<C, A, T>>, ContextTranslator<C, S>?>
typealias DIBindDefiningFactory<C, A, T> = DIFactory<DIContextBinder<C>, DIBindDefining<C, A, T>>

internal typealias MutableBindingsMap = MutableMap<Key<*, *, *>, MutableList<DIDefining<*, *, *>>>
internal typealias ContextTranslatorRegistry = MutableList<ContextTranslator<*, *>>

internal typealias BoundTypeTree = MutableMap<TypeChecker, ContextTypeTree>
internal typealias ContextTypeTree = MutableMap<TypeChecker, ArgumentTypeTree>
internal typealias ArgumentTypeTree = MutableMap<TypeChecker, TagTree>
internal typealias TagTree = MutableMap<Any?, MutableList<DITreeKey<*, *, *, *>>>
