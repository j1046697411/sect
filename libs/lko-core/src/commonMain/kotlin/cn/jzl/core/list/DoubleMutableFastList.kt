package cn.jzl.core.list

/**
 * `Double` 专用的快速可变列表接口。
 * - 增加 `DoubleArray` 的批量插入支持，适合高精度数值场景。
 */
interface DoubleMutableFastList : PrimitiveMutableFastList<Double> {
    fun insertLastAll(elements: DoubleArray)
    fun insertAll(index: Int, elements: DoubleArray)
}