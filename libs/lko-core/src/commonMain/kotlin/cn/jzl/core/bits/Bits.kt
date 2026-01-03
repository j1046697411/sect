@file:Suppress("NOTHING_TO_INLINE")

package cn.jzl.core.bits

import kotlin.jvm.JvmInline

inline fun Short.reverseBytes(): Short {
    val low = ((this.toInt() ushr 0) and 0xFF)
    val high = ((this.toInt() ushr 8) and 0xFF)
    return ((high and 0xFF) or (low shl 8)).toShort()
}

inline fun Char.reverseBytes(): Char = this.code.toShort().reverseBytes().toInt().toChar()

inline fun Int.reverseBytes(): Int {
    val v0 = ((this ushr 0) and 0xFF)
    val v1 = ((this ushr 8) and 0xFF)
    val v2 = ((this ushr 16) and 0xFF)
    val v3 = ((this ushr 24) and 0xFF)
    return (v0 shl 24) or (v1 shl 16) or (v2 shl 8) or (v3 shl 0)
}

inline fun Long.reverseBytes(): Long {
    val v0 = (this ushr 0).toInt().reverseBytes().toLong() and 0xFFFFFFFFL
    val v1 = (this ushr 32).toInt().reverseBytes().toLong() and 0xFFFFFFFFL
    return (v0 shl 32) or (v1 shl 0)
}

inline fun Int.reverseBits(): Int {
    var v = this
    v = ((v ushr 1) and 0x55555555) or ((v and 0x55555555) shl 1)
    v = ((v ushr 2) and 0x33333333) or ((v and 0x33333333) shl 2) // swap consecutive pairs
    v = ((v ushr 4) and 0x0F0F0F0F) or ((v and 0x0F0F0F0F) shl 4) // swap nibbles ...
    v = ((v ushr 8) and 0x00FF00FF) or ((v and 0x00FF00FF) shl 8) // swap bytes
    v = ((v ushr 16) and 0x0000FFFF) or ((v and 0x0000FFFF) shl 16) // swap 2-byte long pairs
    return v
}

inline fun Int.countLeadingZeros(): Int = this.countLeadingZeroBits()

inline fun Int.countTrailingZeros(): Int = this.countTrailingZeroBits()

inline fun Int.countLeadingOnes(): Int = this.inv().countLeadingZeros()

inline fun Int.countTrailingOnes(): Int = this.inv().countTrailingZeros()

inline fun Int.signExtend(bits: Int): Int = (this shl (32 - bits)) shr (32 - bits) // Int.SIZE_BITS
inline fun Long.signExtend(bits: Int): Long = (this shl (64 - bits)) shr (64 - bits) // Long.SIZE_BITS

inline fun Int.mask(): Int = (1 shl this) - 1
inline fun Long.mask(): Long = (1L shl this.toInt()) - 1L

inline fun Int.mask(offset: Int): Int = mask() shl offset
inline fun Long.mask(offset: Int): Long = mask() shl offset

@JvmInline
value class IntMaskRange private constructor(val raw: Int) {
    val offset: Int get() = raw.extract08(0)
    val size: Int get() = raw.extract08(8)
    fun toMask(): Int = size.mask(offset)
    fun extract(value: Int): Int = value.extract(offset, size)
    fun extractSigned(value: Int, signed: Boolean = true): Int = value.extractSigned(offset, size, signed)

    companion object {
        fun fromRange(offset: Int, size: Int): IntMaskRange {
            return IntMaskRange(0.insert08(offset, 0).insert08(size, 8))
        }

        fun fromMask(mask: Int): IntMaskRange {
            if (mask == 0) return IntMaskRange(0)
            val offset = mask.countTrailingZeroBits()
            val size = (32 - mask.countLeadingZeroBits()) - offset
            return fromRange(offset, size)
        }
    }

    operator fun component1(): Int = offset
    operator fun component2(): Int = size

    override fun toString(): String = "IntMaskRange(offset=$offset, size=$size)"
}

