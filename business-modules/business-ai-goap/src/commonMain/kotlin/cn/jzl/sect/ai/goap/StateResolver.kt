/**
 * GOAP 状态解析器接口
 *
 * 用于从 ECS 世界中获取特定状态键的值。
 * 状态解析器将 ECS 组件数据映射到 GOAP 状态系统。
 *
 * 使用示例：
 * ```kotlin
 * object HealthResolver : StateResolver<Health, Int> {
 *     override fun EntityRelationContext.getWorldState(agent: Entity, key: Health): Int {
 *         return agent.getComponent<HealthComponent>().currentHealth
 *     }
 * }
 * ```
 *
 * @param K 状态键类型
 * @param T 状态值类型
 */
package cn.jzl.sect.ai.goap

import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext

/**
 * 状态解析器接口
 *
 * 用于从世界中获取特定键的状态值
 *
 * @param K 状态键类型
 * @param T 状态值类型
 */
interface StateResolver<K : StateKey<T>, T> {
    /**
     * 从世界中获取特定智能体的状态值
     *
     * 在 [EntityRelationContext] 上下文中调用，
     * 可以访问 ECS 世界和实体关系
     *
     * @param agent 智能体实体
     * @param key 状态键
     * @return 状态值
     */
    fun EntityRelationContext.getWorldState(agent: Entity, key: K): T
}
