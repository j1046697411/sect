package cn.jzl.ecs.component

import cn.jzl.core.list.IntFastList
import kotlin.jvm.JvmInline

@JvmInline
value class IntComponentStore(
    private val components: IntFastList = IntFastList()
) : ComponentStore<Int> {
    override val size: Int get() = components.size

    override fun get(index: Int): Int = components[index]

    override fun set(index: Int, value: Int) {
        check(index >= 0) { "Index must be non-negative" }
        if (index >= components.size) {
            components.ensureCapacity(index + 1, 0)
        }
        components[index] = value
    }

    override fun add(value: Int) = components.insertLast(value)

    override fun removeAt(index: Int): Int = components.removeAt(index)
}