fun Int.extractMaskRange(): IntMaskRange = IntMaskRange.fromMask(this)
inline fun Int.extract(offset: Int, count: Int): Int = (this ushr offset) and count.mask()
inline fun Int.extract(offset: Int): Boolean = extract01(offset) != 0
inline fun Int.extractBool(offset: Int): Boolean = extract01(offset) != 0
inline fun Int.extract01(offset: Int): Int = (this ushr offset) and 0b1
inline fun Int.extract02(offset: Int): Int = (this ushr offset) and 0b11
inline fun Int.extract03(offset: Int): Int = (this ushr offset) and 0b111
inline fun Int.extract04(offset: Int): Int = (this ushr offset) and 0b1111
inline fun Int.extract05(offset: Int): Int = (this ushr offset) and 0b11111
inline fun Int.extract06(offset: Int): Int = (this ushr offset) and 0b111111
inline fun Int.extract07(offset: Int): Int = (this ushr offset) and 0b1111111
inline fun Int.extract08(offset: Int): Int = (this ushr offset) and 0b11111111
inline fun Int.extract09(offset: Int): Int = (this ushr offset) and 0b111111111
inline fun Int.extract10(offset: Int): Int = (this ushr offset) and 0b1111111111
inline fun Int.extract11(offset: Int): Int = (this ushr offset) and 0b11111111111
inline fun Int.extract12(offset: Int): Int = (this ushr offset) and 0b111111111111
inline fun Int.extract13(offset: Int): Int = (this ushr offset) and 0b1111111111111
inline fun Int.extract14(offset: Int): Int = (this ushr offset) and 0b11111111111111
inline fun Int.extract15(offset: Int): Int = (this ushr offset) and 0b111111111111111
inline fun Int.extract16(offset: Int): Int = (this ushr offset) and 0b1111111111111111
inline fun Int.extract17(offset: Int): Int = (this ushr offset) and 0b11111111111111111
inline fun Int.extract18(offset: Int): Int = (this ushr offset) and 0b111111111111111111
inline fun Int.extract19(offset: Int): Int = (this ushr offset) and 0b1111111111111111111
inline fun Int.extract20(offset: Int): Int = (this ushr offset) and 0b11111111111111111111
inline fun Int.extract21(offset: Int): Int = (this ushr offset) and 0b111111111111111111111
inline fun Int.extract22(offset: Int): Int = (this ushr offset) and 0b1111111111111111111111
inline fun Int.extract23(offset: Int): Int = (this ushr offset) and 0b11111111111111111111111
inline fun Int.extract24(offset: Int): Int = (this ushr offset) and 0xFFFFFF
inline fun Int.extract25(offset: Int): Int = (this ushr offset) and 0b1111111111111111111111111
inline fun Int.extract26(offset: Int): Int = (this ushr offset) and 0b11111111111111111111111111
inline fun Int.extract27(offset: Int): Int = (this ushr offset) and 0b111111111111111111111111111
inline fun Int.extract28(offset: Int): Int = (this ushr offset) and 0b1111111111111111111111111111
inline fun Int.extract29(offset: Int): Int = (this ushr offset) and 0b11111111111111111111111111111
inline fun Int.extract30(offset: Int): Int = (this ushr offset) and 0b111111111111111111111111111111
inline fun Int.extract31(offset: Int): Int = (this ushr offset) and 0b1111111111111111111111111111111
inline fun Int.extract32(offset: Int): Int = (this ushr offset) and -1

inline fun Int.extractSigned(offset: Int, count: Int, signed: Boolean): Int =
    if (signed) extractSigned(offset, count) else extract(offset, count)

inline fun Int.extractSigned(offset: Int, count: Int): Int = ((this ushr offset) and count.mask()).signExtend(count)
inline fun Int.extract8Signed(offset: Int): Int = (this ushr offset).toByte().toInt()
inline fun Int.extract16Signed(offset: Int): Int = (this ushr offset).toShort().toInt()

inline fun Int.extractByte(offset: Int): Byte = (this ushr offset).toByte()
inline fun Int.extractShort(offset: Int): Short = (this ushr offset).toShort()
inline fun Int.extractScaled(offset: Int, count: Int, scale: Int): Int = (extract(offset, count) * scale) / count.mask()
inline fun Int.extractScaledF01(offset: Int, count: Int): Float {
    return extract(offset, count).toFloat() / count.mask().toFloat()
}

