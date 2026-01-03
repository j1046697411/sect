package cn.jzl.ecs.family

import cn.jzl.core.bits.BitSet
import cn.jzl.core.list.ObjectFastList
import cn.jzl.ecs.archetype.Archetype
import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.component.components
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.relation.component
import cn.jzl.ecs.relation.kind
import cn.jzl.ecs.relation.relation
import cn.jzl.ecs.relation.relations
import cn.jzl.ecs.relation.sharedComponent
import cn.jzl.ecs.relation.target

fun FamilyBuilder.relation(relation: Relation) {
    keys.add(relation.data)
    matcher(object : FamilyMatcher {
        override fun match(archetype: Archetype): Boolean {
            return archetype.entityType.indexOf(relation) != -1
        }

        override fun FamilyMatchScope.getArchetypeBits(): BitSet = getArchetypeBits(relation)
    })
}

inline fun <reified K> FamilyBuilder.relation(target: Entity): Unit = relation(relations.relation<K>(target))
inline fun <reified K, reified T> FamilyBuilder.relation(): Unit = relation(relations.relation<K, T>())
inline fun <reified C> FamilyBuilder.component(): Unit = relation(relations.component<C>())
inline fun <reified C> FamilyBuilder.sharedComponent(): Unit = relation(relations.sharedComponent<C>())

inline fun <reified K> FamilyBuilder.kind(): Unit = kind(components.id<K>())

fun FamilyBuilder.kind(kind: ComponentId) {
    val relation = relations.kind(kind)
    keys.add(relation.data)
    matcher(object : FamilyMatcher {
        override fun match(archetype: Archetype): Boolean {
            return archetype.entityType.any { it.kind == kind }
        }

        override fun FamilyMatchScope.getArchetypeBits(): BitSet = getArchetypeBits(relation)
    })
}

inline fun <reified T> FamilyBuilder.target(): Unit = target(components.id<T>())
fun FamilyBuilder.target(target: Entity) {
    val relation = relations.target(target)
    keys.add(relation.data)
    matcher(object : FamilyMatcher {
        override fun match(archetype: Archetype): Boolean {
            return archetype.entityType.any { it.target == target }
        }

        override fun FamilyMatchScope.getArchetypeBits(): BitSet = getArchetypeBits(relation)
    })
}

@PublishedApi
internal fun FamilyBuilder.composite(
    key: Long,
    block: FamilyBuilder.() -> Unit,
    factory: (List<FamilyMatcher>) -> FamilyMatcher
) {
    keys.add(key)
    val familyMatchers = mutableListOf<FamilyMatcher>()
    val familyBuilder = object : FamilyBuilder by this {
        override fun matcher(familyMatcher: FamilyMatcher) {
            familyMatchers.add(familyMatcher)
        }
    }
    familyBuilder.block()
    matcher(factory(familyMatchers))
}

fun FamilyBuilder.and(block: FamilyBuilder.() -> Unit) {
    val result = BitSet()
    composite(0, block) { familyMatchers ->
        object : FamilyMatcher {
            override fun match(archetype: Archetype): Boolean = familyMatchers.all { it.match(archetype) }

            override fun FamilyMatchScope.getArchetypeBits(): BitSet {
                result.clear()
                familyMatchers.forEachIndexed { index, matcher ->
                    val bits = matcher.run { getArchetypeBits() }
                    if (index == 0) result.or(bits) else result.and(bits)
                }
                return result
            }
        }
    }
}

fun FamilyBuilder.or(block: FamilyBuilder.() -> Unit) {
    val result = BitSet()
    composite(1, block) { familyMatchers ->
        object : FamilyMatcher {
            override fun match(archetype: Archetype): Boolean = familyMatchers.any { it.match(archetype) }

            override fun FamilyMatchScope.getArchetypeBits(): BitSet {
                result.clear()
                familyMatchers.forEach { matcher ->
                    result.or(matcher.run { getArchetypeBits() })
                }
                return result
            }
        }
    }
}

fun FamilyBuilder.xor(block: FamilyBuilder.() -> Unit) {
    val result = BitSet()
    val temp = BitSet(0)
    val m2 = BitSet()
    val allArchetypeResults = ObjectFastList<BitSet>()
    composite(2, block) { familyMatchers ->
        object : FamilyMatcher {
            override fun match(archetype: Archetype): Boolean = familyMatchers.count { it.match(archetype) } == 1

            override fun FamilyMatchScope.getArchetypeBits(): BitSet {
                result.clear()
                when (familyMatchers.size) {
                    0 -> return result
                    1 -> return familyMatchers.single().run { getArchetypeBits() }
                    2 -> {
                        // 两个匹配器的特殊情况 - 使用原生 XOR 操作
                        val first = familyMatchers.component1().run { getArchetypeBits() }
                        val second = familyMatchers.component2().run { getArchetypeBits() }
                        result.or(first)
                        result.xor(second)
                        return result
                    }

                    else -> {
                        // 多个匹配器的通用情况
                        allArchetypeResults.clear()

                        // 保持您的高性能批量插入操作
                        allArchetypeResults.safeInsertLast(familyMatchers.size) {
                            familyMatchers.forEach { unsafeInsert(it.run { getArchetypeBits() }) }
                        }

                        // 计算所有位的 OR (至少在一个匹配器中存在的位)
                        for (i in 0 until allArchetypeResults.size) {
                            result.or(allArchetypeResults[i])
                        }

                        // 计算 M2 (在至少两个匹配器中存在的位)
                        m2.clear()
                        for (i in 0 until allArchetypeResults.size) {
                            val first = allArchetypeResults[i]
                            for (j in i + 1 until allArchetypeResults.size) {
                                val second = allArchetypeResults[j]
                                temp.clear()
                                temp.or(first)
                                temp.and(second)
                                m2.or(temp)
                            }
                        }

                        // 最终结果: OR_all AND NOT M2
                        result.andNot(m2)
                        return result
                    }
                }
            }
        }
    }
}


fun FamilyBuilder.not(block: FamilyBuilder.() -> Unit) {
    val result = BitSet()
    composite(3, block) { familyMatchers ->
        object : FamilyMatcher {
            override fun match(archetype: Archetype): Boolean = familyMatchers.none { it.match(archetype) }

            override fun FamilyMatchScope.getArchetypeBits(): BitSet {
                result.clear()
                familyMatchers.forEach {
                    result.or(it.run { getArchetypeBits() })
                }
                result.xor(allArchetypeBits)
                return result
            }
        }
    }
}