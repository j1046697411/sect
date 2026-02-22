package cn.jzl.sect.core.cultivation

import kotlin.test.Test
import kotlin.test.assertEquals

class TalentTest {

    @Test
    fun `默认构造函数应创建具有默认值的Talent`() {
        // Given & When
        val talent = Talent()

        // Then
        // 资质属性
        assertEquals(50, talent.physique)
        assertEquals(50, talent.comprehension)
        assertEquals(50, talent.fortune)
        assertEquals(50, talent.mental)
        // 战斗属性
        assertEquals(50, talent.strength)
        assertEquals(50, talent.agility)
        assertEquals(50, talent.intelligence)
        assertEquals(50, talent.endurance)
        // 生活属性
        assertEquals(50, talent.charm)
        assertEquals(50, talent.alchemyTalent)
        assertEquals(50, talent.forgingTalent)
    }

    @Test
    fun `自定义构造函数应创建具有指定值的Talent`() {
        // Given & When
        val talent = Talent(
            physique = 80,
            comprehension = 90,
            fortune = 70,
            mental = 85,
            strength = 75,
            agility = 88,
            intelligence = 92,
            endurance = 78,
            charm = 60,
            alchemyTalent = 95,
            forgingTalent = 65
        )

        // Then
        assertEquals(80, talent.physique)
        assertEquals(90, talent.comprehension)
        assertEquals(70, talent.fortune)
        assertEquals(85, talent.mental)
        assertEquals(75, talent.strength)
        assertEquals(88, talent.agility)
        assertEquals(92, talent.intelligence)
        assertEquals(78, talent.endurance)
        assertEquals(60, talent.charm)
        assertEquals(95, talent.alchemyTalent)
        assertEquals(65, talent.forgingTalent)
    }

    @Test
    fun `copy方法应创建具有修改值的副本`() {
        // Given
        val original = Talent()

        // When
        val copy = original.copy(physique = 100)

        // Then
        assertEquals(100, copy.physique)
        assertEquals(50, copy.comprehension)
        assertEquals(50, copy.fortune)
        assertEquals(50, copy.mental)
    }

    @Test
    fun `边界值测试-属性最小值0`() {
        // Given & When
        val talent = Talent(
            physique = 0,
            comprehension = 0,
            fortune = 0,
            mental = 0,
            strength = 0,
            agility = 0,
            intelligence = 0,
            endurance = 0,
            charm = 0,
            alchemyTalent = 0,
            forgingTalent = 0
        )

        // Then
        assertEquals(0, talent.physique)
        assertEquals(0, talent.comprehension)
        assertEquals(0, talent.fortune)
        assertEquals(0, talent.mental)
        assertEquals(0, talent.strength)
        assertEquals(0, talent.agility)
        assertEquals(0, talent.intelligence)
        assertEquals(0, talent.endurance)
        assertEquals(0, talent.charm)
        assertEquals(0, talent.alchemyTalent)
        assertEquals(0, talent.forgingTalent)
    }

    @Test
    fun `边界值测试-属性最大值100`() {
        // Given & When
        val talent = Talent(
            physique = 100,
            comprehension = 100,
            fortune = 100,
            mental = 100,
            strength = 100,
            agility = 100,
            intelligence = 100,
            endurance = 100,
            charm = 100,
            alchemyTalent = 100,
            forgingTalent = 100
        )

        // Then
        assertEquals(100, talent.physique)
        assertEquals(100, talent.comprehension)
        assertEquals(100, talent.fortune)
        assertEquals(100, talent.mental)
        assertEquals(100, talent.strength)
        assertEquals(100, talent.agility)
        assertEquals(100, talent.intelligence)
        assertEquals(100, talent.endurance)
        assertEquals(100, talent.charm)
        assertEquals(100, talent.alchemyTalent)
        assertEquals(100, talent.forgingTalent)
    }

    @Test
    fun `getTotalAptitudeScore应返回资质属性总和`() {
        // Given
        val talent = Talent(
            physique = 80,
            comprehension = 90,
            fortune = 70,
            mental = 60
        )

        // When
        val score = talent.getTotalAptitudeScore()

        // Then
        assertEquals(300, score)
    }

