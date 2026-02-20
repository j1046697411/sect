package cn.jzl.ecs.query

import kotlin.coroutines.cancellation.CancellationException

/**
 * 查询中止异常
 *
 * AbortQueryException 用于提前终止查询流的执行。
 * 当调用 [abort] 函数时抛出此异常，查询流会立即停止遍历剩余实体。
 *
 * ## 使用场景
 * - [take] 操作取够指定数量后终止
 * - [takeWhile] 条件不满足时终止
 * - 自定义提前退出逻辑
 *
 * ## 注意
 * 此异常继承自 [CancellationException]，会被查询系统捕获并静默处理，
 * 不会传播到调用者。
 *
 * @param queryStream 触发中止的查询流作用域
 * @see abort
 */
@PublishedApi
internal data class AbortQueryException(val queryStream: QueryStreamScope) : CancellationException(null as String?)
