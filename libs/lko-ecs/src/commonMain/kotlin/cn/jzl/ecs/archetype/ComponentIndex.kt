package cn.jzl.ecs.archetype

import cn.jzl.core.bits.fromLowHigh
import cn.jzl.core.bits.high
import cn.jzl.core.bits.low
import cn.jzl.ecs.entity.Entity
import kotlin.jvm.JvmInline

@JvmInline
value class ComponentIndex private constructor(val data: Long) {
    companion object{
        operator fun invoke(
            prefabEntity: Entity,
            index: Int
        ): ComponentIndex = ComponentIndex(Long.fromLowHigh(prefabEntity.data, index))
    }
}

val ComponentIndex.entity: Entity get() = Entity(data.low)
val ComponentIndex.index: Int get() = data.high