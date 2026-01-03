package cn.jzl.di

interface BindBuilder {
    infix fun <C : Any> DIContextBinder<C>.bind(factory: DIBindDefiningFactory<C, *, *>)

    infix fun <C : Any> DIContextBinder<C>.scope(scope: Scope): DIContextBinder<C>
}