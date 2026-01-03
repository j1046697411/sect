package cn.jzl.core.list

class SortSet<T> private constructor(
    private val data: MutableFastList<T>,
    private val comparator: Comparator<T>
) : MutableSet<T> {

    override val size: Int get() = data.size

    override fun isEmpty(): Boolean = data.isEmpty()

    @Suppress("SameReturnValue")
    override fun add(element: T): Boolean {
        if (data.isEmpty()) {
            data.add(element)
            return true
        }
        
        // 检查是否应该插入到末尾
        if (comparator.compare(data.last(), element) < 0) {
            data.add(element)
            return true
        }
        
        // 检查是否应该插入到开头
        if (comparator.compare(data.first(), element) > 0) {
            data.add(0, element)
            return true
        }
        
        var low = 0
        var high = size - 1
        var insertionIndex = -1
        
        while (low <= high) {
            val mid = (low + high).ushr(1) // safe from overflows
            val midVal = data[mid]
            val result = comparator.compare(midVal, element)

            when {
                result == 0 -> {
                    // 元素已存在，替换
                    data[mid] = element
                    return true
                }
                result < 0 -> {
                    // 检查是否可以插入到mid+1位置
                    if (mid + 1 < size) {
                        val nextResult = comparator.compare(data[mid + 1], element)
                        if (nextResult > 0) {
                            insertionIndex = mid + 1
                            break
                        } else if (nextResult == 0) {
                            // 元素已存在，替换
                            data[mid + 1] = element
                            return true
                        }
                    }
                    low = mid + 1
                }
                result > 0 -> {
                    // 检查是否可以插入到mid位置
                    if (mid > 0) {
                        val prevResult = comparator.compare(data[mid - 1], element)
                        if (prevResult < 0) {
                            insertionIndex = mid
                            break
                        } else if (prevResult == 0) {
                            // 元素已存在，替换
                            data[mid - 1] = element
                            return true
                        }
                    }
                    high = mid - 1
                }
            }
        }
        
        // 如果找到了插入位置，插入元素
        if (insertionIndex != -1) {
            data.add(insertionIndex, element)
            return true
        }
        
        // 如果循环结束还没有找到确切位置，使用low作为插入位置
        data.add(low, element)
        return true
    }

    override fun remove(element: T): Boolean {
        val index = data.binarySearch(element, comparator)
        if (index < 0) return false
        data.removeAt(index)
        return true
    }

    override fun addAll(elements: Collection<T>): Boolean {
        if (elements.isEmpty()) return false
        var modified = false
        elements.forEach { element ->
            if (add(element)) {
                modified = true
            }
        }
        return modified
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        if (elements.isEmpty()) return false
        var modified = false
        elements.forEach { element ->
            if (remove(element)) {
                modified = true
            }
        }
        return modified
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        if (elements.isEmpty()) {
            val wasEmpty = isEmpty()
            clear()
            return !wasEmpty
        }
        val iterator = data.iterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            if (element !in elements) {
                iterator.remove()
            }
        }
        return true
    }

    override fun clear() {
        data.clear()
    }

    override fun contains(element: T): Boolean = data.binarySearch(element, comparator) >= 0

    override fun containsAll(elements: Collection<T>): Boolean = elements.all { it in this }

    override fun iterator(): MutableIterator<T> = data.iterator()

    override fun toString(): String = data.toString()

    companion object {
        operator fun <T> invoke(comparator: Comparator<T>): SortSet<T> = SortSet(ObjectFastList(order = true), comparator)
        fun int(comparator: Comparator<Int>): SortSet<Int> = SortSet(IntFastList(order = true), comparator)
        fun long(comparator: Comparator<Long>): SortSet<Long> = SortSet(LongFastList(order = true), comparator)
        fun short(comparator: Comparator<Short>): SortSet<Short> = SortSet(ShortFastList(order = true), comparator)
        fun byte(comparator: Comparator<Byte>): SortSet<Byte> = SortSet(ByteFastList(order = true), comparator)
        fun char(comparator: Comparator<Char>): SortSet<Char> = SortSet(CharFastList(order = true), comparator)
        fun float(comparator: Comparator<Float>): SortSet<Float> = SortSet(FloatFastList(order = true), comparator)
        fun double(comparator: Comparator<Double>): SortSet<Double> = SortSet(DoubleFastList(order = true), comparator)
    }
}