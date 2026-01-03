package cn.jzl.ecs.relation

import kotlin.jvm.JvmInline

@JvmInline
value class EntityType(private val data: LongArray) : Sequence<Relation> {

    init {
        data.sort()
    }

    val size: Int get() = data.size

    fun isNotEmpty(): Boolean = size > 0
    fun isEmpty(): Boolean = size == 0

    operator fun get(index: Int): Relation = Relation(data[index])
    fun getOrNull(index: Int): Relation? = data.getOrNull(index)?.let { Relation(it) }
    fun indexOf(relation: Relation): Int = binarySearch(relation)
    operator fun contains(relation: Relation): Boolean = binarySearch(relation) >= 0

    operator fun plus(relation: Relation): EntityType = EntityType(data + relation.data)
    operator fun minus(relation: Relation): EntityType = EntityType(data.filter { it != relation.data }.toLongArray())

    @PublishedApi
    internal fun binarySearch(relation: Relation): Int {
        var left = 0
        var right = data.size - 1
        while (left <= right) {
            val mid = (left + right) ushr 1
            val midVal = data[mid]
            when {
                midVal < relation.data -> left = mid + 1
                midVal > relation.data -> right = mid - 1
                else -> return mid // 找到元素
            }
        }
        return -1
    }

    override fun iterator(): Iterator<Relation> = iterator {
        for (i in data.indices) {
            yield(Relation(data[i]))
        }
    }

    companion object {
        val empty: EntityType = EntityType(longArrayOf())

        operator fun invoke(relations: Sequence<Relation>): EntityType =
            EntityType(relations.map { it.data }.toSet().toLongArray())
    }
}