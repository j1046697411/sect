package cn.jzl.di

import org.kodein.type.TypeToken

@ConsistentCopyVisibility
data class DIModule<C : Any> internal constructor(
    val contextType: TypeToken<C>,
    val name: String,
    val allowOverride: Boolean = false,
    val silentOverride: Boolean = false,
    val configuration: DI.Builder<C>.() -> Unit
)