package cn.jzl.sect.core.ai

data class BehaviorState(
    val currentBehavior: BehaviorType = BehaviorType.CULTIVATE,
    val behaviorStartTime: Long = 0L,
    val lastBehaviorTime: Long = 0L
)
