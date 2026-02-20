package cn.jzl.ecs.family

import androidx.collection.mutableObjectListOf
import cn.jzl.core.list.ObjectFastList
import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.archetype.Archetype

/**
 * 实体家族，表示具有相同组件特征的一组实体
 *
 * Family 是 ECS 框架中用于实体分组和预过滤的机制。
 * 具有相同组件组合的实体属于同一个 Archetype，
 * 而 Family 可以包含多个匹配的 Archetype。
 *
 * ## 工作原理
 * 1. [FamilyMatcher] 定义匹配规则（包含/排除特定组件）
 * 2. 当实体组件变化时，自动从旧 Family 移动到新 Family
 * 3. 查询时通过 Family 预过滤，只遍历匹配的 Archetype
 *
 * ## 使用示例
 * ```kotlin
 * // 通常通过 FamilyBuilder 创建
 * val family = world.familyService.family {
 *     component<Position>()
 *     component<Velocity>()
 *     exclude<DeadTag>()
 * }
 * ```
 *
 * @param world 关联的 ECS 世界
 * @param familyMatcher 家族匹配器，定义哪些实体属于此家族
 * @property archetypes 属于此家族的所有原型列表
 * @property size 家族中实体的总数
 */
class Family(override val world: World, val familyMatcher: FamilyMatcher) : WorldOwner {
    @PublishedApi
    internal val archetypes = mutableObjectListOf<Archetype>()

    /**
     * 家族中实体的总数
     */
    val size: Int get() = archetypes.fold(0) { acc, archetype -> acc + archetype.size }

    /**
     * 添加原型到家族
     *
     * 当新创建的 Archetype 匹配此 Family 的条件时调用
     *
     * @param archetype 要添加的原型
     */
    internal fun addArchetype(archetype: Archetype): Unit {
        archetypes.add(archetype)
    }
}
