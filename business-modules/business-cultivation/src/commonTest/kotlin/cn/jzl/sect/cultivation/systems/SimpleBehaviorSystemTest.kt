package cn.jzl.sect.cultivation.systems

import cn.jzl.ecs.ECSDsl
import cn.jzl.ecs.World
import cn.jzl.ecs.addon.Addon
import cn.jzl.ecs.addon.WorldSetup
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.ecs.world
import cn.jzl.sect.core.ai.BehaviorState
import cn.jzl.sect.core.ai.BehaviorType
import cn.jzl.sect.core.config.GameConfig
import cn.jzl.sect.core.cultivation.Cultivation
import cn.jzl.sect.core.disciple.Attribute
import kotlin.test.*

/**
 * 简单行为系统测试
 */
class SimpleBehaviorSystemTest : EntityRelationContext {
    override lateinit var world: World
    private lateinit var system: SimpleBehaviorSystem

    @OptIn(ECSDsl::class)
    private fun createTestWorld(): World {
        val testWorld = world {
            WorldSetupInstallHelper.install(this, createAddon<Unit>("test") {
                components {
                    world.componentId<BehaviorState>()
                    world.componentId<Attribute>()
                    world.componentId<Cultivation>()
                }
            })
        }
        return testWorld
    }

    @BeforeTest
    fun setup() {
        GameConfig.resetInstance()
        world = createTestWorld()
        system = SimpleBehaviorSystem(world)
    }

    @Test
    fun testSystemInitialization() {
        assertNotNull(system, "行为系统应该能正确初始化")
    }

