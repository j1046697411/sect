package cn.jzl.core.list

/**
 * `Char` 专用的快速可变列表接口。
 * - 增加 `CharArray` 的批量插入支持，适合字符缓冲与文本处理。
 */
interface CharMutableFastList : PrimitiveMutableFastList<Char> {
    fun insertLastAll(elements: CharArray)
    fun insertAll(index: Int, elements: CharArray)
}