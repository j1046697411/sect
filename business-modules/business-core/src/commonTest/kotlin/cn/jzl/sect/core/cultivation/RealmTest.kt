package cn.jzl.sect.core.cultivation

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 境界枚举测试
 */
class RealmTest {

    @Test
    fun `凡人境界应正确配置`() {
        // Given
        val realm = Realm.MORTAL

        // Then
        assertEquals(0, realm.level)
        assertEquals("凡人", realm.displayName)
    }

    @Test
    fun `炼气期境界应正确配置`() {
        // Given
        val realm = Realm.QI_REFINING

        // Then
        assertEquals(1, realm.level)
        assertEquals("炼气期", realm.displayName)
    }

    @Test
    fun `筑基期境界应正确配置`() {
        // Given
        val realm = Realm.FOUNDATION

        // Then
        assertEquals(2, realm.level)
        assertEquals("筑基期", realm.displayName)
    }

    @Test
    fun `金丹期境界应正确配置`() {
        // Given
        val realm = Realm.GOLDEN_CORE

        // Then
        assertEquals(3, realm.level)
        assertEquals("金丹期", realm.displayName)
    }

    @Test
    fun `元婴期境界应正确配置`() {
        // Given
        val realm = Realm.NASCENT_SOUL

        // Then
        assertEquals(4, realm.level)
        assertEquals("元婴期", realm.displayName)
    }

    @Test
    fun `化神期境界应正确配置`() {
        // Given
        val realm = Realm.SOUL_TRANSFORMATION

        // Then
        assertEquals(5, realm.level)
        assertEquals("化神期", realm.displayName)
    }

    @Test
    fun `渡劫期境界应正确配置`() {
        // Given
        val realm = Realm.TRIBULATION

        // Then
        assertEquals(6, realm.level)
        assertEquals("渡劫期", realm.displayName)
    }

    @Test
    fun `成仙境界应正确配置`() {
        // Given
        val realm = Realm.IMMORTAL

        // Then
        assertEquals(7, realm.level)
        assertEquals("成仙", realm.displayName)
    }

    @Test
    fun `境界等级应按顺序递增`() {
        // Given
        val realms = Realm.entries.toList()

        // Then
        for (i in realms.indices) {
            assertEquals(i, realms[i].level)
        }
    }
}
