package cn.jzl.ecs.entity

interface EntityStore {
    val size: Int

    fun create() : Entity

    fun create(entityId: Int) : Entity
    operator fun contains(entity: Entity) : Boolean

    fun destroy(entity: Entity)
}


