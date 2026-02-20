package cn.jzl.ecs.query

/**
 * 查询流接口，提供惰性求值的实体遍历机制
 *
 * QueryStream 是 ECS 查询系统的基础接口，定义了如何收集查询结果。
 * 实现此接口的类可以提供各种查询操作（如 filter、map、take 等）。
 *
 * ## 特性
 * - 惰性求值：只有在调用 [collect] 时才会执行查询
 * - 可关闭：实现 [AutoCloseable] 接口，支持资源释放
 * - 可链式操作：支持 filter、map、take 等流式操作
 *
 * ## 使用示例
 * ```kotlin
 * world.query { HealthContext(this) }
 *     .filter { it.health.current > 0 }
 *     .take(10)
 *     .collect { ctx ->
 *         println("Entity: ${ctx.entity.id}")
 *     }
 * ```
 *
 * @param T 查询流中元素的类型
 * @see QueryStreamExtensions 提供各种流式操作扩展函数
 */
interface QueryStream<T> : AutoCloseable {
    /**
     * 收集查询结果
     *
     * 遍历所有匹配的元素，并通过 [collector] 回调处理每个元素。
     * 这是 QueryStream 的核心执行逻辑。
     *
     * @param collector 结果收集器回调
     */
    fun collect(collector: QueryCollector<T>)
}
