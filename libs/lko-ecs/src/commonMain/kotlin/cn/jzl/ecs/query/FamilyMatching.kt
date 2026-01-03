package cn.jzl.ecs.query

import cn.jzl.ecs.family.FamilyBuilder

interface FamilyMatching {
    val isMarkedNullable: Boolean
    val optionalGroup: OptionalGroup
    fun FamilyBuilder.matching()
}