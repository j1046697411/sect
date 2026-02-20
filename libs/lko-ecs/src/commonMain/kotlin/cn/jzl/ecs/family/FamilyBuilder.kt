package cn.jzl.ecs.family

import cn.jzl.core.list.LongFastList
import cn.jzl.ecs.WorldOwner

/**
 * 家族构建器接口，用于配置 Family 的匹配条件
 *
 * FamilyBuilder 提供 DSL 风格的 API 来定义哪些实体属于一个 Family。
 * 通过组合 component、exclude、or 等方法，可以构建复杂的过滤条件。
 *
 * ## 使用示例
 * ```kotlin
 * val family = world.familyService.family {
 *     component<Position>()           // 必须包含 Position 组件
 *     component<Velocity>()           // 必须包含 Velocity 组件
 *     exclude<DeadTag>()              // 不能包含 DeadTag
 * }
 * ```
 *
 * ## 高级用法
 * ```kotlin
 * val family = world.familyService.family {
 *     // 使用 or 组合多个条件
 *     or {
 *         component<PlayerTag>()
 *         component<EnemyTag>()
 *     }
 *
 *     // 必须包含 Health
 *     component<Health>()
 * }
 * ```
 *
 * @property keys 家族键列表，用于唯一标识家族
 * @see FamilyService.family 创建 Family 的入口
 */
interface FamilyBuilder : WorldOwner {
    /**
     * 家族键列表
     *
     * 用于生成家族的唯一标识
     */
    val keys: LongFastList

    /**
     * 设置家族匹配器
     *
     * @param familyMatcher 家族匹配器实例
     */
    fun matcher(familyMatcher: FamilyMatcher)
}
