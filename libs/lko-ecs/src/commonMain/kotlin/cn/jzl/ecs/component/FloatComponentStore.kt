package cn.jzl.ecs.component

import cn.jzl.core.list.FloatFastList
import kotlin.jvm.JvmInline

@JvmInline
value class FloatComponentStore(
    private val components: FloatFastList = FloatFastList()
) : ComponentStore<Float> {
    override val size: Int get() = components.size

    override fun get(index: Int): Float = components[index]

    override fun set(index: Int, value: Float) {
        check(index >= 0) { "Index must be non-negative" }
        if (index >= components.size) {
            components.ensureCapacity(index + 1, 0f)
        }
        components[index] = value
    }

    override fun add(value: Float) = components.insertLast(value)

    override fun removeAt(index: Int): Float = components.removeAt(index)
}
