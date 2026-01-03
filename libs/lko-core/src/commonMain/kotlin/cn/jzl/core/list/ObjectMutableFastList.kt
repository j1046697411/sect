package cn.jzl.core.list

/**
 * 面向对象元素的扩展接口。
 * - 在 `PrimitiveMutableFastList<T>` 能力之上，增加 `Array<out T>` 的批量插入支持。
 * - 便于与已有数组数据进行高效拼接或插入。
 */
interface ObjectMutableFastList<T> : PrimitiveMutableFastList<T> {
    fun insertLastAll(elements: Array<out T>)
    fun insertAll(index: Int, elements: Array<out T>)
}