package cn.jzl.ecs.component

import cn.jzl.core.list.ObjectFastList
import kotlin.jvm.JvmInline

@JvmInline
value class GeneralComponentStore<C : Any>(
    private val components: ObjectFastList<C> = ObjectFastList(order = false)
) : ComponentStore<C> {
    override val size: Int get() = components.size

    override fun get(index: Int): C = components[index]

    override fun set(index: Int, value: C) {
        components.ensureCapacity(index + 1, value)
        components[index] = value
    }

    override fun add(value: C) = components.insertLast(value)

    override fun removeAt(index: Int): C = components.removeAt(index)
}