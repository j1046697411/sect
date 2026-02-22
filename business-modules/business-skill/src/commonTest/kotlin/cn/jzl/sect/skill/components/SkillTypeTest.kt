package cn.jzl.sect.skill.components

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 功法类型枚举测试类
 */
class SkillTypeTest {

    @Test
    fun `功法类型应包含8种类型`() {
        // Given & When
        val types = SkillType.entries

        // Then
        assertEquals(8, types.size)
    }

    @Test
    fun `CULTIVATION应表示修炼功法`() {
        // Given & When & Then
        assertEquals("修炼", SkillType.CULTIVATION.displayName)
    }

    @Test
    fun `COMBAT应表示战斗功法`() {
        // Given & When & Then
        assertEquals("战斗", SkillType.COMBAT.displayName)
    }

    @Test
    fun `MOVEMENT应表示身法`() {
        // Given & When & Then
        assertEquals("身法", SkillType.MOVEMENT.displayName)
    }

    @Test
    fun `ALCHEMY应表示炼丹功法`() {
        // Given & When & Then
        assertEquals("炼丹", SkillType.ALCHEMY.displayName)
    }

    @Test
    fun `FORGING应表示炼器功法`() {
        // Given & When & Then
        assertEquals("炼器", SkillType.FORGING.displayName)
    }

    @Test
    fun `FORMATION应表示阵法`() {
        // Given & When & Then
        assertEquals("阵法", SkillType.FORMATION.displayName)
    }

    @Test
    fun `SPIRITUAL应表示神识功法`() {
        // Given & When & Then
        assertEquals("神识", SkillType.SPIRITUAL.displayName)
    }

    @Test
    fun `SUPPORT应表示辅助功法`() {
        // Given & When & Then
        assertEquals("辅助", SkillType.SUPPORT.displayName)
    }
}
