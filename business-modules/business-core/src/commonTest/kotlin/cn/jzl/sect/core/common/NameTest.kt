package cn.jzl.sect.core.common

import kotlin.test.Test
import kotlin.test.assertEquals

class NameTest {

    @Test
    fun `Name应存储名称值`() {
        // Given & When
        val name = Name(value = "张三")

        // Then
        assertEquals("张三", name.value)
    }
}
