package cn.jzl.di

internal data class DirectDIImpl<C : Any>(
    override val di: DI,
    override val context: DIContext<C>
) : DirectDI<C> {
    override fun <C1 : Any> on(context: DIContext<C1>): DirectDI<C1> = DirectDIImpl(di, context)
}