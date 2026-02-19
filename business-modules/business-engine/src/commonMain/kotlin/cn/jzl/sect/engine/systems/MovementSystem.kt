package cn.jzl.sect.engine.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.family.component
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.demo.NameComponent
import cn.jzl.sect.core.demo.PositionComponent
import cn.jzl.sect.core.demo.VelocityComponent

/**
 * 移动系统 - 根据速度更新实体位置
 *
 * 查询所有同时具有 PositionComponent 和 VelocityComponent 的实体，
 * 并根据速度和时间增量更新位置。
 */
class MovementSystem(private val world: World) {

    /**
     * 更新所有匹配实体的位置
     * @param deltaTime 时间增量（秒）
     */
    fun update(deltaTime: Float) {
        val movableQuery = world.query { MovementQueryContext(world) }

        // 收集需要更新的实体和数据
        val updates = mutableListOf<UpdateData>()

        movableQuery.forEach { ctx ->
            val position = ctx.position
            val velocity = ctx.velocity

            // 计算新位置
            val newX = position.x + velocity.vx * deltaTime
            val newY = position.y + velocity.vy * deltaTime

            updates.add(UpdateData(ctx.entity, newX, newY))
        }

        // 应用更新
        updates.forEach { data ->
            world.editor(data.entity) {
                it.addComponent(PositionComponent(data.newX, data.newY))
            }
        }
    }

    private data class UpdateData(
        val entity: Entity,
        val newX: Float,
        val newY: Float
    )

    class MovementQueryContext(world: World) : EntityQueryContext(world) {
        val position: PositionComponent by component()
        val velocity: VelocityComponent by component()
        val name: NameComponent? by component<NameComponent?>()
    }
}
