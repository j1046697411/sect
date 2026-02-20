package cn.jzl.ecs.query

/**
 * 查询结果收集器函数式接口
 *
 * QueryCollector 定义了如何处理查询流中的每个元素。
 * 这是一个函数式接口 (SAM)，可以使用 Lambda 表达式实现。
 *
 * ## 使用示例
 * ```kotlin
 * query.collect { ctx ->
 *     println("Entity: ${ctx.entity.id}")
 * }
 * ```
 *
 * ## 与 forEach 的区别
 * - [collect] 是底层方法，需要手动调用 [QueryStreamScope.emit]
 * - [forEach] 是扩展函数，提供更简单的使用方式
 *
 * @param T 收集元素的类型
 * @see QueryStream.collect
 * @see forEach
 */
fun interface QueryCollector<T> {
    /**
     * 在查询流作用域中发射一个值
     *
     * 此方法在 [QueryStreamScope] 上下文中执行，可以访问流的相关信息。
     *
     * @param value 要发射的值
     */
    fun QueryStreamScope.emit(value: T)
}
