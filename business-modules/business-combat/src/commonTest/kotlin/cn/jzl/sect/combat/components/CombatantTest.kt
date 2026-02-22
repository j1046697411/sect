package cn.jzl.sect.combat.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 战斗参与者组件测试类
 */
class CombatantTest {

    @Test
    fun `默认构造函数应创建具有默认值的战斗参与者`() {
        // Given & When
        val combatant = Combatant()

        // Then
        assertEquals(0L, combatant.entityId)
        assertEquals(100, combatant.currentHp)
        assertEquals(100, combatant.maxHp)
        assertEquals(false, combatant.isAlive)
        assertEquals(CombatActionType.ATTACK, combatant.currentAction)
    }

    @Test
    fun `自定义构造函数应创建具有指定值的战斗参与者`() {
        // Given & When
        val combatant = Combatant(
            entityId = 1L,
            currentHp = 80,
            maxHp = 100,
            isAlive = true,
            currentAction = CombatActionType.DEFEND
        )

        // Then
        assertEquals(1L, combatant.entityId)
        assertEquals(80, combatant.currentHp)
        assertEquals(100, combatant.maxHp)
        assertEquals(true, combatant.isAlive)
        assertEquals(CombatActionType.DEFEND, combatant.currentAction)
    }

    @Test
    fun `takeDamage应减少生命值`() {
        // Given
        val combatant = Combatant(currentHp = 100, maxHp = 100, isAlive = true)

        // When
        val damaged = combatant.takeDamage(30)

        // Then
        assertEquals(70, damaged.currentHp)
        assertEquals(100, combatant.currentHp) // 原对象不变
    }

    @Test
    fun `takeDamage不应低于0`() {
        // Given
        val combatant = Combatant(currentHp = 20, maxHp = 100, isAlive = true)

        // When
        val damaged = combatant.takeDamage(50)

        // Then
        assertEquals(0, damaged.currentHp)
        assertEquals(false, damaged.isAlive)
    }

    @Test
    fun `heal应恢复生命值`() {
        // Given
        val combatant = Combatant(currentHp = 50, maxHp = 100, isAlive = true)

        // When
        val healed = combatant.heal(30)

        // Then
        assertEquals(80, healed.currentHp)
    }

    @Test
    fun `heal不应超过最大生命值`() {
        // Given
        val combatant = Combatant(currentHp = 80, maxHp = 100, isAlive = true)

        // When
        val healed = combatant.heal(30)

        // Then
        assertEquals(100, healed.currentHp)
    }

    @Test
    fun `getHpPercentage应返回正确的生命值百分比`() {
        // Given
        val combatant = Combatant(currentHp = 50, maxHp = 100)

        // When & Then
        assertEquals(0.5, combatant.getHpPercentage(), 0.01)
    }

    @Test
    fun `isCriticalHp应返回true当生命值低于20_时`() {
        // Given
        val critical = Combatant(currentHp = 15, maxHp = 100)
        val normal = Combatant(currentHp = 30, maxHp = 100)

        // When & Then
        assertTrue(critical.isCriticalHp())
        assertFalse(normal.isCriticalHp())
    }
}
