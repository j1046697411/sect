package cn.jzl.core.list

/**
 * `Byte` 专用的快速可变列表接口。
 * - 增加 `ByteArray` 的批量插入支持，减少装箱开销。
 */
interface ByteMutableFastList : PrimitiveMutableFastList<Byte> {
    fun insertLastAll(elements: ByteArray)
    fun insertAll(index: Int, elements: ByteArray)
}