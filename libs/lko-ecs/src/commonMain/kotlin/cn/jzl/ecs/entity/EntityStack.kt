package cn.jzl.ecs.entity

import cn.jzl.core.list.IntFastList
import kotlin.jvm.JvmInline

@JvmInline
value class EntityStack(private val stack: IntFastList = IntFastList(256)) {
    fun push(entity: Entity) {
        stack.add(entity.data)
    }

    fun popOrElse(orElse: () -> Entity): Entity {
        return if (stack.isEmpty()) orElse() else Entity(stack.removeFirst()).upgrade()
    }
    private fun Entity.upgrade(): Entity = Entity(id, version + 1)

}