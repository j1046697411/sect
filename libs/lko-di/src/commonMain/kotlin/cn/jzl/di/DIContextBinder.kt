package cn.jzl.di

import org.kodein.type.TypeToken

interface DIContextBinder<C : Any> {
    val contextType: TypeToken<C>
    val scope: Scope
}