inline fun Int.extractScaledFF(offset: Int, count: Int): Int = extractScaled(offset, count, 0xFF)
inline fun Int.extractScaledFFDefault(offset: Int, count: Int, default: Int): Int =
    if (count == 0) default else extractScaled(offset, count, 0xFF)

inline fun Int.insert(value: Int, offset: Int, count: Int): Int {
    val mask = count.mask() shl offset
    val value = (value shl offset) and mask
    return (this and mask.inv()) or value
}

inline fun Int.insertNoClear(value: Int, offset: Int, count: Int): Int {
    return this or ((value and count.mask()) shl offset)
}

inline fun Int.clear(offset: Int, count: Int): Int {
    return (this and (count.mask() shl offset).inv())
}

inline fun Int.insert01(value: Int, offset: Int): Int = insertMask(value, offset, 0b1)
inline fun Int.insert02(value: Int, offset: Int): Int = insertMask(value, offset, 0b11)
inline fun Int.insert03(value: Int, offset: Int): Int = insertMask(value, offset, 0b111)
inline fun Int.insert04(value: Int, offset: Int): Int = insertMask(value, offset, 0b1111)
inline fun Int.insert05(value: Int, offset: Int): Int = insertMask(value, offset, 0b11111)
inline fun Int.insert06(value: Int, offset: Int): Int = insertMask(value, offset, 0b111111)
inline fun Int.insert07(value: Int, offset: Int): Int = insertMask(value, offset, 0b1111111)
inline fun Int.insert08(value: Int, offset: Int): Int = insertMask(value, offset, 0b11111111)
inline fun Int.insert09(value: Int, offset: Int): Int = insertMask(value, offset, 0b111111111)
inline fun Int.insert10(value: Int, offset: Int): Int = insertMask(value, offset, 0b1111111111)
inline fun Int.insert11(value: Int, offset: Int): Int = insertMask(value, offset, 0b11111111111)
inline fun Int.insert12(value: Int, offset: Int): Int = insertMask(value, offset, 0b111111111111)
inline fun Int.insert13(value: Int, offset: Int): Int = insertMask(value, offset, 0b1111111111111)
inline fun Int.insert14(value: Int, offset: Int): Int = insertMask(value, offset, 0b11111111111111)
inline fun Int.insert15(value: Int, offset: Int): Int = insertMask(value, offset, 0b111111111111111)
inline fun Int.insert16(value: Int, offset: Int): Int = insertMask(value, offset, 0b1111111111111111)
inline fun Int.insert17(value: Int, offset: Int): Int = insertMask(value, offset, 0b11111111111111111)
inline fun Int.insert18(value: Int, offset: Int): Int = insertMask(value, offset, 0b111111111111111111)
inline fun Int.insert19(value: Int, offset: Int): Int = insertMask(value, offset, 0b1111111111111111111)
inline fun Int.insert20(value: Int, offset: Int): Int = insertMask(value, offset, 0b11111111111111111111)
inline fun Int.insert21(value: Int, offset: Int): Int = insertMask(value, offset, 0b111111111111111111111)
inline fun Int.insert22(value: Int, offset: Int): Int = insertMask(value, offset, 0b1111111111111111111111)
inline fun Int.insert23(value: Int, offset: Int): Int = insertMask(value, offset, 0b11111111111111111111111)
inline fun Int.insert24(value: Int, offset: Int): Int = insertMask(value, offset, 0b111111111111111111111111)
inline fun Int.insert25(value: Int, offset: Int): Int = insertMask(value, offset, 0b1111111111111111111111111)
inline fun Int.insert26(value: Int, offset: Int): Int = insertMask(value, offset, 0b11111111111111111111111111)
inline fun Int.insert27(value: Int, offset: Int): Int = insertMask(value, offset, 0b111111111111111111111111111)
inline fun Int.insert28(value: Int, offset: Int): Int = insertMask(value, offset, 0b1111111111111111111111111111)
inline fun Int.insert29(value: Int, offset: Int): Int = insertMask(value, offset, 0b11111111111111111111111111111)
inline fun Int.insert30(value: Int, offset: Int): Int = insertMask(value, offset, 0b111111111111111111111111111111)
inline fun Int.insert31(value: Int, offset: Int): Int = insertMask(value, offset, 0b1111111111111111111111111111111)
inline fun Int.insert32(value: Int, offset: Int): Int = insertMask(value, offset, -1)

