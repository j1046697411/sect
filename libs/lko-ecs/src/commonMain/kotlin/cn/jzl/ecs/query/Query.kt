package cn.jzl.ecs.query

import cn.jzl.ecs.World
import cn.jzl.ecs.archetype.Archetype
import cn.jzl.ecs.family.Family

/**
 * 实体查询类，用于遍历符合条件的实体
 *
 * Query 是 ECS 中查询实体的核心类，通过 [EntityQueryContext] 定义查询条件，
 * 并支持链式操作（filter、map、take 等）对结果进行处理。
 *
 * ## 使用示例
 * ```kotlin
 * // 定义查询上下文
 * class HealthContext(world: World) : EntityQueryContext(world) {
 *     val health: Health by component()
 * }
 *
 * // 执行查询
 * world.query { HealthContext(this) }
 *     .filter { it.health.current > 0 }
 *     .forEach { ctx ->
 *         println("Entity ${ctx.entity.id} has ${ctx.health.current} HP")
 *     }
 * ```
 *
 * ## 性能注意
 * - Query 是惰性求值的，只有在调用 [forEach]、[toList] 等终端操作时才会执行
 * - 使用 [Family] 进行预过滤可以显著提高查询性能
 *
 * @param C 查询上下文类型，必须是 [EntityQueryContext] 的子类
 * @property context 查询上下文实例，包含查询条件和组件访问器
 */
class Query<C : EntityQueryContext>(@PublishedApi internal val context: C) : QueryStream<C>, QueryStreamScope {

    /**
     * 获取查询关联的世界
     */
    override val world: World get() = context.world

    /**
     * 查询的实体家族，用于预过滤实体
     */
    override val family: Family by lazy { context.build() }

    /**
     * 检查指定原型是否匹配此查询
     *
     * @param archetype 要检查的原型
     * @return 如果原型匹配查询条件返回 true
     */
    operator fun contains(archetype: Archetype): Boolean = family.familyMatcher.match(archetype)

    /**
     * 收集查询结果
     *
     * 遍历所有匹配的实体，并通过 [collector] 回调处理每个实体。
     * 这是 Query 的核心执行逻辑。
     *
     * @param collector 结果收集器回调
     */
    override fun collect(collector: QueryCollector<C>) = with(collector) {
        if (family.archetypes.isEmpty()) return
        runCatching {
            family.archetypes.forEach { archetype ->
                var entityIndex = 0
                context.updateCache(archetype)
                while (entityIndex < archetype.size) {
                    context.apply(entityIndex) {
                        val oldEntity = context.entity
                        emit(context)
                        if (oldEntity == context.entity) entityIndex++
                    }
                }
            }
        }.recoverCatching {
            if (it !is AbortQueryException) throw it
        }.getOrThrow()
    }

    /**
     * 关闭查询，释放资源
     */
    override fun close() {
    }
}
