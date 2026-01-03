package cn.jzl.ecs.relation

import cn.jzl.core.bits.fromLowHigh
import cn.jzl.core.bits.high
import cn.jzl.core.bits.low
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.entity.Entity
import kotlin.jvm.JvmInline

@JvmInline
value class Relation @PublishedApi internal constructor(val data: Long) : Comparable<Relation> {

    override fun compareTo(other: Relation): Int = data.compareTo(other.data)

    override fun toString(): String = "Relation(kind=${kind}, target=${target})"

    companion object {
        operator fun invoke(kind: ComponentId, target: Entity): Relation {
            return Relation(Long.fromLowHigh(kind.data, target.data))
        }
    }
}

val Relation.kind: ComponentId get() = Entity(data.low)
val Relation.target: Entity get() = Entity(data.high)

