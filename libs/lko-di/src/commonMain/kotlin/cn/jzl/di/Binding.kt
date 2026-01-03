package cn.jzl.di

fun interface Binding<C : Any, A, T : Any> {
    fun getFactory(bindingDI: BindingDI<C, T>, key: DI.Key<C, A, T>): DIFactory<A, T>
}