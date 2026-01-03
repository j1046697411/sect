package cn.jzl.di

open class DIDefining<C : Any, A, T : Any>(val binding: DIBinding<C, A, T>, val fromModule: String? = null)