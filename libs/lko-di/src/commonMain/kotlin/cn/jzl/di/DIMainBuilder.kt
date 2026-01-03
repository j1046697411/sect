package cn.jzl.di

import org.kodein.type.TypeToken


interface DIMainBuilder : DI.Builder<Any> {

    val context: DIContext<*>

    val callbacks: Sequence<DICallback>

    override val contextType: TypeToken<Any> get() = TypeToken.Any

    companion object {
        operator fun invoke(
            name: String = "DIMain",
            context: DIContext<*> = DIContext,
            allowOverride: Boolean = false,
            silentOverride: Boolean = true,
            scope: Scope = NoScope(),
        ): DIMainBuilder = DIMainBuilderImpl(
            name = name,
            context = context,
            allowOverride = allowOverride,
            silentOverride = silentOverride,
            scope = scope,
        )
    }
}

