package cn.jzl.sect.core.common

import kotlin.test.Test
import kotlin.test.assertEquals

class DescriptionTest {

    @Test
    fun `Description应存储描述文本`() {
        // Given & When
        val desc = Description(value = "这是一个描述")

        // Then
        assertEquals("这是一个描述", desc.value)
    }
}
