package cn.jzl.core.list

import kotlin.math.max

/**
 * `Short` 的 FastList 实现。
 * - 使用 `ShortArray` 顺序存储，扩容策略为两倍或刚好满足需要。
 * - 支持批量插入、`ensureCapacity`/`fill`、以及与 `ShortArray` 的高效互操作。
 */
class ShortFastList(capacity: Int = 7, val order: Boolean = true) : AbstractMutableFastList<Short>(),
    ShortMutableFastList {
    private var data = ShortArray(capacity)

    override var size: Int = 0
        private set

    private fun ensure(count: Int) {
        if (count + size > data.size) {
            data = data.copyOf(max(count + size, data.size * 2))
        }
    }

    override fun ensureCapacity(capacity: Int, element: Short) {
        if (size >= capacity) return
        ensure(capacity - size)
        data.fill(element, size, capacity)
        size = capacity
    }

    override fun fill(element: Short, startIndex: Int, endIndex: Int) {
        checkIndex(startIndex)
        check(startIndex < endIndex && endIndex <= size) { "startIndex $startIndex, endIndex $endIndex, size $size" }
        data.fill(element, startIndex, endIndex)
    }

    override fun safeInsert(index: Int, count: Int, callback: ListEditor<Short>.() -> Unit) {
        ensure(count)
        data.copyInto(data, index + count, index, size)
        var offset = index
        ListEditor<Short> { data[offset++] = it }.apply(callback)
        check(offset == index + count) { "offset $offset != index $index + count $count" }
        size += count
    }

    override fun safeInsertLast(count: Int, callback: ListEditor<Short>.() -> Unit) {
        ensure(count)
        val offset = size
        ListEditor<Short> { data[size++] = it }.apply(callback)
        check(offset + count == size) { "offset $offset + count $count != size $size" }
    }

    override fun set(index: Int, element: Short): Short {
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
    override fun removeAt(index: Int): Short {
        checkIndex(index)
        val old = data[index]
        if (order) {
            // 优化：当移除最后一个元素时，不需要复制数组
            if (index != size - 1) {
                data.copyInto(data, index, index + 1, size)
            }
            data[size - 1] = 0
        } else {
            data[index] = data[size - 1]
            data[size - 1] = 0
        }
        size--
        return old
    }

    override fun add(index: Int, element: Short) {
        ensure(1)
        data.copyInto(data, index + 1, index, size)
        data[index] = element
        size++
    }

    override fun insertLast(element: Short) {
        ensure(1)
        data[size++] = element
    }

    override fun insertLast(element1: Short, element2: Short) {
        ensure(2)
        data[size++] = element1
        data[size++] = element2
    }

    override fun insertLast(element1: Short, element2: Short, element3: Short) {
        ensure(3)
        data[size++] = element1
        data[size++] = element2
        data[size++] = element3
    }

    override fun insertLast(element1: Short, element2: Short, element3: Short, element4: Short) {
        ensure(4)
        data[size++] = element1
        data[size++] = element2
        data[size++] = element3
        data[size++] = element4
    }

    override fun insertLast(element1: Short, element2: Short, element3: Short, element4: Short, element5: Short) {
        ensure(5)
        data[size++] = element1
        data[size++] = element2
        data[size++] = element3
        data[size++] = element4
        data[size++] = element5
    }

    override fun insertLast(element1: Short, element2: Short, element3: Short, element4: Short, element5: Short, element6: Short) {
        ensure(6)
        data[size++] = element1
        data[size++] = element2
        data[size++] = element3
        data[size++] = element4
        data[size++] = element5
        data[size++] = element6
    }

    override fun insertLastAll(elements: Iterable<Short>) {
        if (elements is Collection<*> && elements.isEmpty()) return
        when (elements) {
            is ShortFastList -> {
                ensure(elements.size)
                elements.data.copyInto(data, size, 0, elements.size)
                size += elements.size
            }

            is Collection<Short> -> {
                ensure(elements.size)
                elements.forEachIndexed { i, e -> data[size + i] = e }
                size += elements.size
            }

            else -> {
                val shorts = elements.toList()
                if (shorts.isNotEmpty()) {
                    ensure(shorts.size)
                    shorts.forEachIndexed { i, e -> data[size + i] = e }
                    size += shorts.size
                }
            }
        }
    }

    override fun insertLastAll(elements: ShortArray) {
        if (elements.isEmpty()) return
        ensure(elements.size)
        elements.copyInto(data, size, 0, elements.size)
        size += elements.size
    }

    override fun insert(index: Int, element: Short) {
        ensure(1)
        data.copyInto(data, index + 1, index, size)
        data[index] = element
        size++
    }

    override fun insert(index: Int, element1: Short, element2: Short) {
        ensure(2)
        data.copyInto(data, index + 2, index, size)
        data[index] = element1
        data[index + 1] = element2
        size += 2
    }

    override fun insert(index: Int, element1: Short, element2: Short, element3: Short) {
        ensure(3)
        data.copyInto(data, index + 3, index, size)
        data[index] = element1
        data[index + 1] = element2
        data[index + 2] = element3
        size += 3
    }

    override fun insert(index: Int, element1: Short, element2: Short, element3: Short, element4: Short) {
        ensure(4)
        data.copyInto(data, index + 4, index, size)
        data[index] = element1
        data[index + 1] = element2
        data[index + 2] = element3
        data[index + 3] = element4
        size += 4
    }

    override fun insert(index: Int, element1: Short, element2: Short, element3: Short, element4: Short, element5: Short) {
        ensure(5)
        data.copyInto(data, index + 5, index, size)
        data[index] = element1
        data[index + 1] = element2
        data[index + 2] = element3
        data[index + 3] = element4
        data[index + 4] = element5
        size += 5
    }

    override fun insert(index: Int, element1: Short, element2: Short, element3: Short, element4: Short, element5: Short, element6: Short) {
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

    override fun insertAll(index: Int, elements: Iterable<Short>) {
        if (elements is Collection<*> && elements.isEmpty()) return
        when (elements) {
            is ShortFastList -> {
                ensure(elements.size)
                data.copyInto(data, index + elements.size, index, size)
                elements.data.copyInto(data, index, 0, elements.size)
                size += elements.size
            }

            is Collection<Short> -> {
                ensure(elements.size)
                data.copyInto(data, index + elements.size, index, size)
                elements.forEachIndexed { i, e -> data[index + i] = e }
                size += elements.size
            }

            else -> {
                val shorts = elements.toList()
                if (shorts.isNotEmpty()) {
                    ensure(shorts.size)
                    data.copyInto(data, index + shorts.size, index, size)
                    shorts.forEachIndexed { i, e -> data[index + i] = e }
                    size += shorts.size
                }
            }
        }
    }

    override fun insertAll(index: Int, elements: ShortArray) {
        if (elements.isEmpty()) return
        ensure(elements.size)
        data.copyInto(data, index + elements.size, index, size)
        elements.copyInto(data, index, 0, elements.size)
        size += elements.size
    }

    override fun get(index: Int): Short {
        checkIndex(index)
        return data[index]
    }
}