package cn.jzl.ecs.component

import cn.jzl.core.list.LongFastList
import kotlin.jvm.JvmInline

@JvmInline
value class LongComponentStore(
    private val components: LongFastList = LongFastList()
) : ComponentStore<Long> {
    override val size: Int get() = components.size

    override fun get(index: Int): Long = components[index]

    override fun set(index: Int, value: Long) {
        check(index >= 0) { "Index must be non-negative" }
        if (index >= components.size) {
            components.ensureCapacity(index + 1, 0L)
        }
        components[index] = value
    }

    override fun add(value: Long) = components.insertLast(value)

    override fun removeAt(index: Int): Long = components.removeAt(index)
}
