package cn.jzl.core.list

/**
 * `Long` 专用的快速可变列表接口。
 * - 增加 `LongArray` 的批量插入支持，适合大范围整型数据处理。
 */
interface LongMutableFastList : PrimitiveMutableFastList<Long> {
    fun insertLastAll(elements: LongArray)
    fun insertAll(index: Int, elements: LongArray)
}