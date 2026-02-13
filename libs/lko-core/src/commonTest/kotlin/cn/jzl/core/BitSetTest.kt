package cn.jzl.core

import cn.jzl.core.bits.BitSet
import cn.jzl.core.list.LongFastList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BitSetTest {

    @Test
    fun testEmptyBitSet() {
        val bitSet = BitSet(LongFastList())
        assertTrue(bitSet.isEmpty())
        assertEquals(0, bitSet.size)
    }

    @Test
    fun testSetBit() {
        val bitSet = BitSet(LongFastList())
        
        bitSet[0] = true
        bitSet[5] = true
        bitSet[10] = true
        
        assertTrue(bitSet[0])
        assertTrue(bitSet[5])
        assertTrue(bitSet[10])
        assertFalse(bitSet[1])
        assertFalse(bitSet[6])
    }

    @Test
    fun testClearBit() {
        val bitSet = BitSet(LongFastList())
        
        bitSet[0] = true
        assertTrue(bitSet[0])
        
        bitSet[0] = false
        assertFalse(bitSet[0])
    }

    @Test
    fun testCountOneBits() {
        val bitSet = BitSet(LongFastList())
        
        bitSet[0] = true
        bitSet[5] = true
        bitSet[10] = true
        bitSet[64] = true  // First bit of second long
        bitSet[70] = true
        
        assertEquals(5, bitSet.countOneBits())
    }

    @Test
    fun testClear() {
        val bitSet = BitSet(LongFastList())
        
        bitSet[0] = true
        bitSet[5] = true
        
        bitSet.clear()
        
        assertTrue(bitSet.isEmpty())
        assertFalse(bitSet[0])
        assertFalse(bitSet[5])
    }

    @Test
    fun testIntersects() {
        val bitSet1 = BitSet(LongFastList())
        val bitSet2 = BitSet(LongFastList())
        
        bitSet1[0] = true
        bitSet1[5] = true
        bitSet1[10] = true
        
        bitSet2[5] = true  // Common bit
        bitSet2[20] = true
        
        assertTrue(bitSet1.intersects(bitSet2))
    }

    @Test
    fun testIntersectsNoIntersection() {
        val bitSet1 = BitSet(LongFastList())
        val bitSet2 = BitSet(LongFastList())
        
        bitSet1[0] = true
        bitSet1[5] = true
        
        bitSet2[10] = true
        bitSet2[20] = true
        
        assertFalse(bitSet1.intersects(bitSet2))
    }

    @Test
    fun testContains() {
        val bitSet1 = BitSet(LongFastList())
        val bitSet2 = BitSet(LongFastList())
        
        bitSet1[0] = true
        bitSet1[5] = true
        bitSet1[10] = true
        
        bitSet2[5] = true  // Subset
        
        assertTrue(bitSet1.contains(bitSet2))
    }

    @Test
    fun testContainsNotContained() {
        val bitSet1 = BitSet(LongFastList())
        val bitSet2 = BitSet(LongFastList())
        
        bitSet1[0] = true
        bitSet1[5] = true
        
        bitSet2[0] = true
        bitSet2[5] = true
        bitSet2[10] = true  // Not in bitSet1
        
        assertFalse(bitSet1.contains(bitSet2))
    }

    @Test
    fun testOr() {
        val bitSet = BitSet(LongFastList())
        
        bitSet[0] = true
        bitSet[5] = true
        
        val other = BitSet(LongFastList())
        other[5] = true
        other[10] = true
        
        bitSet.or(other)
        
        assertTrue(bitSet[0])
        assertTrue(bitSet[5])
        assertTrue(bitSet[10])
    }

    @Test
    fun testAnd() {
        val bitSet = BitSet(LongFastList())
        
        bitSet[0] = true
        bitSet[5] = true
        bitSet[10] = true
        
        val other = BitSet(LongFastList())
        other[5] = true
        other[10] = true
        other[20] = true
        
        bitSet.and(other)
        
        assertFalse(bitSet[0])
        assertTrue(bitSet[5])
        assertTrue(bitSet[10])
        assertFalse(bitSet[20])
    }

    @Test
    fun testAndNot() {
        val bitSet = BitSet(LongFastList())
        
        bitSet[0] = true
        bitSet[5] = true
        bitSet[10] = true
        
        val other = BitSet(LongFastList())
        other[5] = true  // This should be removed
        
        bitSet.andNot(other)
        
        assertTrue(bitSet[0])
        assertFalse(bitSet[5])
        assertTrue(bitSet[10])
    }

    @Test
    fun testNot() {
        val bitSet = BitSet(LongFastList())
        
        bitSet[0] = true
        bitSet[5] = true
        
        bitSet.not()
        
        assertFalse(bitSet[0])
        assertFalse(bitSet[5])
        assertTrue(bitSet[1])
        assertTrue(bitSet[10])
    }

    @Test
    fun testXor() {
        val bitSet = BitSet(LongFastList())
        
        bitSet[0] = true
        bitSet[5] = true
        
        val other = BitSet(LongFastList())
        other[5] = true
        other[10] = true
        
        bitSet.xor(other)
        
        assertTrue(bitSet[0])   // Only in bitSet
        assertFalse(bitSet[5])  // In both, so XOR gives false
        assertTrue(bitSet[10])  // Only in other
    }

    @Test
    fun testIsNotEmpty() {
        val bitSet = BitSet(LongFastList())
        
        assertTrue(bitSet.isEmpty())
        assertFalse(bitSet.isNotEmpty())
        
        bitSet[0] = true
        
        assertFalse(bitSet.isEmpty())
        assertTrue(bitSet.isNotEmpty())
    }

    @Test
    fun testSizeCalculation() {
        val bitSet = BitSet(LongFastList())
        
        bitSet[0] = true
        bitSet[63] = true  // Last bit of first long
        
        assertEquals(64, bitSet.size)
        
        bitSet[64] = true  // First bit of second long
        assertEquals(65, bitSet.size)
    }

    @Test
    fun testSequence() {
        val bitSet = BitSet(LongFastList())
        
        bitSet[0] = true
        bitSet[5] = true
        bitSet[10] = true
        
        val elements = bitSet.toList()
        
        assertEquals(3, elements.size)
        assertTrue(elements.contains(0))
        assertTrue(elements.contains(5))
        assertTrue(elements.contains(10))
    }
}
