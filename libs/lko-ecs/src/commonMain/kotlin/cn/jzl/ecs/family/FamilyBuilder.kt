package cn.jzl.ecs.family

import cn.jzl.core.list.LongFastList
import cn.jzl.ecs.WorldOwner

interface FamilyBuilder : WorldOwner {
    val keys: LongFastList
    fun matcher(familyMatcher: FamilyMatcher)
}