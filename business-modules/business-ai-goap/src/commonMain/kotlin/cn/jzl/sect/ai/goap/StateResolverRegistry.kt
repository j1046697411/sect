/**
 * GOAP 状态解析器注册表接口
 *
 * 用于注册和获取状态解析器的接口。
 * 实现此接口可以向 GOAP 系统提供状态解析能力。
 *
 * 使用示例：
 * ```kotlin
 * class MyStateResolverRegistry : StateResolverRegistry {
 *     private val resolvers = mapOf<StateKey<*>, StateResolver<*, *>>(
 *         Health to HealthResolver,
 *         HasTarget to HasTargetResolver
 *     )
 *
 *     @Suppress("UNCHECKED_CAST")
 *     override fun <K : StateKey<T>, T> getStateHandler(key: K): StateResolver<K, T>? {
 *         return resolvers[key] as? StateResolver<K, T>
 *     }
 * }
 * ```
 */
package cn.jzl.sect.ai.goap

/**
 * 状态解析器注册表接口
 *
 * 管理状态键与解析器的映射关系
 */
interface StateResolverRegistry {
    /**
     * 获取特定状态键的解析器
     *
     * @param K 状态键类型
     * @param T 状态值类型
     * @param key 状态键
     * @return 状态解析器，如果不存在则返回 null
     */
    fun <K : StateKey<T>, T> getStateHandler(key: K): StateResolver<K, T>?
}
