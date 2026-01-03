package cn.jzl.ecs.family

import androidx.collection.mutableObjectListOf
import cn.jzl.core.list.ObjectFastList
import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.archetype.Archetype

class Family(override val world: World, val familyMatcher: FamilyMatcher) : WorldOwner {
    @PublishedApi
    internal val archetypes = mutableObjectListOf<Archetype>()
    val size: Int get() = archetypes.fold(0) { acc, archetype -> acc + archetype.size }

    internal fun addArchetype(archetype: Archetype): Unit {
        archetypes.add(archetype)
    }
}