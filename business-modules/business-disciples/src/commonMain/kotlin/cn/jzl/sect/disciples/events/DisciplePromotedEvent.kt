package cn.jzl.sect.disciples.events

import cn.jzl.ecs.entity.Entity
import cn.jzl.sect.core.sect.SectPositionType

data class DisciplePromotedEvent(
    val entity: Entity,
    val oldPosition: SectPositionType,
    val newPosition: SectPositionType
)
