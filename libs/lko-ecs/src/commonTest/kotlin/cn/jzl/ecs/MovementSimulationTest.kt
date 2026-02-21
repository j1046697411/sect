package cn.jzl.ecs

import cn.jzl.ecs.entity.*
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 移动模拟测试 - 模拟 MovementSystem 的行为
 */
class MovementSimulationTest : EntityRelationContext {

    override lateinit var world: World

    data class Position(var x: Float, var y: Float)
    data class Velocity(val vx: Float, val vy: Float)

    @Test
    fun testMovementSimulation() {
        // Given
        world = world { }

        // 创建两个移动实体
        val entity1 = world.entity {
            it.addComponent(Position(0f, 0f))
            it.addComponent(Velocity(10f, 5f))
        }
        val entity2 = world.entity {
            it.addComponent(Position(10f, 20f))
            it.addComponent(Velocity(5f, -3f))
        }

        // When - 模拟 MovementSystem 更新
        // 收集更新
        data class UpdateData(val entity: cn.jzl.ecs.entity.Entity, val newX: Float, val newY: Float)
        val updates = mutableListOf<UpdateData>()

        world.query { MovementQueryContext(world) }.forEach { ctx ->
            val position = ctx.position
            val velocity = ctx.velocity
            val newX = position.x + velocity.vx * 1.0f
            val newY = position.y + velocity.vy * 1.0f
            updates.add(UpdateData(ctx.entity, newX, newY))
        }

        // 应用更新
        updates.forEach { data ->
            data.entity.editor {
                it.addComponent(Position(data.newX, data.newY))
            }
        }

        // Then - 验证位置更新
        with(world) {
            assertEquals(10f, entity1.getComponent<Position>().x, "Entity1 x should be 10")
            assertEquals(5f, entity1.getComponent<Position>().y, "Entity1 y should be 5")
            assertEquals(15f, entity2.getComponent<Position>().x, "Entity2 x should be 15")
            assertEquals(17f, entity2.getComponent<Position>().y, "Entity2 y should be 17")
        }
    }

    class MovementQueryContext(world: World) : EntityQueryContext(world) {
        val position: Position by component()
        val velocity: Velocity by component()
    }
}
