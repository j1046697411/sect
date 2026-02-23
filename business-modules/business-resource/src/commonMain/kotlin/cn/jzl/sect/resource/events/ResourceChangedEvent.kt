package cn.jzl.sect.resource.events

import cn.jzl.sect.resource.components.ResourceType

data class ResourceChangedEvent(
    val type: ResourceType,
    val previousAmount: Long,
    val currentAmount: Long,
    val change: Long,
    val reason: ResourceChangeReason
)

enum class ResourceChangeReason {
    PRODUCTION,
    CONSUMPTION,
    ADJUSTMENT
}
