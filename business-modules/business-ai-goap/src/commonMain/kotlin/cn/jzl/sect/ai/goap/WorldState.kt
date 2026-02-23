/**
 * GOAP 世界状态接口
 *
 * 表示游戏世界的当前状态，继承自 [WorldStateReader]。
 * 提供状态键的枚举能力，用于状态哈希和比较。
 *
 * 在 GOAP 规划中，世界状态用于：
 * - 表示当前世界
 * - 作为 A* 搜索中节点的状态
 * - 比较不同状态的差异
 *
 * @see WorldStateReader 只读版本
 * @see WorldStateWriter 可写版本
 */
package cn.jzl.sect.ai.goap

/**
 * 世界状态接口
 *
 * 表示世界的当前状态，提供状态键枚举
 */
interface WorldState : WorldStateReader {
    /**
     * 当前状态中的所有键
     *
     * 用于状态比较和哈希计算
     */
    val stateKeys: Sequence<StateKey<*>>
}
