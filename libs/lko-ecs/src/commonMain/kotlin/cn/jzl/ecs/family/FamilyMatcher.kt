package cn.jzl.ecs.family

import cn.jzl.core.bits.BitSet
import cn.jzl.ecs.archetype.Archetype

/**
 * 家族匹配器接口
 *
 * FamilyMatcher 定义了判断原型是否属于某个家族的条件。
 * 实现此接口的类可以定义复杂的匹配规则，如包含特定组件、
 * 排除特定组件、满足特定关系等。
 *
 * ## 使用场景
 * - 定义 Family 的过滤条件
 * - 在 Archetype 层面预过滤实体
 * - 优化查询性能
 *
 * ## 实现示例
 * ```kotlin
 * class MyMatcher : FamilyMatcher {
 *     override fun match(archetype: Archetype): Boolean {
 *         return archetype.entityType.contains(someRelation)
 *     }
 *
 *     override fun FamilyMatchScope.getArchetypeBits(): BitSet {
 *         // 返回匹配的原型位图
 *     }
 * }
 * ```
 *
 * @see FamilyMatchScope 匹配作用域，提供位图操作能力
 */
interface FamilyMatcher {
    /**
     * 检查原型是否匹配此家族
     *
     * @param archetype 要检查的原型
     * @return 如果原型匹配返回 true
     */
    fun match(archetype: Archetype): Boolean

    /**
     * 获取匹配的原型位图
     *
     * 在 [FamilyMatchScope] 上下文中执行，返回所有匹配的原型位图
     *
     * @return 原型位图
     */
    fun FamilyMatchScope.getArchetypeBits(): BitSet
}
