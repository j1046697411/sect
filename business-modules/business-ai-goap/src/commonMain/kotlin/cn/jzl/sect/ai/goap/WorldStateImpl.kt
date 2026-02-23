/**
 * GOAP 世界状态实现类
 *
 * 使用 Map 存储状态键值对的轻量级实现。
 * 使用 [JvmInline] 避免对象分配开销。
 *
 * 使用示例：
 * ```kotlin
 * val state = WorldStateImpl(mapOf(
 *     Health to 100,
 *     HasTarget to true,
 *     Location to LocationType.FOREST
 * ))
 *
 * val health = state.getValue(agent, Health)
 * ```
 */
package cn.jzl.sect.ai.goap

import cn.jzl.ecs.entity.Entity
import kotlin.jvm.JvmInline

/**
 * 世界状态实现类
 *
 * 基于 Map 的轻量级状态存储
 *
 * @param map 状态键值对映射
 */
@JvmInline
value class WorldStateImpl(private val map: Map<StateKey<*>, Any?>) : WorldState {
    override val stateKeys: Sequence<StateKey<*>> get() = map.keys.asSequence()

    @Suppress("UNCHECKED_CAST")
    override fun <K : StateKey<T>, T> getValue(agent: Entity, key: K): T {
        return map.getValue(key) as T
    }
}
