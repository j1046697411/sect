/**
 * GOAP 世界状态读取器接口
 *
 * 提供只读的世界状态访问能力。
 * 是 GOAP 系统中最基础的状态访问接口。
 *
 * 在以下场景使用：
 * - 检查前置条件是否满足
 * - 判断目标是否达成
 * - 计算启发式值
 *
 * @see WorldState 可枚举状态键的版本
 * @see WorldStateWriter 可写版本
 */
package cn.jzl.sect.ai.goap

import cn.jzl.ecs.entity.Entity

/**
 * 世界状态读取器接口
 *
 * 提供类型安全的状态值读取
 */
interface WorldStateReader {
    /**
     * 获取特定智能体的状态值
     *
     * @param K 状态键类型
     * @param T 状态值类型
     * @param agent 智能体实体
     * @param key 状态键
     * @return 状态值
     */
    fun <K : StateKey<T>, T> getValue(agent: Entity, key: K): T
}
