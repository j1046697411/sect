package cn.jzl.core.bits

import cn.jzl.core.list.LongFastList
import kotlin.jvm.JvmInline
import kotlin.math.max

@JvmInline
value class BitSet(val data: LongFastList) : Sequence<Int> {

    val size: Int get() = lastBitIndex() // 每个Long存储64位

    /**
     * 检查BitSet是否为空（所有位都为0）
     */
    fun isEmpty(): Boolean = data.all { it == 0L }

    fun isNotEmpty() : Boolean = data.any { it != 0L }

    private fun lastBitIndex(): Int {
        for (i in data.size - 1 downTo 0) {
            val value = data[i]
            if (value != 0L) {
                return (i shl 6) + 64 - value.countLeadingZeroBits()
            }
        }
        return 0
    }

    /**
     * 计算设置为1的位的数量
     */
    fun countOneBits(): Int = data.fold(0) { acc, word -> acc + word.countOneBits() }

    /**
     * 清空所有位
     */
    fun clear() = data.clear()

    /**
     * 获取指定索引位置的位值
     */
    operator fun get(index: Int): Boolean {
        val wordIndex = index.wordIndex
        return wordIndex < data.size && (data[wordIndex] and index.bitMask) != 0L
    }

    /**
     * 设置指定索引位置的位值
     */
    operator fun set(index: Int, value: Boolean) {
        val wordIndex = index.wordIndex
        // 确保有足够的空间
        ensureCapacity(wordIndex + 1)

        if (value) {
            data[wordIndex] = data[wordIndex] or index.bitMask
        } else {
            data[wordIndex] = data[wordIndex] and index.bitMask.inv()
        }
    }

    /**
     * 检查两个BitSet是否有交集
     */
    fun intersects(other: BitSet): Boolean {
        val size = minOf(other.data.size, data.size)
        for (i in size - 1 downTo 0) {
            if (other.data[i] and data[i] != 0L) return true
        }
        return false
    }

    /**
     * 检查this是否包含other的所有设置位
     */
    operator fun contains(other: BitSet): Boolean {
        // 首先检查other的数据长度是否超过this
        if (other.data.size > data.size) {
            // 检查other超出部分是否有设置位
            for (i in data.size until other.data.size) {
                if (other.data[i] != 0L) return false
            }
        }

        // 检查共同部分
        val commonSize = minOf(other.data.size, data.size)
        for (i in 0 until commonSize) {
            if ((other.data[i] and data[i]) != other.data[i]) {
                return false
            }
        }
        return true
    }

    /**
     * 迭代所有设置为1的位的索引
     */
    override fun iterator(): Iterator<Int> = iterator {
        var nextId = nextSetBit(0)
        while (nextId != -1) {
            yield(nextId)
            nextId = nextSetBit(nextId + 1)
        }
    }

    /**
     * 位或操作，将other的位设置到this
     */
    fun or(other: BitSet) {
        ensureCapacity(other.data.size)
        for (i in 0 until other.data.size) {
            data[i] = data[i] or other.data[i]
        }
    }

    /**
     * 位与操作，保留同时在this和other中设置的位
     */
    fun and(other: BitSet) {
        val minSize = minOf(data.size, other.data.size)
        for (i in 0 until minSize) {
            data[i] = data[i] and other.data[i]
        }
        // 清除超出other范围的位
        for (i in minSize until data.size) {
            data[i] = 0L
        }
    }

    /**
     * 位异或操作，保留只在this或只在other中设置的位
     */
    fun xor(other: BitSet) {
        ensureCapacity(other.data.size)
        for (i in 0 until other.data.size) {
            data[i] = data[i] xor other.data[i]
        }
    }

    fun andNot(other: BitSet) {
        ensureCapacity(max(data.size, other.data.size))
        for (i in 0 until data.size) {
            data[i] = data[i] and (other.data.getOrNull(i)?.inv() ?: -1L)
        }
    }

    /**
     * 位取反操作，翻转所有位
     */
    fun not() {
        for (i in 0 until data.size) {
            data[i] = data[i].inv()
        }
    }

    /**
     * 设置指定索引位置的位为1
     */
    fun set(index: Int) = set(index, true)

    /**
     * 清除指定索引位置的位（设置为0）
     */
    fun clear(index: Int) = set(index, false)

    /**
     * 翻转指定索引位置的位
     */
    fun flip(index: Int) {
        val wordIndex = index.wordIndex
        ensureCapacity(wordIndex + 1)
        data[wordIndex] = data[wordIndex] xor index.bitMask
    }

    /**
     * 确保BitSet至少可以容纳指定数量的Long字
     */
    private fun ensureCapacity(wordCount: Int) = data.ensureCapacity(wordCount, 0)

    /**
     * 创建BitSet的副本
     */
    fun copy(): BitSet {
        val newData = LongFastList()
        newData.insertLastAll(data)
        return BitSet(newData)
    }

    fun nextSetBit(fromIndex: Int = 0): Int {
        if (fromIndex < 0) return -1
        var wordIndex = fromIndex.wordIndex
        if (wordIndex >= data.size) return -1

        val bitIndex = fromIndex.bitIndex
        var word = data[wordIndex]
        if (bitIndex != 0) word = word and (fromIndex.bitMask - 1L).inv()

        while (true) {
            if (word != 0L) return (wordIndex shl 6) + word.countTrailingZeroBits()
            if (++wordIndex >= data.size) return -1
            word = data[wordIndex]
        }
    }

    fun nextClearBit(fromIndex: Int = 0): Int {
        if (fromIndex < 0) return -1
        var wordIndex = fromIndex.wordIndex

        while (wordIndex < data.size) {
            var word = data[wordIndex].inv()
            if (wordIndex == fromIndex.wordIndex) {
                val mask = (1L shl fromIndex.bitIndex) - 1
                word = word and mask.inv()
            }

            if (word != 0L) {
                return (wordIndex shl 6) + word.countTrailingZeroBits()
            }
            wordIndex++
        }
        return data.size * 64
    }

    private val Int.wordIndex: Int get() = this ushr 6
    private val Int.bitIndex: Int get() = this and 0b111111
    private val Int.bitMask: Long get() = 1L shl bitIndex

    /**
     * 创建一个空的BitSet
     */
    companion object {
        /**
         * 创建一个具有指定初始容量的BitSet
         */
        operator fun invoke(initialCapacity: Int = 63): BitSet {
            val wordCount = (initialCapacity + 63) / 64 // 向上取整到Long的数量
            return BitSet(LongFastList(wordCount))
        }
    }
}