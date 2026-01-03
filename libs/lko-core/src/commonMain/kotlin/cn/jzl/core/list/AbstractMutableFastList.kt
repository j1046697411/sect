package cn.jzl.core.list

import kotlin.IndexOutOfBoundsException

/**
 * FastList 抽象基类。
 * - 提供通用索引检查与与 `add`/`add(index, element)` 的默认实现（委托到 `insert`/`insertLast`）。
 * - 具体类型负责底层存储与容量管理。
 */
abstract class AbstractMutableFastList<T> : AbstractMutableList<T>(), MutableFastList<T> {

    protected fun checkIndex(index: Int) {
        if (index !in 0 until size) throw IndexOutOfBoundsException("index $index is out of bounds for size $size")
    }

    override fun add(element: T): Boolean {
        insertLast(element)
        return true
    }

    override fun add(index: Int, element: T): Unit = insert(index, element)
}