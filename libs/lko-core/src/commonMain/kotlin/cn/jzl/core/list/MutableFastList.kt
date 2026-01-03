package cn.jzl.core.list

internal fun Collection<*>.checkIndex(index: Int) {
    if (index !in 0..<size) {
        throw IndexOutOfBoundsException("Index $index is out of bounds for size $size")
    }
}

fun interface ListEditor<T> {
    fun unsafeInsert(element: T)
}

/**
 * 快速可变列表接口。
 * - 拓展 `MutableList<T>`，增加批量插入与容量管理相关方法。
 * - `insertLast`/`insert` 系列支持一次性插入多个元素，减少扩容和移动开销。
 * - `safeInsertLast`/`safeInsert` 通过回调批量写入，保证写入数量与预期一致。
 */
interface MutableFastList<T> : MutableList<T> {
    override fun add(element: T): Boolean
    override fun add(index: Int, element: T)

    fun insertLast(element: T)
    fun insertLast(element1: T, element2: T)
    fun insertLast(element1: T, element2: T, element3: T)
    fun insertLast(element1: T, element2: T, element3: T, element4: T)
    fun insertLast(element1: T, element2: T, element3: T, element4: T, element5: T)
    fun insertLast(element1: T, element2: T, element3: T, element4: T, element5: T, element6: T)
    fun insertLastAll(elements: Iterable<T>)

    fun insert(index: Int, element: T)
    fun insert(index: Int, element1: T, element2: T)
    fun insert(index: Int, element1: T, element2: T, element3: T)
    fun insert(index: Int, element1: T, element2: T, element3: T, element4: T)
    fun insert(index: Int, element1: T, element2: T, element3: T, element4: T, element5: T)
    fun insert(index: Int, element1: T, element2: T, element3: T, element4: T, element5: T, element6: T)
    fun insertAll(index: Int, elements: Iterable<T>)

    fun safeInsertLast(count: Int, callback: ListEditor<T>.() -> Unit)
    fun safeInsert(index: Int, count: Int, callback: ListEditor<T>.() -> Unit)
}

/**
 * 基于“单值”元素的容量与填充扩展接口。
 * - 提供 `ensureCapacity(capacity, element)`：将列表扩展到给定容量，并用元素初始化新增区间。
 * - 提供 `fill(element, startIndex, endIndex)`：在已有区间内批量赋值。
 */
interface PrimitiveMutableFastList<T> : MutableFastList<T> {
    fun ensureCapacity(capacity: Int, element: T)
    fun fill(element: T, startIndex: Int = 0, endIndex: Int = size)
}

/**
 * 基于“复合值”元素的容量与填充扩展接口。
 * - 与 `PrimitiveMutableFastList` 类似，但以 `V` 为填充值，对 `T` 列表进行初始化或批量赋值。
 * - 常用于复合结构（如向量/结构体）作为元素时的列表实现。
 */
interface CompositeMutableFastList<V, T> : MutableFastList<T> {
    fun ensureCapacity(capacity: Int, element: V)
    fun fill(element: V, startIndex: Int = 0, endIndex: Int = size)
}
