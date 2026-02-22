package cn.jzl.sect.core.combat

import kotlin.test.Test
import kotlin.test.assertEquals

class CombatAttributeTest {

    @Test
    fun `默认构造函数应创建具有默认值的CombatAttribute`() {
        // Given & When
        val attr = CombatAttribute()

        // Then
        assertEquals(20, attr.strength)
        assertEquals(20, attr.agility)
        assertEquals(20, attr.intelligence)
        assertEquals(20, attr.endurance)
    }

    @Test
    fun `自定义构造函数应创建具有指定值的CombatAttribute`() {
        // Given & When
        val attr = CombatAttribute(
            strength = 50,
            agility = 40,
            intelligence = 60,
            endurance = 70
        )

        // Then
        assertEquals(50, attr.strength)
        assertEquals(40, attr.agility)
        assertEquals(60, attr.intelligence)
        assertEquals(70, attr.endurance)
    }

    @Test
    fun `copy方法应创建具有修改值的副本`() {
        // Given
        val original = CombatAttribute()

        // When
        val copy = original.copy(strength = 100)

        // Then
        assertEquals(100, copy.strength)
        assertEquals(20, copy.agility)
        assertEquals(20, copy.intelligence)
        assertEquals(20, copy.endurance)
    }
}
