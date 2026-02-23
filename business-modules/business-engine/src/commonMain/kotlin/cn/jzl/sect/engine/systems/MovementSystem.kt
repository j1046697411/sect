package cn.jzl.sect.engine.systems

import cn.jzl.core.log.ConsoleLogger
import cn.jzl.core.log.LogLevel
import cn.jzl.core.log.Logger
import cn.jzl.ecs.World
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.family.component
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.demo.Name
import cn.jzl.sect.core.demo.Position
import cn.jzl.sect.core.demo.Velocity

/**
 * 移动系统 - 根据速度更新实体位置
 *
 * 查询所有同时具有 Position 和 Velocity 的实体，
 * 并根据速度和时间增量更新位置。
 */
class MovementSystem(private val world: World) {

    private val log: Logger = ConsoleLogger(LogLevel.DEBUG, "MovementSystem")

    /**
     * 更新所有匹配实体的位置
     * @param deltaTime 时间增量（秒）
     */
    fun update(deltaTime: Float) {
        log.debug { "开始更新移动系统，时间增量: ${deltaTime}秒" }

        val movableQuery = world.query { MovementQueryContext(world) }

        // 收集需要更新的实体和数据
        val updates = mutableListOf<UpdateData>()
        var entityCount = 0

        movableQuery.forEach { ctx ->
            entityCount++
            val position = ctx.position
            val velocity = ctx.velocity
            val name = ctx.name

            // 计算新位置
            val newX = position.x + velocity.vx * deltaTime
            val newY = position.y + velocity.vy * deltaTime

            log.debug { "实体移动: ${name?.name ?: ctx.entity} 从 (${position.x}, ${position.y}) 到 (${newX}, ${newY})" }

            updates.add(UpdateData(ctx.entity, newX, newY))
        }

        log.debug { "移动系统处理了 ${entityCount} 个实体" }

        // 应用更新
        updates.forEach { data ->
            world.editor(data.entity) {
                it.addComponent(Position(data.newX, data.newY))
            }
        }

        log.debug { "移动系统更新完成" }
    }

    private data class UpdateData(
        val entity: Entity,
        val newX: Float,
        val newY: Float
    )

    class MovementQueryContext(world: World) : EntityQueryContext(world) {
        val position: Position by component()
        val velocity: Velocity by component()
        val name: Name? by component<Name?>()
    }
}
