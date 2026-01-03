package cn.jzl.di

class DIDefinition<C : Any, A, T : Any>(val tree: DITree, binding: DIBinding<C, A, T>, fromModule: String? = null) :
    DIDefining<C, A, T>(binding, fromModule)