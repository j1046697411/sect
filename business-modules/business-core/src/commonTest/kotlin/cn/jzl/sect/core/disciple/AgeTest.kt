package cn.jzl.sect.core.disciple

import kotlin.test.Test
import kotlin.test.assertEquals

class AgeTest {

    @Test
    fun `默认构造函数应创建具有默认值的Age`() {
        // Given & When
        val age = Age()

        // Then
        assertEquals(18, age.age)
    }

    @Test
    fun `自定义构造函数应创建具有指定值的Age`() {
        // Given & When
        val age = Age(age = 25)

        // Then
        assertEquals(25, age.age)
    }
}
