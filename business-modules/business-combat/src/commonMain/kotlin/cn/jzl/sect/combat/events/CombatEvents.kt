package cn.jzl.sect.combat.events

data class CombatStartedEvent(
    val attackerId: Long,
    val defenderId: Long
)

data class CombatEndedEvent(
    val winnerId: Long?,
    val loserId: Long?,
    val isDraw: Boolean
)

data class DamageDealtEvent(
    val attackerId: Long,
    val defenderId: Long,
    val damage: Int
)
