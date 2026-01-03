package cn.jzl.ecs.archetype

import cn.jzl.ecs.component.ComponentStoreFactory
import cn.jzl.ecs.entity.Entities
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.relation.EntityType
import cn.jzl.ecs.relation.Relation

class Table(
    val holdsDataType: EntityType,
    private val componentStoreFactory: ComponentStoreFactory<Any>
) {
    private val componentArrays by lazy {
        Array(holdsDataType.size) {
            componentStoreFactory.create(holdsDataType[it])
        }
    }

    internal val entities = Entities()

    val size: Int get() = entities.size

    fun insert(entity: Entity, provider: Relation.(Int) -> Any): Int {
        val size = entities.size
        entities.add(entity)
        if (holdsDataType.isNotEmpty()) {
            holdsDataType.forEachIndexed { index, relation ->
                componentArrays[index].add(relation.provider(index))
            }
        }
        return size
    }

    operator fun get(entityIndex: Int): Entity = entities[entityIndex]

    operator fun get(entityIndex: Int, componentIndex: Int): Any {
        require(entityIndex in 0 until entities.size)
        require(componentIndex in 0 until holdsDataType.size)
        return componentArrays[componentIndex][entityIndex]
    }

    operator fun set(entityIndex: Int, componentIndex: Int, value: Any) {
        require(entityIndex in 0 until entities.size)
        require(componentIndex in 0 until holdsDataType.size)
        componentArrays[componentIndex][entityIndex] = value
    }

    fun removeAt(entityIndex: Int) {
        require(entityIndex in 0 until entities.size)
        entities.removeAt(entityIndex)
        if (holdsDataType.isNotEmpty()) {
            componentArrays.forEach { it.removeAt(entityIndex) }
        }
    }
}

