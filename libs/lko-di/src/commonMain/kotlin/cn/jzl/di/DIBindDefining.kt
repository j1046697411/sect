package cn.jzl.di

data class DIBindDefining<C : Any, A, T : Any>(
    val binding: DIBinding<C, A, T>,
    val overrides: Boolean? = null
)