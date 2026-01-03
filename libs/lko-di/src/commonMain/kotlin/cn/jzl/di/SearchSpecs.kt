package cn.jzl.di

import org.kodein.type.TypeToken

data class SearchSpecs(
    val contextType: TypeToken<*>? = null,
    val argType: TypeToken<*>? = null,
    val targetType: TypeToken<*>? = null,
    val tag: Any? = null
)