/**
 * GOAP 世界状态写入器接口
 *
 * 提供可写的世界状态访问能力。
 * 继承自 [WorldStateReader]，同时支持读写操作。
 *
 * 在以下场景使用：
 * - 应用动作效果
 * - 在规划过程中模拟状态变化
 *
 * @see WorldStateReader 只读版本
 * @see AgentState 智能体状态（包含更多功能）
 */
package cn.jzl.sect.ai.goap

/**
 * 世界状态写入器接口
 *
 * 扩展 [WorldStateReader]，提供状态写入能力
 */
interface WorldStateWriter : WorldStateReader {
    /**
     * 设置状态值
     *
     * @param K 状态键类型
     * @param T 状态值类型
     * @param key 状态键
     * @param value 状态值
     */
    fun <K : StateKey<T>, T> setValue(key: K, value: T)
}
