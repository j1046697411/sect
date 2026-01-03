package cn.jzl.ecs.entity

import androidx.collection.mutableObjectListOf

class BucketedLongArray(private val bucketSize: Int = 1024) {
    private var maxSupportedSize = 0
    var size: Int = 0
        private set
    private val buckets = mutableObjectListOf<LongArray>()

    fun ensureSize(including: Int) {
        var maxSupportedSize = maxSupportedSize
        while (including >= maxSupportedSize) {
            buckets.add(LongArray(bucketSize))
            maxSupportedSize += bucketSize
        }
        this.maxSupportedSize = maxSupportedSize
    }

    operator fun set(index: Int, value: Long) {
        ensureSize(index)
        buckets[index / bucketSize][index % bucketSize] = value
        size = maxOf(size, index + 1)
    }

    operator fun get(index: Int): Long {
        require(index in 0 until size)
        return buckets[index / bucketSize][index % bucketSize]
    }
}