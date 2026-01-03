package cn.jzl.ecs.entity

import cn.jzl.core.bits.extract08
import cn.jzl.core.bits.extract24
import cn.jzl.core.bits.insert08
import kotlin.jvm.JvmInline

@JvmInline
value class Entity @PublishedApi internal constructor(val data: Int) {

    override fun toString(): String = "Entity(id=${id}, version=${version})"

    companion object {
        val ENTITY_INVALID = Entity(-1)
        operator fun invoke(id: Int, version: Int): Entity = Entity(id.insert08(version, 24))
    }
}

val Entity.id: Int get() = data.extract24(0)
val Entity.version: Int get() = data.extract08(24)

