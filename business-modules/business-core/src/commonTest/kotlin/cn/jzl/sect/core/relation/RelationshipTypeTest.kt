package cn.jzl.sect.core.relation

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 关系类型枚举测试类
 */
class RelationshipTypeTest {

    @Test
    fun `关系类型应包含6种类型`() {
        // Given & When
        val types = RelationshipType.entries

        // Then
        assertEquals(6, types.size)
    }

    @Test
    fun `MASTER_APPRENTICE应表示师徒关系`() {
        // Given & When & Then
        assertEquals("师徒", RelationshipType.MASTER_APPRENTICE.displayName)
    }

    @Test
    fun `PEER应表示同门关系`() {
        // Given & When & Then
        assertEquals("同门", RelationshipType.PEER.displayName)
    }

    @Test
    fun `COMPETITOR应表示竞争关系`() {
        // Given & When & Then
        assertEquals("竞争", RelationshipType.COMPETITOR.displayName)
    }

    @Test
    fun `COOPERATOR应表示合作关系`() {
        // Given & When & Then
        assertEquals("合作", RelationshipType.COOPERATOR.displayName)
    }

    @Test
    fun `FRIENDLY应表示友好关系`() {
        // Given & When & Then
        assertEquals("友好", RelationshipType.FRIENDLY.displayName)
    }

    @Test
    fun `HOSTILE应表示敌对关系`() {
        // Given & When & Then
        assertEquals("敌对", RelationshipType.HOSTILE.displayName)
    }
}
