/**
 * GOAP 动作实现类
 *
 * 提供一个简单的 [GOAPAction] 实现，用于定义具体的动作。
 *
 * 使用示例：
 * ```kotlin
 * Action(
 *     name = "采集资源",
 *     cost = 1.0,
 *     preconditions = sequenceOf(
 *         Precondition { state, agent -> state.getValue(agent, HasTool) }
 *     ),
 *     effects = sequenceOf(
 *         ActionEffect { state, agent -> state.setValue(ResourceCount, 10) }
 *     ),
 *     task = ActionTask {
 *         delay(1.seconds)
 *         // 执行采集逻辑
 *     }
 * )
 * ```
 */
package cn.jzl.sect.ai.goap

/**
 * GOAP 动作的具体实现
 *
 * @property name 动作名称，用于调试和日志
 * @property cost 动作执行成本，用于 A* 搜索中的路径代价计算
 * @property preconditions 动作执行的前置条件序列
 * @property effects 动作执行后产生的效果序列
 * @property task 动作执行的实际任务
 */
class Action(
    override val name: String,
    override val cost: Double,
    override val preconditions: Sequence<Precondition>,
    override val effects: Sequence<ActionEffect>,
    override val task: ActionTask
) : GOAPAction
