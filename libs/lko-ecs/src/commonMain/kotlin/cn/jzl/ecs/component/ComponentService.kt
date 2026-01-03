package cn.jzl.ecs.component

import androidx.collection.mutableIntObjectMapOf
import androidx.collection.mutableScatterMapOf
import cn.jzl.core.bits.BitSet
import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.id
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.relation.kind
import cn.jzl.ecs.relation.target
import kotlin.reflect.KClassifier

class ComponentService(override val world: World) : WorldOwner, ComponentProvider, ComponentStoreFactory<Any> {
    private val componentIdEntities = mutableScatterMapOf<KClassifier, ComponentId>()
    private val componentStoreFactories = mutableIntObjectMapOf<ComponentStoreFactory<*>>()

    @PublishedApi
    internal val entityTags = BitSet()

    @PublishedApi
    internal val singleRelationBits = BitSet()

    fun holdsData(relation: Relation): Boolean = relation.kind.id !in entityTags

    fun isSingleRelation(relation: Relation): Boolean = relation.kind.id in singleRelationBits

    fun isShadedComponent(relation: Relation): Boolean = components.sharedOf == relation.target

    override fun create(relation: Relation): ComponentStore<Any> {
        val factory = componentStoreFactories.getOrPut(relation.kind.data) { ComponentStoreFactory.Companion }
        @Suppress("UNCHECKED_CAST")
        return factory.create(relation) as ComponentStore<Any>
    }

    override fun getOrRegisterEntityForClass(classifier: KClassifier): ComponentId {
        return componentIdEntities.getOrPut(classifier) { world.entityService.create(false) }
    }
}