package cn.jzl.sect.core.ai

/**
 * 当前行为组件 - 存储实体当前的行为状态
 */
data class CurrentBehavior(
    val type: BehaviorType = BehaviorType.CULTIVATE,
    val startTime: Long = 0L,
    val lastBehaviorTime: Long = 0L
)
