package cn.jzl.sect.core.common

import kotlin.test.Test
import kotlin.test.assertEquals

class LevelTest {

    @Test
    fun `默认构造函数应创建等级为1的Level`() {
        // Given & When
        val level = Level()

        // Then
        assertEquals(1, level.value)
    }

    @Test
    fun `自定义构造函数应创建具有指定值的Level`() {
        // Given & When
        val level = Level(value = 5)

        // Then
        assertEquals(5, level.value)
    }
}
