/**
 * 智能体状态模块
 *
 * 定义智能体在 GOAP 规划过程中的状态表示。
 * 智能体状态是世界状态的扩展，支持：
 * - 状态复制用于 A* 搜索
 * - 效果合并模拟动作执行
 * - 前置条件检查
 */
package cn.jzl.sect.ai.goap

/**
 * 智能体状态接口
 *
 * 表示智能体的当前状态，继承自世界状态。
 * 在 A* 搜索过程中，每个搜索节点都维护一个独立的智能体状态副本。
 */
interface AgentState : WorldState {
    /**
     * 创建状态副本
     *
     * 在 A* 搜索中，每个分支需要独立的状态副本以避免状态污染
     *
     * @return 状态副本
     */
    fun copy(): AgentState

    /**
     * 合并动作效果到当前状态
     *
     * 创建新状态并应用动作效果，用于模拟动作执行后的世界状态
     *
     * @param effects 动作效果序列
     * @return 合并后的新状态
     */
    fun mergeEffects(effects: Sequence<ActionEffect>): AgentState

    /**
     * 检查当前状态是否满足条件序列
     *
     * 用于验证动作的前置条件是否满足
     *
     * @param conditions 条件序列
     * @return 是否满足所有条件
     */
    fun satisfiesConditions(conditions: Sequence<Precondition>): Boolean
}
