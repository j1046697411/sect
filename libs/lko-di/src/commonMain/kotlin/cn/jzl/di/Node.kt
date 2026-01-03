package cn.jzl.di

data class Node<C : Any, A, T : Any>(
    val parentNode: Node<*, *, *>? = null,
    val key: DI.Key<C, A, T>,
    val overrideLevel: Int
)