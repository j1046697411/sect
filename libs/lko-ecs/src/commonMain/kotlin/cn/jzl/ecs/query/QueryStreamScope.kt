package cn.jzl.ecs.query

import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.family.Family

/**
 * 查询流作用域接口
 *
 * QueryStreamScope 提供了在查询流操作中的上下文环境，
 * 允许访问当前世界和查询的 Family 信息。
 *
 * ## 使用场景
 * - 在 [QueryCollector.emit] 中访问世界服务
 * - 在流操作（filter、map 等）中获取查询信息
 * - 提前终止查询（通过 [abort] 函数）
 *
 * ## 使用示例
 * ```kotlin
 * query.collect { ctx ->
 *     // 可以通过 this.world 访问世界
 *     val time = world.query { TimeContext(this) }.firstOrNull()
 * }
 * ```
 *
 * @property family 当前查询的 Family，用于预过滤实体
 * @see WorldOwner
 */
interface QueryStreamScope : WorldOwner {
    /**
     * 当前查询的实体家族
     *
     * Family 定义了查询的过滤条件，用于在 Archetype 层面预筛选实体
     */
    val family: Family
}
