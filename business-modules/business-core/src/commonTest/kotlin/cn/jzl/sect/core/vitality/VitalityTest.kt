package cn.jzl.sect.core.vitality

import kotlin.test.Test
import kotlin.test.assertEquals

class VitalityTest {

    @Test
    fun `默认构造函数应创建具有默认值的Vitality`() {
        // Given & When
        val vitality = Vitality()

        // Then
        assertEquals(100, vitality.currentHealth)
        assertEquals(100, vitality.maxHealth)
    }

    @Test
    fun `自定义构造函数应创建具有指定值的Vitality`() {
        // Given & When
        val vitality = Vitality(currentHealth = 80, maxHealth = 120)

        // Then
        assertEquals(80, vitality.currentHealth)
        assertEquals(120, vitality.maxHealth)
    }
}
