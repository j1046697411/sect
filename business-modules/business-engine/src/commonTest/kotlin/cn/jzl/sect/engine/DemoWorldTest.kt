package cn.jzl.sect.engine

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.family.component
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.demo.NameComponent
import cn.jzl.sect.core.demo.PositionComponent
import cn.jzl.sect.core.demo.VelocityComponent
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * ECS Demo 测试
 *
 * 验证 ECS 基础功能：
 * 1. 世界创建
 * 2. 实体创建和组件添加
 * 3. 系统更新逻辑
 */
class DemoWorldTest : EntityRelationContext {

    override lateinit var world: World
    private lateinit var context: DemoWorld.DemoContext

    @BeforeTest
    fun setup() {
        context = DemoWorld.create()
        world = context.world
    }

    @Test
    fun `创建 Demo 世界应该成功`() {
        // Then
        assertNotNull(context.world)
        assertNotNull(context.movementSystem)
        assertEquals(3, context.entities.size)
    }

    @Test
    fun `实体应该正确添加组件`() {
        // Given - 查询所有实体
        val query = world.query { AllEntitiesQueryContext(world) }
        val entities = mutableListOf<EntityData>()

        query.forEach { ctx ->
            entities.add(EntityData(ctx.name, ctx.position, ctx.velocity))
        }

        // Then - 验证有3个实体
        assertEquals(3, entities.size)

        // Then - 验证有2个实体有速度组件（可移动实体）
        val movableEntities = entities.filter { it.velocity != null }
        assertEquals(2, movableEntities.size)

        // Then - 验证有1个实体没有速度组件（静止实体）
        val staticEntities = entities.filter { it.velocity == null }
        assertEquals(1, staticEntities.size)

        // Then - 验证实体名称
        val names = entities.mapNotNull { it.name?.name }.toSet()
        assertTrue(names.contains("小球1"))
        assertTrue(names.contains("小球2"))
        assertTrue(names.contains("静止点"))
    }

    @Test
    fun `移动系统应该正确更新位置`() {
        // Given - 获取初始位置
        val initialQuery = world.query { MovableEntitiesQueryContext(world) }
        val initialPositions = mutableMapOf<String, Pair<Float, Float>>()

        initialQuery.forEach { ctx ->
            val name = ctx.name?.name ?: "unknown"
            initialPositions[name] = Pair(ctx.position.x, ctx.position.y)
        }

        // 验证初始位置
        assertTrue(initialPositions.containsKey("小球1"))
        assertEquals(Pair(0f, 0f), initialPositions["小球1"])

        // When - 更新1秒
        context.movementSystem.update(deltaTime = 1.0f)

        // Then - 验证位置更新
        val updatedQuery = world.query { MovableEntitiesQueryContext(world) }
        val updatedPositions = mutableMapOf<String, Pair<Float, Float>>()

        updatedQuery.forEach { ctx ->
            val name = ctx.name?.name ?: "unknown"
            updatedPositions[name] = Pair(ctx.position.x, ctx.position.y)
        }

        // 小球1: 初始(0,0)，速度(10,5)，1秒后应该是(10,5)
        assertEquals(Pair(10f, 5f), updatedPositions["小球1"])

        // 小球2: 初始(10,20)，速度(5,-3)，1秒后应该是(15,17)
        assertEquals(Pair(15f, 17f), updatedPositions["小球2"])
    }

    @Test
    fun `移动系统应该只更新有速度的实体`() {
        // Given - 查询所有实体
        val allQuery = world.query { AllEntitiesQueryContext(world) }
        val initialPositions = mutableMapOf<String, Pair<Float, Float>>()

        allQuery.forEach { ctx ->
            val name = ctx.name?.name ?: "unknown"
            initialPositions[name] = Pair(ctx.position.x, ctx.position.y)
        }

        // 验证静止点初始位置
        assertEquals(Pair(50f, 50f), initialPositions["静止点"])

        // When - 更新1秒
        context.movementSystem.update(deltaTime = 1.0f)

        // Then - 查询所有实体并验证
        val afterQuery = world.query { AllEntitiesQueryContext(world) }
        val afterPositions = mutableMapOf<String, Pair<Float, Float>>()

        afterQuery.forEach { ctx ->
            val name = ctx.name?.name ?: "unknown"
            afterPositions[name] = Pair(ctx.position.x, ctx.position.y)
        }

        // 静止点位置应该保持不变
        assertEquals(Pair(50f, 50f), afterPositions["静止点"])

        // 可移动实体位置应该改变
        assertEquals(Pair(10f, 5f), afterPositions["小球1"])
        assertEquals(Pair(15f, 17f), afterPositions["小球2"])
    }

    @Test
    fun `多次更新应该累积位置变化`() {
        // Given - 获取初始位置
        val query = world.query { NamedEntityQueryContext(world) }
        var initialX = 0f
        var initialY = 0f

        query.forEach { ctx ->
            if (ctx.name.name == "小球1") {
                initialX = ctx.position.x
                initialY = ctx.position.y
            }
        }

        assertEquals(0f, initialX)
        assertEquals(0f, initialY)

        // When - 多次更新（总共1.5秒）
        repeat(3) {
            context.movementSystem.update(deltaTime = 0.5f)
        }

        // Then - 验证累积位置
        val finalQuery = world.query { NamedEntityQueryContext(world) }
        var finalX = 0f
        var finalY = 0f

        finalQuery.forEach { ctx ->
            if (ctx.name.name == "小球1") {
                finalX = ctx.position.x
                finalY = ctx.position.y
            }
        }

        // 总时间1.5秒，速度(10,5)，位置应该是(15, 7.5)
        assertEquals(15f, finalX)
        assertEquals(7.5f, finalY)
    }

    // 查询上下文定义
    class AllEntitiesQueryContext(world: World) : EntityQueryContext(world) {
        val name: NameComponent? by component<NameComponent?>()
        val position: PositionComponent by component()
        val velocity: VelocityComponent? by component<VelocityComponent?>()
    }

    class MovableEntitiesQueryContext(world: World) : EntityQueryContext(world) {
        val name: NameComponent? by component<NameComponent?>()
        val position: PositionComponent by component()
        val velocity: VelocityComponent by component()
    }

    class NamedEntityQueryContext(world: World) : EntityQueryContext(world) {
        val name: NameComponent by component()
        val position: PositionComponent by component()
    }

    // 数据类用于存储实体信息
    data class EntityData(
        val name: NameComponent?,
        val position: PositionComponent,
        val velocity: VelocityComponent?
    )
}