inline fun Int.fastInsert(value: Int, offset: Int): Int = this or (value shl offset)
inline fun Int.fastInsert24(value: Int, offset: Int): Int = this or ((value and 0xFFFFFF) shl offset)
inline fun Int.fastInsert16(value: Int, offset: Int): Int = this or ((value and 0xFFFF) shl offset)
inline fun Int.fastInsert12(value: Int, offset: Int): Int = this or ((value and 0xFFF) shl offset)
inline fun Int.fastInsert08(value: Int, offset: Int): Int = this or ((value and 0xFF) shl offset)
inline fun Int.fastInsert07(value: Int, offset: Int): Int = this or ((value and 0b1111111) shl offset)
inline fun Int.fastInsert06(value: Int, offset: Int): Int = this or ((value and 0b111111) shl offset)
inline fun Int.fastInsert05(value: Int, offset: Int): Int = this or ((value and 0b11111) shl offset)
inline fun Int.fastInsert04(value: Int, offset: Int): Int = this or ((value and 0b1111) shl offset)
inline fun Int.fastInsert03(value: Int, offset: Int): Int = this or ((value and 0b111) shl offset)
inline fun Int.fastInsert02(value: Int, offset: Int): Int = this or ((value and 0b11) shl offset)
inline fun Int.fastInsert01(value: Int, offset: Int): Int = this or ((value and 0b1) shl offset)
inline fun Int.fastInsert(value: Boolean, offset: Int): Int = fastInsert(if (value) 1 else 0, offset)

inline fun Int.insertMask(value: Int, offset: Int, mask: Int): Int {
    return (this and (mask shl offset).inv()) or ((value and mask) shl offset)
}

inline fun Int.insert(value: Boolean, offset: Int): Int {
    val bits = (1 shl offset)
    return if (value) this or bits else this and bits.inv()
}

inline fun Int.insertScaled(value: Int, offset: Int, count: Int, scale: Int): Int {
    return insert((value * count.mask()) / scale, offset, count)
}

inline fun Int.insertScaledFF(value: Int, offset: Int, count: Int): Int {
    return if (count == 0) this else this.insertScaled(value, offset, count, 0xFF)
}

inline fun Int.insertScaledF01(value: Float, offset: Int, count: Int): Int {
    return this.insert((value.coerceIn(0f, 1f) * count.mask()).toInt(), offset, count)
}

inline infix fun Int.hasFlags(bits: Int): Boolean = (this and bits) == bits
inline infix fun Int.hasBits(bits: Int): Boolean = (this and bits) == bits

inline infix fun Int.hasBitSet(index: Int): Boolean = ((this ushr index) and 1) != 0

inline infix fun Long.hasFlags(bits: Long): Boolean = (this and bits) == bits
inline infix fun Long.hasBits(bits: Long): Boolean = (this and bits) == bits

inline fun bit(bit: Int): Int = 1 shl bit

inline fun Int.unsetBits(bits: Int): Int = this and bits.inv()

inline fun Int.setBits(bits: Int): Int = this or bits

inline fun Int.setBits(bits: Int, set: Boolean): Int = if (set) setBits(bits) else unsetBits(bits)

inline fun Int.without(bits: Int): Int = this and bits.inv()
inline fun Int.with(bits: Int): Int = this or bits

inline fun Long.without(bits: Long): Long = this and bits.inv()
inline fun Long.with(bits: Long): Long = this or bits

inline val Long.high: Int get() = (this ushr 32).toInt()
inline val Long.low: Int get() = (this and 0xffffffff).toInt()

inline fun Long.Companion.fromLowHigh(low: Int, high: Int): Long {
    return (low.toLong() and 0xFFFFFFFFL) or (high.toLong() shl 32)
}

inline fun Int.fastForEachOneBits(block: (Int) -> Unit) {
    var value = this
    var index = 0
    while (value != 0) {
        val shift = value.countTrailingZeroBits()
        index += shift
        if (index < 32) block(index)
        value = value ushr (shift + 1)
        index++
    }
}