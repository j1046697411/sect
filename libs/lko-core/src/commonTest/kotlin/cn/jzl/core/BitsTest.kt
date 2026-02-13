package cn.jzl.core

import cn.jzl.core.bits.IntMaskRange
import cn.jzl.core.bits.hasFlags
import cn.jzl.core.bits.setBits
import cn.jzl.core.bits.unsetBits
import cn.jzl.core.bits.without
import cn.jzl.core.bits.with
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class BitsTest {

    @Test
    fun testHasFlags() {
        val value = 0b1111
        assertTrue(value hasFlags(0b0001))
        assertTrue(value hasFlags(0b0011))
        assertTrue(value hasFlags(0b1111))
        assertFalse(value hasFlags(0b11111))
    }

    @Test
    fun testSetBits() {
        var value = 0b0000
        value = value.setBits(0b1111)
        assertEquals(0b1111, value)
    }

    @Test
    fun testUnsetBits() {
        var value = 0b1111
        value = value.unsetBits(0b0101)
        assertEquals(0b1010, value)
    }

    @Test
    fun testWith() {
        var value = 0b0000
        value = value.with(0b1111)
        assertEquals(0b1111, value)
    }

    @Test
    fun testWithout() {
        var value = 0b1111
        value = value.without(0b0101)
        assertEquals(0b1010, value)
    }

    @Test
    fun testIntMaskRangeFromRange() {
        val range = IntMaskRange.fromRange(8, 8)
        assertEquals(8, range.offset)
        assertEquals(8, range.size)
    }

    @Test
    fun testIntMaskRangeComponent() {
        val range = IntMaskRange.fromRange(4, 8)
        val (offset, size) = range
        assertEquals(4, offset)
        assertEquals(8, size)
    }

    @Test
    fun testIntMaskRangeToMask() {
        val range = IntMaskRange.fromRange(4, 8)
        assertEquals(0xFF shl 4, range.toMask())
    }
}
