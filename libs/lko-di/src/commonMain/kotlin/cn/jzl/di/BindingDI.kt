package cn.jzl.di

interface BindingDI<C : Any, T : Any> : DirectDI<C> {

    val key: DI.Key<C, Any?, T>
    val node: Node<C, Any?, T>

    fun overriddenFactory(): DIFactory<Any?, T>?
}