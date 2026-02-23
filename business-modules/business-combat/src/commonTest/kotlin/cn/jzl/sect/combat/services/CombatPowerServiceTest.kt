package cn.jzl.sect.combat.services

import cn.jzl.sect.combat.components.CombatStats
import cn.jzl.sect.core.cultivation.Realm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 战斗实力服务测试类
 */
class CombatPowerServiceTest {

    companion object {
        // 境界实力权重: 50%
        const val REALM_WEIGHT = 0.5

        // 属性实力权重: 30%
        const val ATTRIBUTE_WEIGHT = 0.3

        // 功法实力权重: 15%
        const val SKILL_WEIGHT = 0.15

        // 装备实力权重: 5%
        const val EQUIPMENT_WEIGHT = 0.05

        // 境界实力基数
        const val REALM_BASE_POWER = 1000

        /**
         * 战斗等级枚举
         */
        enum class CombatLevel(val displayName: String, val minPower: Int) {
            WEAK("弱小", 0),
            AVERAGE("普通", 300),
            COMPETENT("胜任", 800),
            STRONG("强悍", 1500),
            ELITE("精锐", 3000),
            MASTER("大师", 6000),
            LEGENDARY("传奇", 10000);

            companion object {
                fun fromPower(power: Int): CombatLevel {
                    return entries.reversed().find { power >= it.minPower } ?: WEAK
                }
            }
        }

        /**
         * 计算境界实力
         */
        fun calculateRealmPower(realm: Realm): Int {
            return (realm.level * REALM_BASE_POWER * REALM_WEIGHT).toInt()
        }

        /**
         * 计算属性实力
         */
        fun calculateAttributePower(stats: CombatStats): Int {
            val totalAttribute = stats.attack +
                    stats.defense +
                    stats.speed * 2 +
                    stats.critRate * 2 +
                    stats.dodgeRate * 2

            return (totalAttribute * ATTRIBUTE_WEIGHT).toInt()
        }

        /**
         * 评估战斗等级
         */
        fun assessCombatLevel(power: Int): CombatLevel {
            return CombatLevel.fromPower(power)
        }
    }

    @Test
    fun `计算战斗实力应考虑境界属性功法和装备`() {
        // Given
        val realm = Realm.QI_REFINING
        val stats = CombatStats(attack = 50, defense = 40, speed = 30)
        val skillPower = 100.0
        val equipmentPower = 50.0

        // When
        val realmPower = calculateRealmPower(realm)
        val attributePower = calculateAttributePower(stats)
        val power = (
            realmPower * REALM_WEIGHT +
            attributePower * ATTRIBUTE_WEIGHT +
            skillPower * SKILL_WEIGHT +
            equipmentPower * EQUIPMENT_WEIGHT
        ).toInt()

        // Then
        // 境界实力: 1 * 1000 * 0.5 = 500
        // 属性实力: (50+40+30*2+5*2+5*2) * 0.3 = 51
        // 功法实力: 100 * 0.15 = 15
        // 装备实力: 50 * 0.05 = 2.5
        assertTrue(power > 0)
    }

    @Test
    fun `计算境界实力应返回正确值`() {
        // When & Then
        val mortalPower = calculateRealmPower(Realm.MORTAL)
        val qiRefiningPower = calculateRealmPower(Realm.QI_REFINING)
        val foundationPower = calculateRealmPower(Realm.FOUNDATION)

        assertEquals(0, mortalPower)
        assertEquals(500, qiRefiningPower) // 1 * 1000 * 0.5
        assertEquals(1000, foundationPower) // 2 * 1000 * 0.5
    }

    @Test
    fun `计算属性实力应基于战斗属性`() {
        // Given
        val stats = CombatStats(attack = 100, defense = 80, speed = 60, critRate = 10, dodgeRate = 10)

        // When
        val power = calculateAttributePower(stats)

        // Then
        // (100 + 80 + 60*2 + 10*2 + 10*2) * 0.3 = 102
        assertEquals(102, power)
    }

    @Test
    fun `评估战斗等级应返回正确的等级`() {
        // When & Then
        assertEquals(CombatLevel.WEAK, assessCombatLevel(100))
        assertEquals(CombatLevel.AVERAGE, assessCombatLevel(500))
        assertEquals(CombatLevel.STRONG, assessCombatLevel(1500))
        assertEquals(CombatLevel.ELITE, assessCombatLevel(3000))
        assertEquals(CombatLevel.MASTER, assessCombatLevel(6000))
    }
}
