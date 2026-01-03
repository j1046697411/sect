package cn.jzl.ecs.entity

import androidx.collection.MutableIntList
import androidx.collection.mutableIntListOf
import kotlin.jvm.JvmInline

@JvmInline
value class Entities(private val entities: MutableIntList = mutableIntListOf()) : Collection<Entity> {

    override val size: Int get() = entities.size

    override fun isEmpty(): Boolean = entities.isEmpty()

    fun add(entity: Entity) {
        entities.add(entity.data)
    }

    operator fun get(index: Int): Entity = Entity(entities[index])

    operator fun set(index: Int, entity: Entity) {
        entities[index] = entity.data
    }

    override fun contains(element: Entity): Boolean {
        return element.data in entities
    }

    override fun containsAll(elements: Collection<Entity>): Boolean = elements.all { it in this }

    fun removeAt(index: Int) : Entity {
        val lastIndex = entities.lastIndex
        val removedEntity = Entity(entities[index])
        if (lastIndex != index) entities[index] = entities[lastIndex]
        entities.removeAt(lastIndex)
        return removedEntity
    }

    override fun iterator(): Iterator<Entity> = iterator {
        entities.forEach { yield(Entity(it)) }
    }
}