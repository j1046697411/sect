package cn.jzl.core.list

/**
 * `Float` 专用的快速可变列表接口。
 * - 增加 `FloatArray` 的批量插入支持，适合数值型流式写入。
 */
interface FloatMutableFastList : PrimitiveMutableFastList<Float> {
    fun insertLastAll(elements: FloatArray)
    fun insertAll(index: Int, elements: FloatArray)
}