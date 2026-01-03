package cn.jzl.core.list

/**
 * `Short` 专用的快速可变列表接口。
 * - 增加 `ShortArray` 的批量插入支持，适合数值型批量操作。
 */
interface ShortMutableFastList : PrimitiveMutableFastList<Short> {
    fun insertLastAll(elements: ShortArray)
    fun insertAll(index: Int, elements: ShortArray)
}