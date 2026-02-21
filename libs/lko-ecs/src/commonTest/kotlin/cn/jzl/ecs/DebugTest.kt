package cn.jzl.ecs

import cn.jzl.ecs.entity.*
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 调试测试 - 用于重现组件移除问题
 */
class DebugTest : EntityRelationContext {

    override lateinit var world: World

    @Test
    fun testComponentRemoval() {
        // Given
        world = world { }

        // 创建测试组件
        data class Position(val x: Int, val y: Int)
        data class Velocity(val vx: Int, val vy: Int)

        // 创建实体并添加组件
        val entity = world.entity {
            it.addComponent(Position(0, 0))
            it.addComponent(Velocity(1, 1))
        }

        // 验证实体有组件
        assertTrue(entity.hasComponent<Position>(), "Entity should have Position")
        assertTrue(entity.hasComponent<Velocity>(), "Entity should have Velocity")

        // When - 移除 Velocity 组件
        entity.editor {
            it.removeComponent<Velocity>()
        }

        // Then
        assertTrue(entity.hasComponent<Position>(), "Entity should still have Position")
        assertFalse(entity.hasComponent<Velocity>(), "Entity should not have Velocity")
    }
}
