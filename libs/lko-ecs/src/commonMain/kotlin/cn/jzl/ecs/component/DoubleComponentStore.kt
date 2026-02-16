package cn.jzl.ecs.component

import cn.jzl.core.list.DoubleFastList
import kotlin.jvm.JvmInline

@JvmInline
value class DoubleComponentStore(
    private val components: DoubleFastList = DoubleFastList()
) : ComponentStore<Double> {
    override val size: Int get() = components.size

    override fun get(index: Int): Double = components[index]

    override fun set(index: Int, value: Double) {
        check(index >= 0) { "Index must be non-negative" }
        if (index >= components.size) {
            components.ensureCapacity(index + 1, 0.0)
        }
        components[index] = value
    }

    override fun add(value: Double) = components.insertLast(value)

    override fun removeAt(index: Int): Double = components.removeAt(index)
}
