package cn.jzl.di

import org.kodein.type.TypeToken

interface ContextTranslator<C : Any, S : Any> {

    val contextType: TypeToken<C>

    val scopeType: TypeToken<S>

    fun translate(directDI: DirectDI<C>): DirectDI<S>?
}