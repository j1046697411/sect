package cn.jzl.di

internal class DIImpl(override val context: DIContext<*>, override val container: DIContainer) : DI {

    override val trigger: DITrigger = DITrigger()

    override fun <C : Any> on(context: DIContext<C>): DirectDI<C> = DirectDIImpl(this, context)
}