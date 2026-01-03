package cn.jzl.ecs.entity

import cn.jzl.core.bits.BitSet
import kotlinx.atomicfu.atomic

class EntityStoreImpl : EntityStore {
    private val activeEntities = BitSet(1024)
    private val currentId = atomic(0)
    private val entityStack = EntityStack()

    override val size: Int get() = activeEntities.size

    override fun create(): Entity {
        val entity = entityStack.popOrElse { Entity(currentId.getAndIncrement(), 0) }
        activeEntities.set(entity.id)
        return entity
    }

    override fun create(entityId: Int): Entity {
        TODO("Not yet implemented")
    }

    override fun contains(entity: Entity): Boolean = entity.id in activeEntities

    override fun destroy(entity: Entity) {
        if (entity !in this) return
        activeEntities.clear(entity.id)
        entityStack.push(entity)
    }
}

