package cn.jzl.ecs.query

import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner

/**
 * 查询服务，管理和缓存查询实例
 *
 * QueryService 负责创建和管理 [Query] 实例，提供查询缓存机制以提高性能。
 * 相同的查询工厂函数会被缓存，避免重复创建查询上下文。
 *
 * ## 缓存机制
 * - 使用工厂函数作为缓存键
 * - 相同的查询上下文只创建一次
 * - 缓存的查询实例可以被重复使用
 *
 * ## 使用示例
 * ```kotlin
 * // 通常通过 World.query 扩展函数使用
 * val query = world.query { HealthContext(this) }
 *
 * // 多次调用相同的查询会返回缓存的实例
 * val sameQuery = world.query { HealthContext(this) } // 使用缓存
 * ```
 *
 * @param world 关联的 ECS 世界
 * @property queryCache 查询缓存映射表
 */
class QueryService(override val world: World) : WorldOwner {
    private val queryCache = mutableMapOf<Any, Query<*>>()

    /**
     * 创建或获取缓存的查询实例
     *
     * 使用工厂函数创建查询上下文，并缓存查询实例以提高性能。
     *
     * @param E 查询上下文类型
     * @param factory 查询上下文工厂函数
     * @return 查询实例
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : EntityQueryContext> query(factory: World.() -> E): Query<E> {
        return queryCache.getOrPut(factory) { Query(world.factory()) } as Query<E>
    }
}
