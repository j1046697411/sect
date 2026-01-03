package cn.jzl.core.list

import kotlin.math.max

/**
 * `Double` 的 FastList 实现。
 * - 使用 `DoubleArray` 顺序存储，扩容为两倍或刚好满足需要。
 * - 支持批量插入、`ensureCapacity`/`fill`、以及与 `DoubleArray` 的高效互操作。
 */
class DoubleFastList(capacity: Int = 7, val order: Boolean = true) : AbstractMutableFastList<Double>(),
    DoubleMutableFastList {
    private var data = DoubleArray(capacity)

    override var size: Int = 0
        private set

    private fun ensure(count: Int) {
        if (count + size > data.size) {
            data = data.copyOf(max(count + size, data.size * 2))
        }
    }

    override fun ensureCapacity(capacity: Int, element: Double) {
        if (size >= capacity) return
        ensure(capacity - size)
        data.fill(element, size, capacity)
        size = capacity
    }

    override fun fill(element: Double, startIndex: Int, endIndex: Int) {
        checkIndex(startIndex)
        check(startIndex < endIndex && endIndex <= size) { "startIndex $startIndex, endIndex $endIndex, size $size" }
        data.fill(element, startIndex, endIndex)
    }

    override fun safeInsert(index: Int, count: Int, callback: ListEditor<Double>.() -> Unit) {
        ensure(count)
        data.copyInto(data, index + count, index, size)
        var offset = index
        ListEditor<Double> { data[offset++] = it }.apply(callback)
        check(offset == index + count) { "offset $offset != index $index + count $count" }
        size += count
    }

    override fun safeInsertLast(count: Int, callback: ListEditor<Double>.() -> Unit) {
        ensure(count)
        val offset = size
        ListEditor<Double> { data[size++] = it }.apply(callback)
        check(offset + count == size) { "offset $offset + count $count != size $size" }
    }

    override fun set(index: Int, element: Double): Double {
        checkIndex(index)
        val old = data[index]
        data[index] = element
        return old
    }

    /**
     * 移除指定索引处的元素并返回该元素。
     * 当移除最后一个元素时，仅减少大小而不进行数组复制以提高性能。
     *
     * @param index 要移除的元素的索引
     * @return 被移除的元素
     * @throws IndexOutOfBoundsException 如果索引超出范围 [0, size)
     */
    override fun removeAt(index: Int): Double {
        checkIndex(index)
        val old = data[index]
        if (order) {
            // 优化：当移除最后一个元素时，不需要复制数组
            if (index != size - 1) {
                data.copyInto(data, index, index + 1, size)
            }
            data[size - 1] = 0.0
        } else {
            data[index] = data[size - 1]
            data[size - 1] = 0.0
        }
        size--
        return old
    }

    override fun add(index: Int, element: Double) {
        ensure(1)
        data.copyInto(data, index + 1, index, size)
        data[index] = element
        size++
    }

    override fun insertLast(element: Double) {
        ensure(1)
        data[size++] = element
    }

    override fun insertLast(element1: Double, element2: Double) {
        ensure(2)
        data[size++] = element1
        data[size++] = element2
    }

    override fun insertLast(element1: Double, element2: Double, element3: Double) {
        ensure(3)
        data[size++] = element1
        data[size++] = element2
        data[size++] = element3
    }

    override fun insertLast(element1: Double, element2: Double, element3: Double, element4: Double) {
        ensure(4)
        data[size++] = element1
        data[size++] = element2
        data[size++] = element3
        data[size++] = element4
    }

    override fun insertLast(element1: Double, element2: Double, element3: Double, element4: Double, element5: Double) {
        ensure(5)
        data[size++] = element1
        data[size++] = element2
        data[size++] = element3
        data[size++] = element4
        data[size++] = element5
    }

    override fun insertLast(element1: Double, element2: Double, element3: Double, element4: Double, element5: Double, element6: Double) {
        ensure(6)
        data[size++] = element1
        data[size++] = element2
        data[size++] = element3
        data[size++] = element4
        data[size++] = element5
        data[size++] = element6
    }

    override fun insertLastAll(elements: Iterable<Double>) {
        if (elements is Collection<*> && elements.isEmpty()) return
        when (elements) {
            is DoubleFastList -> {
                ensure(elements.size)
                elements.data.copyInto(data, size, 0, elements.size)
                size += elements.size
            }

            is Collection<Double> -> {
                ensure(elements.size)
                elements.forEachIndexed { i, e -> data[size + i] = e }
                size += elements.size
            }

            else -> {
                val doubles = elements.toList()
                if (doubles.isNotEmpty()) {
                    ensure(doubles.size)
                    doubles.forEachIndexed { i, e -> data[size + i] = e }
                    size += doubles.size
                }
            }
        }
    }

    override fun insertLastAll(elements: DoubleArray) {
        if (elements.isEmpty()) return
        ensure(elements.size)
        elements.copyInto(data, size, 0, elements.size)
        size += elements.size
    }

    override fun insert(index: Int, element: Double) {
        ensure(1)
        data.copyInto(data, index + 1, index, size)
        data[index] = element
        size++
    }

    override fun insert(index: Int, element1: Double, element2: Double) {
        ensure(2)
        data.copyInto(data, index + 2, index, size)
        data[index] = element1
        data[index + 1] = element2
        size += 2
    }

    override fun insert(index: Int, element1: Double, element2: Double, element3: Double) {
        ensure(3)
        data.copyInto(data, index + 3, index, size)
        data[index] = element1
        data[index + 1] = element2
        data[index + 2] = element3
        size += 3
    }

    override fun insert(index: Int, element1: Double, element2: Double, element3: Double, element4: Double) {
        ensure(4)
        data.copyInto(data, index + 4, index, size)
        data[index] = element1
        data[index + 1] = element2
        data[index + 2] = element3
        data[index + 3] = element4
        size += 4
    }

    override fun insert(index: Int, element1: Double, element2: Double, element3: Double, element4: Double, element5: Double) {
        ensure(5)
        data.copyInto(data, index + 5, index, size)
        data[index] = element1
        data[index + 1] = element2
        data[index + 2] = element3
        data[index + 3] = element4
        data[index + 4] = element5
        size += 5
    }

    override fun insert(index: Int, element1: Double, element2: Double, element3: Double, element4: Double, element5: Double, element6: Double) {
        ensure(6)
        data.copyInto(data, index + 6, index, size)
        data[index] = element1
        data[index + 1] = element2
        data[index + 2] = element3
        data[index + 3] = element4
        data[index + 4] = element5
        data[index + 5] = element6
        size += 6
    }

    override fun insertAll(index: Int, elements: Iterable<Double>) {
        if (elements is Collection<*> && elements.isEmpty()) return
        when (elements) {
            is DoubleFastList -> {
                ensure(elements.size)
                data.copyInto(data, index + elements.size, index, size)
                elements.data.copyInto(data, index, 0, elements.size)
                size += elements.size
            }

            is Collection<Double> -> {
                ensure(elements.size)
                data.copyInto(data, index + elements.size, index, size)
                elements.forEachIndexed { i, e -> data[index + i] = e }
                size += elements.size
            }

            else -> {
                val doubles = elements.toList()
                if (doubles.isNotEmpty()) {
                    ensure(doubles.size)
                    data.copyInto(data, index + doubles.size, index, size)
                    doubles.forEachIndexed { i, e -> data[index + i] = e }
                    size += doubles.size
                }
            }
        }
    }

    override fun insertAll(index: Int, elements: DoubleArray) {
        if (elements.isEmpty()) return
        ensure(elements.size)
        data.copyInto(data, index + elements.size, index, size)
        elements.copyInto(data, index, 0, elements.size)
        size += elements.size
    }

    override fun get(index: Int): Double {
        checkIndex(index)
        return data[index]
    }
}