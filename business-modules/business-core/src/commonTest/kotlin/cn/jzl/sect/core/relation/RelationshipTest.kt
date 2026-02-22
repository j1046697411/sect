package cn.jzl.sect.core.relation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 关系组件测试类
 */
class RelationshipTest {

    @Test
    fun `默认构造函数应创建具有默认值的关系`() {
        // Given & When
        val relationship = Relationship()

        // Then
        assertEquals(0L, relationship.sourceId)
        assertEquals(0L, relationship.targetId)
        assertEquals(RelationshipType.FRIENDLY, relationship.type)
        assertEquals(50, relationship.level)
        assertEquals(0L, relationship.establishedTime)
    }

    @Test
    fun `自定义构造函数应创建具有指定值的关系`() {
        // Given & When
        val relationship = Relationship(
            sourceId = 1L,
            targetId = 2L,
            type = RelationshipType.MASTER_APPRENTICE,
            level = 80,
            establishedTime = 1000L
        )

        // Then
        assertEquals(1L, relationship.sourceId)
        assertEquals(2L, relationship.targetId)
        assertEquals(RelationshipType.MASTER_APPRENTICE, relationship.type)
        assertEquals(80, relationship.level)
        assertEquals(1000L, relationship.establishedTime)
    }

    @Test
    fun `isPositive应返回true对于正面关系类型`() {
        // Given
        val friendly = Relationship(type = RelationshipType.FRIENDLY)
        val cooperative = Relationship(type = RelationshipType.COOPERATOR)
        val masterApprentice = Relationship(type = RelationshipType.MASTER_APPRENTICE)
        val peer = Relationship(type = RelationshipType.PEER)

        // When & Then
        assertTrue(friendly.isPositive())
        assertTrue(cooperative.isPositive())
        assertTrue(masterApprentice.isPositive())
        assertTrue(peer.isPositive())
    }

    @Test
    fun `isNegative应返回true对于负面关系类型`() {
        // Given
        val hostile = Relationship(type = RelationshipType.HOSTILE)
        val competitor = Relationship(type = RelationshipType.COMPETITOR)

        // When & Then
        assertTrue(hostile.isNegative())
        assertTrue(competitor.isNegative())
    }

    @Test
    fun `getEffectBonus应返回正确的加成值`() {
        // Given
        val highLevel = Relationship(level = 80)
        val mediumLevel = Relationship(level = 50)
        val lowLevel = Relationship(level = 20)

        // When & Then
        assertEquals(16, highLevel.getEffectBonus())  // 80 / 5
        assertEquals(10, mediumLevel.getEffectBonus()) // 50 / 5
        assertEquals(4, lowLevel.getEffectBonus())    // 20 / 5
    }

    @Test
    fun `improve应增加关系等级`() {
        // Given
        val relationship = Relationship(level = 50)

        // When
        val improved = relationship.improve(10)

        // Then
        assertEquals(60, improved.level)
        assertEquals(50, relationship.level) // 原对象不变
    }

    @Test
    fun `improve不应超过最大等级100`() {
        // Given
        val relationship = Relationship(level = 95)

        // When
        val improved = relationship.improve(10)

        // Then
        assertEquals(100, improved.level)
    }

    @Test
    fun `worsen应降低关系等级`() {
        // Given
        val relationship = Relationship(level = 50)

        // When
        val worsened = relationship.worsen(10)

        // Then
        assertEquals(40, worsened.level)
        assertEquals(50, relationship.level) // 原对象不变
    }

    @Test
    fun `worsen不应低于最小等级0`() {
        // Given
        val relationship = Relationship(level = 5)

        // When
        val worsened = relationship.worsen(10)

        // Then
        assertEquals(0, worsened.level)
    }
}
