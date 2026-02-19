package cn.jzl.sect.engine

import cn.jzl.ecs.ECSDsl
import cn.jzl.ecs.World
import cn.jzl.ecs.addon.Addon
import cn.jzl.ecs.addon.WorldSetup
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.component.tag
import cn.jzl.ecs.entity
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.world
import cn.jzl.sect.core.demo.ActiveTag
import cn.jzl.sect.core.demo.NameComponent
import cn.jzl.sect.core.demo.PositionComponent
import cn.jzl.sect.core.demo.VelocityComponent
import cn.jzl.sect.engine.systems.MovementSystem

/**
 * ECS Demo 世界
 *
 * 展示 ECS 基础用法：
 * 1. 定义组件和标签
 * 2. 创建 Addon 并注册组件
 * 3. 初始化 World
 * 4. 创建实体并添加组件
 * 5. 运行系统更新
 */
object DemoWorld {

    /**
     * 创建 Demo 专用的 Addon，注册所有 Demo 组件
     */
    val demoAddon = createAddon<Unit>("demo") {
        components {
            world.componentId<PositionComponent>()
            world.componentId<VelocityComponent>()
            world.componentId<NameComponent>()
            world.componentId<ActiveTag> { it.tag() }
        }
    }

    /**
     * 创建 Demo 世界并初始化实体
     */
    @OptIn(ECSDsl::class)
    fun create(): DemoContext {
        val world = world {
            WorldSetupInstallHelper.install(this, demoAddon)
        }

        // 创建移动实体 - 向右上方移动
        val entity1 = world.entity {
            it.addComponent(NameComponent("小球1"))
            it.addComponent(PositionComponent(x = 0f, y = 0f))
            it.addComponent(VelocityComponent(vx = 10f, vy = 5f))
        }

        // 创建移动实体 - 向右下方移动
        val entity2 = world.entity {
            it.addComponent(NameComponent("小球2"))
            it.addComponent(PositionComponent(x = 10f, y = 20f))
            it.addComponent(VelocityComponent(vx = 5f, vy = -3f))
        }

        // 创建静止实体（只有位置，没有速度）
        val staticEntity = world.entity {
            it.addComponent(NameComponent("静止点"))
            it.addComponent(PositionComponent(x = 50f, y = 50f))
        }

        // 初始化移动系统
        val movementSystem = MovementSystem(world)

        return DemoContext(world, movementSystem, listOf(entity1, entity2, staticEntity))
    }

    /**
     * Demo 上下文，包含世界、系统和实体引用
     */
    data class DemoContext(
        val world: World,
        val movementSystem: MovementSystem,
        val entities: List<Entity>
    )

    private object WorldSetupInstallHelper {
        @Suppress("UNCHECKED_CAST")
        fun install(ws: WorldSetup, addon: Addon<*, *>) {
            ws.install(addon as Addon<Any, Any>) {}
        }
    }
}
