/**
 * GOAP 状态键接口
 *
 * 用于标识世界状态中特定值的类型安全键。
 * 状态键是类型安全的，每个键关联一个特定类型的值。
 *
 * 使用示例：
 * ```kotlin
 * // 定义状态键
 * object Health : StateKey<Int>
 * object HasTarget : StateKey<Boolean>
 * object Location : StateKey<LocationType>
 *
 * // 使用状态键
 * val health = state.getValue(agent, Health)
 * state.setValue(HasTarget, true)
 * ```
 *
 * @param T 状态值类型
 */
package cn.jzl.sect.ai.goap

/**
 * 状态键接口
 *
 * 类型安全的状态键，用于标识世界状态中的特定值
 *
 * @param T 状态值类型
 */
interface StateKey<T>
