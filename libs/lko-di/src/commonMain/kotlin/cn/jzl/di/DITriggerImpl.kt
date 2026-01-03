package cn.jzl.di

internal class DITriggerImpl : DITrigger {

    private val lazy = arrayListOf<Lazy<*>>()

    override fun iterator(): Iterator<Lazy<*>> = lazy.iterator()

    override fun bindLazy(lazy: Lazy<*>) {
        this.lazy.add(lazy)
    }

    override fun trigger() {
        lazy.forEach { it.value }
    }
}