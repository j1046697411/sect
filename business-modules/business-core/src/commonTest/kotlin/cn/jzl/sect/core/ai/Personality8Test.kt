package cn.jzl.sect.core.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.math.abs

class Personality8Test {

    @Test
    fun testNormalized() {
        // Given
        val p = Personality8(
            diligence = 0.5,
            cautious = -0.5,
            greed = 0.2,
            loyalty = 0.0,
            ambition = 0.0,
            sociability = 0.0,
            morality = 0.0,
            patience = 0.0
        )
        // Total abs sum = 0.5 + 0.5 + 0.2 = 1.2

        // When
        val normalized = p.normalized()

        // Then
        val sum = abs(normalized.diligence) + abs(normalized.cautious) + abs(normalized.greed) +
                abs(normalized.loyalty) + abs(normalized.ambition) + abs(normalized.sociability) +
                abs(normalized.morality) + abs(normalized.patience)
        
        assertEquals(1.0, sum, 0.0001)
        assertEquals(0.5 / 1.2, normalized.diligence, 0.0001)
        assertEquals(-0.5 / 1.2, normalized.cautious, 0.0001)
        assertEquals(0.2 / 1.2, normalized.greed, 0.0001)
    }

    @Test
    fun testNormalizedZero() {
        // Given
        val p = Personality8()

        // When
        val normalized = p.normalized()

        // Then
        assertEquals(0.0, normalized.diligence)
    }

    @Test
    fun testArchetypeMiser() {
        val miser = Personality8.Miser
        assertEquals(0.8, miser.greed)
        assertEquals(-0.4, miser.morality)
        assertEquals(0.0, miser.diligence)
    }

    @Test
    fun testArchetypeAscetic() {
        val ascetic = Personality8.Ascetic
        assertEquals(0.9, ascetic.diligence)
        assertEquals(-0.8, ascetic.greed)
        assertEquals(-0.5, ascetic.sociability)
    }

    @Test
    fun testRandom() {
        val random = Personality8.random()
        val values = listOf(
            random.diligence, random.cautious, random.greed, random.loyalty,
            random.ambition, random.sociability, random.morality, random.patience
        )
        values.forEach {
            assertTrue(it >= -0.5 && it <= 0.5, "Value $it should be in range [-0.5, 0.5]")
        }
    }
}
