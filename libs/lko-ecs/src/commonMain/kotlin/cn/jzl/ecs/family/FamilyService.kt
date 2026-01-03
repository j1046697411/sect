package cn.jzl.ecs.family

import androidx.collection.mutableLongObjectMapOf
import androidx.collection.mutableScatterMapOf
import cn.jzl.core.bits.BitSet
import cn.jzl.core.list.LongFastList
import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.archetype.Archetype
import cn.jzl.ecs.component.components
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.relation.kind
import cn.jzl.ecs.relation.relations
import cn.jzl.ecs.relation.target

class FamilyService(override val world: World) : FamilyMatchScope, WorldOwner {

    override val allArchetypeBits: BitSet = BitSet.Companion()
    private val componentMap = mutableLongObjectMapOf<BitSet>()
    private val families = mutableScatterMapOf<String, Family>()
    private val emptyBits = BitSet()

    override fun getArchetypeBits(relation: Relation): BitSet {
        return componentMap[relation.data] ?: emptyBits
    }

    @PublishedApi
    internal fun registerArchetype(archetype: Archetype) {
        allArchetypeBits.set(archetype.id)
        fun setRelation(relation: Relation): Unit =
            componentMap.getOrPut(relation.data) { BitSet.Companion() }.set(archetype.id)
        archetype.entityType.forEach { relation ->
            if (relation.isRelation()) {
                setRelation(relations.kind(relation.kind))
                setRelation(relations.target(relation.target))
            }
            setRelation(relation)
        }
        families.forEachValue {
            if (!it.familyMatcher.match(archetype)) {
                return@forEachValue
            }
            it.addArchetype(archetype)
        }
    }

    private fun Relation.isRelation(): Boolean = components.componentOf != target

    fun family(block: FamilyBuilder.() -> Unit): Family {
        val keys = LongFastList()
        val familyMatcher = and(keys, block)
        val key = keys.joinToString("|") { it.toString(16) }
        return families.getOrPut(key) {
            val family = Family(world, familyMatcher)
            familyMatcher.run { getArchetypeBits() }.map { world.archetypeService[it] }.forEach(family::addArchetype)
            family
        }
    }

    fun buildArchetype(receiver: (Archetype) -> Unit, block: FamilyBuilder.() -> Unit) {
        val keys = LongFastList()
        val familyMatcher = and(keys, block)
        familyMatcher.run { getArchetypeBits() }.map { world.archetypeService[it] }.forEach(receiver)
    }

    private fun and(keys: LongFastList, block: FamilyBuilder.() -> Unit): FamilyMatcher {
        var matcher: FamilyMatcher? = null
        val familyBuilder = object : FamilyBuilder, WorldOwner by this {
            override val keys: LongFastList get() = keys
            override fun matcher(familyMatcher: FamilyMatcher) {
                matcher = familyMatcher
            }
        }
        familyBuilder.and(block)
        return requireNotNull(matcher) { "" }
    }
}