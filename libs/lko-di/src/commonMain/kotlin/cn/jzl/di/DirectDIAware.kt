package cn.jzl.di

interface DirectDIAware<C : Any> {
    val directDI: DirectDI<C>
}