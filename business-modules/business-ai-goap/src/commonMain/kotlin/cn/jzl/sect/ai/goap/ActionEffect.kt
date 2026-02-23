/**
 * 动作效果接口
 *
 * 定义动作执行后对世界状态的影响。
 * 是一个函数式接口，可以直接使用 lambda 表达式创建。
 *
 * 使用示例：
 * ```kotlin
 * // 增加资源数量
 * val addResource = ActionEffect { state, agent ->
 *     state.setValue(ResourceCount, state.getValue(agent, ResourceCount) + 10)
 * }
 *
 * // 设置状态标志
 * val setFlag = ActionEffect { state, _ ->
 *     state.setValue(HasTarget, true)
 * }
 * ```
 */
package cn.jzl.sect.ai.goap

import cn.jzl.ecs.entity.Entity

/**
 * 动作效果函数式接口
 *
 * 表示动作执行后对世界状态的修改操作
 */
fun interface ActionEffect {
    /**
     * 应用效果到世界状态
     *
     * @param stateWriter 世界状态写入器，用于修改状态
     * @param agent 执行动作的智能体实体
     */
    fun apply(stateWriter: WorldStateWriter, agent: Entity)
}
