package cn.jzl.ecs.family

import cn.jzl.core.bits.BitSet
import cn.jzl.ecs.relation.Relation

interface FamilyMatchScope {

    val allArchetypeBits: BitSet

    fun getArchetypeBits(relation: Relation): BitSet
}