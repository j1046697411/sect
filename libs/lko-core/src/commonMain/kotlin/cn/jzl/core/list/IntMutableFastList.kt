package cn.jzl.core.list

/**
 * `Int` 专用的快速可变列表接口。
 * - 在通用能力基础上，提供 `IntArray` 的批量插入方法，减少装箱和复制成本。
 */
interface IntMutableFastList : PrimitiveMutableFastList<Int> {
    fun insertLastAll(elements: IntArray)
    fun insertAll(index: Int, elements: IntArray)
}