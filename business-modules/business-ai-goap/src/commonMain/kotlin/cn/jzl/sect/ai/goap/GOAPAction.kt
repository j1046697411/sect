/**
 * GOAP 动作接口
 *
 * 定义面向目标动作规划中动作的核心契约。
 * 每个 GOAP 动作包含：
 * - 名称：用于调试和日志
 * - 前置条件：动作执行前必须满足的条件
 * - 效果：动作执行后对世界状态的修改
 * - 成本：用于 A* 搜索中的路径代价计算
 * - 任务：动作执行的实际逻辑
 *
 * @see Action 具体实现类
 */
package cn.jzl.sect.ai.goap

/**
 * GOAP 动作接口
 *
 * 定义动作的所有属性，包括规划所需的元数据和执行所需的任务
 */
interface GOAPAction {
    /**
     * 动作名称
     *
     * 用于调试、日志和可视化
     */
    val name: String

    /**
     * 前置条件序列
     *
     * 动作执行前必须满足的所有条件
     */
    val preconditions: Sequence<Precondition>

    /**
     * 效果序列
     *
     * 动作执行后对世界状态的所有修改
     */
    val effects: Sequence<ActionEffect>

    /**
     * 动作成本
     *
     * 用于 A* 搜索中的路径代价计算
     * 值越小，该动作越可能被选择
     */
    val cost: Double

    /**
     * 执行任务
     *
     * 动作实际执行的逻辑，使用协程支持异步操作
     */
    val task: ActionTask
}
