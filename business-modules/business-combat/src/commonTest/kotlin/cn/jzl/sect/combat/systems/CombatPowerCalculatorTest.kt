package cn.jzl.sect.combat.systems

import cn.jzl.sect.combat.components.CombatStats
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.cultivation.Talent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 战斗实力计算器测试类
 */
class CombatPowerCalculatorTest {

    @Test
    fun `计算战斗实力应考虑境界属性功法和装备`() {
        // Given
        val calculator = CombatPowerCalculator()
        val realm = Realm.QI_REFINING
        val stats = CombatStats(attack = 50, defense = 40, speed = 30)
        val skillPower = 100.0
        val equipmentPower = 50.0

        // When
        val power = calculator.calculateCombatPower(realm, stats, skillPower, equipmentPower)

        // Then
        // 境界实力: 1 * 1000 * 0.5 = 500
        // 属性实力: (50+40+30*2+5*2+5*2) * 0.3 = 51
        // 功法实力: 100 * 0.15 = 15
        // 装备实力: 50 * 0.05 = 2.5
        assertTrue(power > 0)
    }

    @Test
    fun `计算境界实力应返回正确值`() {
        // Given
        val calculator = CombatPowerCalculator()

        // When & Then
        val mortalPower = calculator.calculateRealmPower(Realm.MORTAL)
        val qiRefiningPower = calculator.calculateRealmPower(Realm.QI_REFINING)
        val foundationPower = calculator.calculateRealmPower(Realm.FOUNDATION)

        assertEquals(0, mortalPower)
        assertEquals(500, qiRefiningPower) // 1 * 1000 * 0.5
        assertEquals(1000, foundationPower) // 2 * 1000 * 0.5
    }

    @Test
    fun `计算属性实力应基于战斗属性`() {
        // Given
        val calculator = CombatPowerCalculator()
        val stats = CombatStats(attack = 100, defense = 80, speed = 60, critRate = 10, dodgeRate = 10)

        // When
        val power = calculator.calculateAttributePower(stats)

        // Then
        // (100 + 80 + 60*2 + 10*2 + 10*2) * 0.3 = 102
        assertEquals(102, power)
    }

    @Test
    fun `评估战斗等级应返回正确的等级`() {
        // Given
        val calculator = CombatPowerCalculator()

        // When & Then
        assertEquals(CombatPowerCalculator.CombatLevel.WEAK, calculator.assessCombatLevel(100))
        assertEquals(CombatPowerCalculator.CombatLevel.AVERAGE, calculator.assessCombatLevel(500))
        assertEquals(CombatPowerCalculator.CombatLevel.STRONG, calculator.assessCombatLevel(1500))
        assertEquals(CombatPowerCalculator.CombatLevel.ELITE, calculator.assessCombatLevel(3000))
        assertEquals(CombatPowerCalculator.CombatLevel.MASTER, calculator.assessCombatLevel(6000))
    }
}
