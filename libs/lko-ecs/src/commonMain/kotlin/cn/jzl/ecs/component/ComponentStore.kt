package cn.jzl.ecs.component

interface ComponentStore<C> {

    val size: Int

    operator fun get(index: Int): C

    operator fun set(index: Int, value: C)

    fun add(value: C)

    fun removeAt(index: Int) : C
}