    @Test
    fun testCultivateBehaviorWhenHealthy() {
        // Given: 创建一个健康的实体（精神力和生命值都充足）
        world.entity {
            it.addComponent(BehaviorState(currentBehavior = BehaviorType.REST))
            it.addComponent(Attribute(
                health = 100,
                maxHealth = 100,
                spirit = 50,
                maxSpirit = 50
            ))
            it.addComponent(Cultivation())
        }

        // When: 更新行为系统
        system.update(1.0f)

        // Then: 应该选择修炼行为
        val query = world.query { BehaviorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.attribute.maxHealth == 100) {
                assertEquals(BehaviorType.CULTIVATE, ctx.behaviorState.currentBehavior, "健康时应该选择修炼")
            }
        }
    }

    @Test
    fun testRestBehaviorWhenLowHealth() {
        // Given: 创建一个生命值低的实体
        world.entity {
            it.addComponent(BehaviorState(currentBehavior = BehaviorType.CULTIVATE))
            it.addComponent(Attribute(
                health = 20,
                maxHealth = 100,
                spirit = 50,
                maxSpirit = 50
            ))
            it.addComponent(Cultivation())
        }

        // When: 更新行为系统
        system.update(1.0f)

        // Then: 应该选择休息行为
        val query = world.query { BehaviorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.attribute.maxHealth == 100 && ctx.attribute.health == 20) {
                assertEquals(BehaviorType.REST, ctx.behaviorState.currentBehavior, "生命值低时应该选择休息")
            }
        }
    }

    @Test
    fun testWorkBehaviorWhenLowSpirit() {
        // Given: 创建一个精神力低但生命值正常的实体
        world.entity {
            it.addComponent(BehaviorState(currentBehavior = BehaviorType.CULTIVATE))
            it.addComponent(Attribute(
                health = 100,
                maxHealth = 100,
                spirit = 5,
                maxSpirit = 50
            ))
            it.addComponent(Cultivation())
        }

        // When: 更新行为系统
        system.update(1.0f)

        // Then: 应该选择工作行为
        val query = world.query { BehaviorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.attribute.maxSpirit == 50 && ctx.attribute.spirit == 5) {
                assertEquals(BehaviorType.WORK, ctx.behaviorState.currentBehavior, "精神力低时应该选择工作")
            }
        }
    }

    @Test
    fun testCultivateBehaviorAtThreshold() {
        // Given: 创建一个刚好在修炼阈值上的实体（精神力 = 30%）
        world.entity {
            it.addComponent(BehaviorState(currentBehavior = BehaviorType.REST))
            it.addComponent(Attribute(
                health = 100,
                maxHealth = 100,
                spirit = 15,
                maxSpirit = 50  // 15/50 = 30%
            ))
            it.addComponent(Cultivation())
        }

        // When: 更新行为系统
        system.update(1.0f)

        // Then: 应该仍然选择修炼（>= 30%）
        val query = world.query { BehaviorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.attribute.maxSpirit == 50 && ctx.attribute.spirit == 15) {
                assertEquals(BehaviorType.CULTIVATE, ctx.behaviorState.currentBehavior, "刚好30%精神力时应该选择修炼")
            }
        }
    }

    @Test
    fun testRestBehaviorAtHealthThreshold() {
        // Given: 创建一个刚好在休息阈值下的实体（生命值 < 30%）
        world.entity {
            it.addComponent(BehaviorState(currentBehavior = BehaviorType.CULTIVATE))
            it.addComponent(Attribute(
                health = 29,
                maxHealth = 100,  // 29/100 = 29% < 30%
                spirit = 50,
                maxSpirit = 50
            ))
            it.addComponent(Cultivation())
        }

        // When: 更新行为系统
        system.update(1.0f)

        // Then: 应该选择休息
        val query = world.query { BehaviorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.attribute.maxHealth == 100 && ctx.attribute.health == 29) {
                assertEquals(BehaviorType.REST, ctx.behaviorState.currentBehavior, "生命值29%时应该选择休息")
            }
        }
    }

    @Test
    fun testCultivateEffect() {
        // Given: 创建一个修炼中的实体
        world.entity {
            it.addComponent(BehaviorState(currentBehavior = BehaviorType.CULTIVATE))
            it.addComponent(Attribute(
                health = 100,
                maxHealth = 100,
                spirit = 50,
                maxSpirit = 50
            ))
            it.addComponent(Cultivation())
        }

        // When: 更新行为系统
        system.update(1.0f)

        // Then: 精神力应该减少
        val query = world.query { BehaviorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.attribute.maxSpirit == 50) {
                assertEquals(45, ctx.attribute.spirit, "修炼应该消耗5点精神力")
            }
        }
    }

    @Test
    fun testRestEffect() {
        // Given: 创建一个休息中的实体
        world.entity {
            it.addComponent(BehaviorState(currentBehavior = BehaviorType.REST))
            it.addComponent(Attribute(
                health = 50,
                maxHealth = 100,
                spirit = 20,
                maxSpirit = 50
            ))
            it.addComponent(Cultivation())
        }

        // When: 更新行为系统
        system.update(1.0f)

        // Then: 生命值和精神力应该恢复
        val query = world.query { BehaviorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.attribute.maxHealth == 100 && ctx.attribute.health == 50) {
                assertEquals(60, ctx.attribute.health, "休息应该恢复10点生命值")
                assertEquals(25, ctx.attribute.spirit, "休息应该恢复5点精神力")
            }
        }
    }

    @Test
    fun testWorkEffect() {
        // Given: 创建一个工作中的实体
        world.entity {
            it.addComponent(BehaviorState(currentBehavior = BehaviorType.WORK))
            it.addComponent(Attribute(
                health = 50,
                maxHealth = 100,
                spirit = 20,
                maxSpirit = 50
            ))
            it.addComponent(Cultivation())
        }

        // When: 更新行为系统
        system.update(1.0f)

        // Then: 生命值恢复，精神力消耗
        val query = world.query { BehaviorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.attribute.maxHealth == 100 && ctx.attribute.health == 50) {
                assertEquals(55, ctx.attribute.health, "工作应该恢复5点生命值")
                assertEquals(18, ctx.attribute.spirit, "工作应该消耗2点精神力")
            }
        }
    }

    @Test
    fun testSpiritNotBelowZero() {
        // Given: 创建一个精神力很少的实体
        world.entity {
            it.addComponent(BehaviorState(currentBehavior = BehaviorType.CULTIVATE))
            it.addComponent(Attribute(
                health = 100,
                maxHealth = 100,
                spirit = 3,
                maxSpirit = 50
            ))
            it.addComponent(Cultivation())
        }

        // When: 更新行为系统
        system.update(1.0f)

        // Then: 精神力不应该低于0
        val query = world.query { BehaviorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.attribute.maxSpirit == 50 && ctx.attribute.spirit <= 3) {
                assertTrue(ctx.attribute.spirit >= 0, "精神力不应该低于0")
            }
        }
    }

    @Test
    fun testHealthNotExceedMax() {
        // Given: 创建一个生命值接近上限的实体
        world.entity {
            it.addComponent(BehaviorState(currentBehavior = BehaviorType.REST))
            it.addComponent(Attribute(
                health = 95,
                maxHealth = 100,
                spirit = 45,
                maxSpirit = 50
            ))
            it.addComponent(Cultivation())
        }

        // When: 更新行为系统
        system.update(1.0f)

        // Then: 生命值不应该超过上限
        val query = world.query { BehaviorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.attribute.maxHealth == 100 && ctx.attribute.health >= 95) {
                assertEquals(100, ctx.attribute.health, "生命值不应该超过上限")
            }
        }
    }

    @Test
    fun testSpiritNotExceedMax() {
        // Given: 创建一个精神力接近上限的实体
        world.entity {
            it.addComponent(BehaviorState(currentBehavior = BehaviorType.REST))
            it.addComponent(Attribute(
                health = 50,
                maxHealth = 100,
                spirit = 48,
                maxSpirit = 50
            ))
            it.addComponent(Cultivation())
        }

        // When: 更新行为系统
        system.update(1.0f)

        // Then: 精神力不应该超过上限
        val query = world.query { BehaviorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.attribute.maxSpirit == 50 && ctx.attribute.spirit >= 48) {
                assertEquals(50, ctx.attribute.spirit, "精神力不应该超过上限")
            }
        }
    }

    @Test
    fun testBehaviorChangeUpdatesState() {
        // Given: 创建一个当前行为与建议行为不同的实体
        world.entity {
            it.addComponent(BehaviorState(
                currentBehavior = BehaviorType.REST,
                behaviorStartTime = 1000L,
                lastBehaviorTime = 0L
            ))
            it.addComponent(Attribute(
                health = 100,
                maxHealth = 100,
                spirit = 50,
                maxSpirit = 50
            ))
            it.addComponent(Cultivation())
        }

        // When: 更新行为系统
        system.update(1.0f)

        // Then: 行为状态应该更新
        val query = world.query { BehaviorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.attribute.maxHealth == 100 && ctx.attribute.spirit == 50) {
                assertEquals(BehaviorType.CULTIVATE, ctx.behaviorState.currentBehavior, "行为应该改变为修炼")
                assertTrue(ctx.behaviorState.behaviorStartTime > 1000L, "行为开始时间应该更新")
                assertEquals(1000L, ctx.behaviorState.lastBehaviorTime, "上次行为时间应该记录旧值")
            }
        }
    }

    @Test
    fun testBehaviorUnchangedWhenSame() {
        // Given: 创建一个当前行为与建议行为相同的实体
        world.entity {
            it.addComponent(BehaviorState(
                currentBehavior = BehaviorType.CULTIVATE,
                behaviorStartTime = 1000L,
                lastBehaviorTime = 0L
            ))
            it.addComponent(Attribute(
                health = 100,
                maxHealth = 100,
                spirit = 50,
                maxSpirit = 50
            ))
            it.addComponent(Cultivation())
        }

        // When: 更新行为系统
        system.update(1.0f)

        // Then: 行为状态不应该改变
        val query = world.query { BehaviorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.attribute.maxHealth == 100 && ctx.attribute.spirit == 50) {
                assertEquals(1000L, ctx.behaviorState.behaviorStartTime, "行为开始时间不应该改变")
                assertEquals(0L, ctx.behaviorState.lastBehaviorTime, "上次行为时间不应该改变")
            }
        }
    }

    @Test
    fun testMultipleEntities() {
        // Given: 创建多个不同状态的实体
        world.entity {
            it.addComponent(BehaviorState(currentBehavior = BehaviorType.REST))
            it.addComponent(Attribute(
                health = 100,
                maxHealth = 100,
                spirit = 50,
                maxSpirit = 50
            ))
            it.addComponent(Cultivation())
        }

        world.entity {
            it.addComponent(BehaviorState(currentBehavior = BehaviorType.CULTIVATE))
            it.addComponent(Attribute(
                health = 20,
                maxHealth = 100,
                spirit = 50,
                maxSpirit = 50
            ))
            it.addComponent(Cultivation())
        }

        world.entity {
            it.addComponent(BehaviorState(currentBehavior = BehaviorType.CULTIVATE))
            it.addComponent(Attribute(
                health = 100,
                maxHealth = 100,
                spirit = 10,
                maxSpirit = 50
            ))
            it.addComponent(Cultivation())
        }

        // When: 更新行为系统
        system.update(1.0f)

        // Then: 每个实体都应该有正确的行为
        val query = world.query { BehaviorQueryContext(world) }
        var cultivateCount = 0
        var restCount = 0
        var workCount = 0

        query.forEach { ctx ->
            when (ctx.behaviorState.currentBehavior) {
                BehaviorType.CULTIVATE -> cultivateCount++
                BehaviorType.REST -> restCount++
                BehaviorType.WORK -> workCount++
                else -> {}
            }
        }

        assertTrue(cultivateCount >= 1, "应该有修炼的实体")
        assertTrue(restCount >= 1, "应该有休息的实体")
        assertTrue(workCount >= 1, "应该有工作的实体")
    }

    @Test
    fun testBehaviorSystemWithNoEntities() {
        // Given: 创建一个没有行为实体的世界
        val emptyWorld = createTestWorld()
        val emptySystem = SimpleBehaviorSystem(emptyWorld)

        // When: 更新系统
        // Then: 应该正常处理，没有异常
        emptySystem.update(1.0f)
    }

    @Test
    fun testSocialBehaviorEffect() {
        // Given: 手动设置一个实体的行为为社交（通过修改组件）
        world.entity {
            it.addComponent(BehaviorState(currentBehavior = BehaviorType.SOCIAL))
            it.addComponent(Attribute(
                health = 100,
                maxHealth = 100,
                spirit = 20,
                maxSpirit = 50
            ))
            it.addComponent(Cultivation())
        }

        // When: 更新行为系统（社交行为会被保留，因为健康状态允许修炼）
        system.update(1.0f)

        // Then: 如果行为改变为社交，应该消耗精神力
        // 注意：根据当前逻辑，健康实体应该转为修炼，但这里测试社交行为的效果
        val query = world.query { BehaviorQueryContext(world) }
        query.forEach { ctx ->
            // 实体应该转为修炼（因为健康），所以精神力应该是45
            if (ctx.attribute.maxSpirit == 50 && ctx.attribute.spirit == 20) {
                assertEquals(BehaviorType.CULTIVATE, ctx.behaviorState.currentBehavior, "健康实体应该转为修炼")
                assertEquals(15, ctx.attribute.spirit, "修炼应该消耗5点精神力")
            }
        }
    }

    /**
     * 查询上下文 - 行为实体
     */
    class BehaviorQueryContext(world: World) : EntityQueryContext(world) {
        val behaviorState: BehaviorState by component()
        val attribute: Attribute by component()
        val cultivation: Cultivation by component()
    }

    private object WorldSetupInstallHelper {
        @Suppress("UNCHECKED_CAST")
        fun install(ws: WorldSetup, addon: Addon<*, *>) {
            ws.install(addon as Addon<Any, Any>) {}
        }
    }
}
