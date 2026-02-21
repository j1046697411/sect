package cn.jzl.ecs

import cn.jzl.ecs.entity.*
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 多实体更新测试 - 验证同时更新多个实体时索引正确性
 */
class MultiEntityUpdateTest : EntityRelationContext {

    override lateinit var world: World

    @Test
    fun testUpdateMultipleEntities() {
        // Given
        world = world { }

        data class Position(val x: Int, val y: Int)

        // 创建多个实体
        val entity1 = world.entity {
            it.addComponent(Position(0, 0))
        }
        val entity2 = world.entity {
            it.addComponent(Position(10, 10))
        }
        val entity3 = world.entity {
            it.addComponent(Position(20, 20))
        }

        // When - 同时更新所有实体
        listOf(
            entity1 to Position(1, 1),
            entity2 to Position(11, 11),
            entity3 to Position(21, 21)
        ).forEach { (entity, newPos) ->
            entity.editor {
                it.addComponent(newPos)
            }
        }

        // Then - 验证所有实体都更新了
        with(world) {
            assertEquals(1, entity1.getComponent<Position>().x)
            assertEquals(11, entity2.getComponent<Position>().x)
            assertEquals(21, entity3.getComponent<Position>().x)
        }
    }
}
