package cn.jzl.sect.core.cultivation

import kotlin.test.Test
import kotlin.test.assertEquals

class TalentTest {

    @Test
    fun `默认构造函数应创建具有默认值的Talent`() {
        // Given & When
        val talent = Talent()

        // Then
        assertEquals(50, talent.physique)
        assertEquals(50, talent.comprehension)
        assertEquals(50, talent.fortune)
        assertEquals(50, talent.charm)
    }

    @Test
    fun `自定义构造函数应创建具有指定值的Talent`() {
        // Given & When
        val talent = Talent(
            physique = 80,
            comprehension = 90,
            fortune = 70,
            charm = 60
        )

        // Then
        assertEquals(80, talent.physique)
        assertEquals(90, talent.comprehension)
        assertEquals(70, talent.fortune)
        assertEquals(60, talent.charm)
    }

    @Test
    fun `copy方法应创建具有修改值的副本`() {
        // Given
        val original = Talent(physique = 50, comprehension = 50, fortune = 50, charm = 50)

        // When
        val copy = original.copy(physique = 100)

        // Then
        assertEquals(100, copy.physique)
        assertEquals(50, copy.comprehension)
        assertEquals(50, copy.fortune)
        assertEquals(50, copy.charm)
    }
}
