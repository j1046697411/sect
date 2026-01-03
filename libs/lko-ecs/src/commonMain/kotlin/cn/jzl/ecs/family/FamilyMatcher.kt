package cn.jzl.ecs.family

import cn.jzl.core.bits.BitSet
import cn.jzl.ecs.archetype.Archetype

interface FamilyMatcher {
    fun match(archetype: Archetype): Boolean

    fun FamilyMatchScope.getArchetypeBits(): BitSet
}

