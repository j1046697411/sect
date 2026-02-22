package cn.jzl.sect.core.vitality

import kotlin.test.Test
import kotlin.test.assertEquals

class SpiritTest {

    @Test
    fun `默认构造函数应创建具有默认值的Spirit`() {
        // Given & When
        val spirit = Spirit()

        // Then
        assertEquals(50, spirit.currentSpirit)
        assertEquals(50, spirit.maxSpirit)
    }

    @Test
    fun `自定义构造函数应创建具有指定值的Spirit`() {
        // Given & When
        val spirit = Spirit(currentSpirit = 30, maxSpirit = 80)

        // Then
        assertEquals(30, spirit.currentSpirit)
        assertEquals(80, spirit.maxSpirit)
    }
}
