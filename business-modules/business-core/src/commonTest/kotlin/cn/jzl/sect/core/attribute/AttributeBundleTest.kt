package cn.jzl.sect.core.attribute

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * AttributeBundle 组件测试类
 */
class AttributeBundleTest {

    @Test
    fun `默认构造函数应创建具有默认值的AttributeBundle`() {
        // Given & When
        val attr = AttributeBundle()

        // Then - 基础属性
        assertEquals(1, attr.realmLevel)
        assertEquals(100, attr.health)
        assertEquals(100, attr.spirit)

        // Then - 资质属性
        assertEquals(10, attr.physique)
        assertEquals(10, attr.comprehension)
        assertEquals(10, attr.fortune)
        assertEquals(10, attr.mental)
        assertEquals(10, attr.consciousness)

        // Then - 战斗属性
        assertEquals(10, attr.attack)
        assertEquals(10, attr.defense)
        assertEquals(10, attr.speed)
        assertEquals(5, attr.critRate)
        assertEquals(5, attr.dodgeRate)
        assertEquals(0, attr.penetration)

        // Then - 生活属性
        assertEquals(10, attr.charm)
        assertEquals(10, attr.leadership)
        assertEquals(0, attr.alchemy)
        assertEquals(0, attr.forging)
        assertEquals(0, attr.planting)
        assertEquals(0, attr.medicine)
    }

    @Test
    fun `自定义构造函数应创建具有指定值的AttributeBundle`() {
        // Given & When
        val attr = AttributeBundle(
            realmLevel = 5,
            health = 500,
            spirit = 300,
            physique = 20,
            comprehension = 25,
            fortune = 30,
            mental = 15,
            consciousness = 20,
            attack = 100,
            defense = 80,
            speed = 50,
            critRate = 15,
            dodgeRate = 10,
            penetration = 20,
            charm = 25,
            leadership = 30,
            alchemy = 40,
            forging = 35,
            planting = 20,
            medicine = 25
        )

        // Then - 基础属性
        assertEquals(5, attr.realmLevel)
        assertEquals(500, attr.health)
        assertEquals(300, attr.spirit)

        // Then - 资质属性
        assertEquals(20, attr.physique)
        assertEquals(25, attr.comprehension)
        assertEquals(30, attr.fortune)
        assertEquals(15, attr.mental)
        assertEquals(20, attr.consciousness)

        // Then - 战斗属性
        assertEquals(100, attr.attack)
        assertEquals(80, attr.defense)
        assertEquals(50, attr.speed)
        assertEquals(15, attr.critRate)
        assertEquals(10, attr.dodgeRate)
        assertEquals(20, attr.penetration)

        // Then - 生活属性
        assertEquals(25, attr.charm)
        assertEquals(30, attr.leadership)
        assertEquals(40, attr.alchemy)
        assertEquals(35, attr.forging)
        assertEquals(20, attr.planting)
        assertEquals(25, attr.medicine)
    }

    @Test
    fun `copy方法应创建具有修改值的副本`() {
        // Given
        val original = AttributeBundle()

        // When
        val copy = original.copy(health = 200, attack = 50)

        // Then
        assertEquals(200, copy.health)
        assertEquals(50, copy.attack)
        assertEquals(100, original.health)
        assertEquals(10, original.attack)
    }

    @Test
    fun `getFinalHealth应返回基础值加加成值`() {
        // Given
        val attr = AttributeBundle(health = 100, healthBonus = 50)

        // When & Then
        assertEquals(150, attr.getFinalHealth())
    }

    @Test
    fun `getFinalSpirit应返回基础值加加成值`() {
        // Given
        val attr = AttributeBundle(spirit = 200, spiritBonus = 100)

        // When & Then
        assertEquals(300, attr.getFinalSpirit())
    }

    @Test
    fun `getFinalAttack应返回基础值加加成值`() {
        // Given
        val attr = AttributeBundle(attack = 50, attackBonus = 25)

        // When & Then
        assertEquals(75, attr.getFinalAttack())
    }

    @Test
    fun `getFinalDefense应返回基础值加加成值`() {
        // Given
        val attr = AttributeBundle(defense = 40, defenseBonus = 20)

        // When & Then
        assertEquals(60, attr.getFinalDefense())
    }

    @Test
    fun `getFinalSpeed应返回基础值加加成值`() {
        // Given
        val attr = AttributeBundle(speed = 30, speedBonus = 15)

        // When & Then
        assertEquals(45, attr.getFinalSpeed())
    }

    @Test
    fun `getFinalCritRate应返回基础值加加成值`() {
        // Given
        val attr = AttributeBundle(critRate = 10, critRateBonus = 5)

        // When & Then
        assertEquals(15, attr.getFinalCritRate())
    }

    @Test
    fun `getFinalDodgeRate应返回基础值加加成值`() {
        // Given
        val attr = AttributeBundle(dodgeRate = 8, dodgeRateBonus = 4)

        // When & Then
        assertEquals(12, attr.getFinalDodgeRate())
    }

    @Test
    fun `getFinalPenetration应返回基础值加加成值`() {
        // Given
        val attr = AttributeBundle(penetration = 10, penetrationBonus = 5)

        // When & Then
        assertEquals(15, attr.getFinalPenetration())
    }
}