    @Test
    fun `getTotalAptitudeScore默认值应为200`() {
        // Given
        val talent = Talent()

        // When
        val score = talent.getTotalAptitudeScore()

        // Then
        assertEquals(200, score)
    }

    @Test
    fun `getAverageAptitude应返回资质属性平均值`() {
        // Given
        val talent = Talent(
            physique = 80,
            comprehension = 90,
            fortune = 70,
            mental = 60
        )

        // When
        val average = talent.getAverageAptitude()

        // Then
        assertEquals(75.0, average)
    }

    @Test
    fun `getAverageAptitude默认值应为50`() {
        // Given
        val talent = Talent()

        // When
        val average = talent.getAverageAptitude()

        // Then
        assertEquals(50.0, average)
    }

    @Test
    fun `getCombatPower应返回战斗属性总和`() {
        // Given
        val talent = Talent(
            strength = 80,
            agility = 90,
            intelligence = 70,
            endurance = 60
        )

        // When
        val power = talent.getCombatPower()

        // Then
        assertEquals(300, power)
    }

    @Test
    fun `getCombatPower默认值应为200`() {
        // Given
        val talent = Talent()

        // When
        val power = talent.getCombatPower()

        // Then
        assertEquals(200, power)
    }

    @Test
    fun `getLifeSkillScore应返回生活属性总和`() {
        // Given
        val talent = Talent(
            charm = 80,
            alchemyTalent = 90,
            forgingTalent = 70
        )

        // When
        val score = talent.getLifeSkillScore()

        // Then
        assertEquals(240, score)
    }

    @Test
    fun `getLifeSkillScore默认值应为150`() {
        // Given
        val talent = Talent()

        // When
        val score = talent.getLifeSkillScore()

        // Then
        assertEquals(150, score)
    }

    @Test
    fun `getOverallScore应返回所有属性总和`() {
        // Given
        val talent = Talent(
            physique = 80,
            comprehension = 90,
            fortune = 70,
            mental = 60,
            strength = 75,
            agility = 85,
            intelligence = 95,
            endurance = 65,
            charm = 88,
            alchemyTalent = 92,
            forgingTalent = 78
        )

        // When
        val score = talent.getOverallScore()

        // Then
        assertEquals(878, score)
    }

    @Test
    fun `getOverallScore默认值应为550`() {
        // Given
        val talent = Talent()

        // When
        val score = talent.getOverallScore()

        // Then
        assertEquals(550, score)
    }

    @Test
    fun `getAptitudeLevel资质评级-天才`() {
        // Given
        val talent = Talent(
            physique = 90,
            comprehension = 95,
            fortune = 88,
            mental = 92
        )

        // When & Then
        assertEquals(AptitudeLevel.GENIUS, talent.getAptitudeLevel())
    }

    @Test
    fun `getAptitudeLevel资质评级-优秀`() {
        // Given
        val talent = Talent(
            physique = 75,
            comprehension = 80,
            fortune = 78,
            mental = 76
        )

        // When & Then
        assertEquals(AptitudeLevel.EXCELLENT, talent.getAptitudeLevel())
    }

    @Test
    fun `getAptitudeLevel资质评级-良好`() {
        // Given - 平均值在[40, 60)区间
        val talent = Talent(
            physique = 45,
            comprehension = 48,
            fortune = 42,
            mental = 46
        )

        // When & Then - (45+48+42+46)/4 = 45.25，属于良好
        assertEquals(AptitudeLevel.GOOD, talent.getAptitudeLevel())
    }

    @Test
    fun `getAptitudeLevel资质评级-普通`() {
        // Given - 平均值在[20, 40)区间
        val talent = Talent(
            physique = 25,
            comprehension = 28,
            fortune = 22,
            mental = 26
        )

        // When & Then - (25+28+22+26)/4 = 25.25，属于普通
        assertEquals(AptitudeLevel.AVERAGE, talent.getAptitudeLevel())
    }

    @Test
    fun `getAptitudeLevel资质评级-平庸`() {
        // Given - 平均值<20
        val talent = Talent(
            physique = 15,
            comprehension = 18,
            fortune = 12,
            mental = 16
        )

        // When & Then - (15+18+12+16)/4 = 15.25，属于平庸
        assertEquals(AptitudeLevel.POOR, talent.getAptitudeLevel())
    }
}
