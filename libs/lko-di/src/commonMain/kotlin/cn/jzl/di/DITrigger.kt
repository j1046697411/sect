package cn.jzl.di

interface DITrigger : Sequence<Lazy<*>> {

    fun bindLazy(lazy: Lazy<*>)

    fun trigger()

    companion object {
        operator fun invoke(): DITrigger = DITriggerImpl()
    }
}