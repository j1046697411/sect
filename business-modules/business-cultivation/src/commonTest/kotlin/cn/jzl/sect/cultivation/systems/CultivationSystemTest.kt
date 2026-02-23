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
import cn.jzl.log.logAddon
import cn.jzl.sect.core.ai.CurrentBehavior
import cn.jzl.sect.core.ai.BehaviorType
import cn.jzl.sect.core.config.GameConfig
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.cultivation.components.CultivationProgress
import cn.jzl.sect.cultivation.components.Talent
import cn.jzl.sect.cultivation.services.CultivationService
import kotlin.test.*

/**
 * 修炼服务测试
 */
class CultivationServiceTest : EntityRelationContext {
    override lateinit var world: World
    private lateinit var service: CultivationService

    @OptIn(ECSDsl::class)
    private fun createTestWorld(): World {
        val testWorld = world {
            WorldSetupInstallHelper.install(this, createAddon<Unit>("test") {
                components {
                    world.componentId<CultivationProgress>()
                    world.componentId<Talent>()
                    world.componentId<SectPositionInfo>()
                    world.componentId<CurrentBehavior>()
                }
            })
            WorldSetupInstallHelper.install(this, logAddon)
        }
        return testWorld
    }

    @BeforeTest
    fun setup() {
        world = createTestWorld()
        service = CultivationService(world)
    }

    @Test
    fun testServiceInitialization() {
        assertNotNull(service, "修炼服务应该能正确初始化")
    }

    @Test
    fun testCultivationGain() {
        // Given: 创建一个修炼者实体
        val entity = world.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.MORTAL,
                layer = 1,
                cultivation = 0L,
                maxCultivation = 1000L
            ))
            it.addComponent(Talent(
                physique = 50,
                comprehension = 50,
                fortune = 50
            ))
            it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
            it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
        }

        // When: 推进修炼时间
        val breakthroughs = service.update(1)

        // Then: 修为应该增加
        val query = world.query { CultivatorQueryContext(world) }
        var found = false
        query.forEach { ctx ->
            if (ctx.entity == entity) {
                found = true
                assertTrue(ctx.cultivation.cultivation > 0, "修为应该增加")
            }
        }
        assertTrue(found, "应该能找到该实体")
        assertTrue(breakthroughs.isEmpty(), "不应该有突破事件")
    }

    @Test
    fun testCultivationGainWithHighAttributes() {
        // Given: 创建一个高资质的修炼者（设置足够大的maxCultivation避免突破）
        world.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.MORTAL,
                layer = 1,
                cultivation = 0L,
                maxCultivation = 10000L
            ))
            it.addComponent(Talent(
                physique = 100,
                comprehension = 100,
                fortune = 50
            ))
            it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
            it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
        }

        // When: 推进1小时
        service.update(1)

        // Then: 高资质应该获得更多修为
        val query = world.query { CultivatorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.talent.physique == 100) {
                // 基础增长 = (100 * 100 / 100) * 1 * 10 = 1000
                assertEquals(1000L, ctx.cultivation.cultivation, "高资质修炼者应该获得更多修为")
            }
        }
    }

    @Test
    fun testCultivationGainWithLowAttributes() {
        // Given: 创建一个低资质的修炼者
        world.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.MORTAL,
                layer = 1,
                cultivation = 0L,
                maxCultivation = 1000L
            ))
            it.addComponent(Talent(
                physique = 10,
                comprehension = 10,
                fortune = 50
            ))
            it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
            it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
        }

        // When: 推进1小时
        service.update(1)

        // Then: 低资质应该获得较少修为，但至少为1
        val query = world.query { CultivatorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.talent.physique == 10) {
                // 基础增长 = (10 * 10 / 100) * 10 = 10，但最低为1
                assertTrue(ctx.cultivation.cultivation >= 1, "修为增长至少为1")
            }
        }
    }

    @Test
    fun testMultipleHoursCultivation() {
        // Given: 创建一个修炼者（设置足够大的maxCultivation避免突破）
        world.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.MORTAL,
                layer = 1,
                cultivation = 0L,
                maxCultivation = 10000L
            ))
            it.addComponent(Talent(
                physique = 50,
                comprehension = 50,
                fortune = 50
            ))
            it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
            it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
        }

        // When: 推进10小时
        service.update(10)

        // Then: 修为应该按比例增加
        val query = world.query { CultivatorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.talent.physique == 50) {
                // 基础增长 = (50 * 50 / 100) * 10 * 10 = 2500
                assertEquals(2500L, ctx.cultivation.cultivation, "10小时修炼应该获得2500修为")
            }
        }
    }

    @Test
    fun testBreakthroughLayer() {
        // Given: 创建一个即将突破层数的修炼者
        world.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.MORTAL,
                layer = 1,
                cultivation = 990L,
                maxCultivation = 1000L
            ))
            it.addComponent(Talent(
                physique = 100,
                comprehension = 100,
                fortune = 100
            ))
            it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
            it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
        }

        // When: 推进足够时间触发突破
        val breakthroughs = service.update(1)

        // Then: 应该突破到第2层
        val query = world.query { CultivatorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.talent.fortune == 100) {
                assertEquals(2, ctx.cultivation.layer, "应该突破到第2层")
                assertTrue(ctx.cultivation.cultivation < 1000, "突破后修为应该清零或保留剩余")
            }
        }
    }

    @Test
    fun testBreakthroughRealm() {
        // Given: 创建一个即将突破境界的修炼者（凡人第9层）
        val config = GameConfig
        val maxLayer = config.cultivation.maxLayerPerRealm

        world.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.MORTAL,
                layer = maxLayer,
                cultivation = 990L,
                maxCultivation = 1000L
            ))
            it.addComponent(Talent(
                physique = 100,
                comprehension = 100,
                fortune = 100
            ))
            it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
            it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
        }

        // When: 推进足够时间触发突破
        val breakthroughs = service.update(1)

        // Then: 应该突破到炼气期第1层
        val query = world.query { CultivatorQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.talent.fortune == 100) {
                assertEquals(Realm.QI_REFINING, ctx.cultivation.realm, "应该突破到炼气期")
                assertEquals(1, ctx.cultivation.layer, "应该回到第1层")
            }
        }
    }

    @Test
    fun testBreakthroughEventGenerated() {
        // Given: 创建一个即将突破的修炼者
        world.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.MORTAL,
                layer = 1,
                cultivation = 995L,
                maxCultivation = 1000L
            ))
            it.addComponent(Talent(
                physique = 100,
                comprehension = 100,
                fortune = 100
            ))
            it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
            it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
        }

        // When: 推进修炼
        val breakthroughs = service.update(1)

        // Then: 应该生成突破事件
        assertTrue(breakthroughs.isNotEmpty(), "应该生成突破事件")
        val event = breakthroughs.first()
        assertEquals(Realm.MORTAL, event.oldRealm, "旧境界应该是凡人")
        assertEquals(1, event.oldLayer, "旧层数应该是1")
        assertEquals(2, event.newLayer, "新层数应该是2")
        assertEquals(SectPositionType.DISCIPLE_OUTER, event.position, "职位应该是外门弟子")
    }

    @Test
    fun testBreakthroughEventDisplay() {
        // Given: 创建一个突破事件
        val entity = world.entity {
            it.addComponent(CultivationProgress())
            it.addComponent(Talent())
            it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_INNER))
            it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
        }

        val event = CultivationService.BreakthroughEvent(
            entity = entity,
            oldRealm = Realm.MORTAL,
            oldLayer = 9,
            newRealm = Realm.QI_REFINING,
            newLayer = 1,
            position = SectPositionType.DISCIPLE_INNER
        )

        // When: 获取显示字符串
        val display = event.toDisplayString()

        // Then: 应该包含正确的信息
        assertTrue(display.contains("内门弟子"), "应该包含弟子类型")
        assertTrue(display.contains("凡人"), "应该包含旧境界")
        assertTrue(display.contains("炼气期"), "应该包含新境界")
        assertTrue(display.contains("突破成功"), "应该包含突破成功")
    }

    @Test
    fun testMultipleCultivators() {
        // Given: 创建多个修炼者
        repeat(3) { i ->
            world.entity {
                it.addComponent(CultivationProgress(
                    realm = Realm.MORTAL,
                    layer = 1,
                    cultivation = (i * 100).toLong(),
                    maxCultivation = 1000L
                ))
                it.addComponent(Talent(
                    physique = 40 + i * 10,
                    comprehension = 40 + i * 10,
                    fortune = 50
                ))
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
                it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
            }
        }

        // When: 推进修炼
        service.update(1)

        // Then: 所有修炼者都应该获得修为
        val query = world.query { CultivatorQueryContext(world) }
        var count = 0
        query.forEach { ctx ->
            if (ctx.position.position == SectPositionType.DISCIPLE_OUTER) {
                count++
                assertTrue(ctx.cultivation.cultivation > 0, "每个修炼者都应该有修为")
            }
        }
        assertTrue(count >= 3, "应该至少有3个外门弟子修炼者")
    }

    @Test
    fun testCultivationServiceWithNoCultivators() {
        // Given: 创建一个没有修炼者的世界（只有空世界）
        val emptyWorld = createTestWorld()
        val emptyService = CultivationService(emptyWorld)

        // When: 更新服务
        val breakthroughs = emptyService.update(1)

        // Then: 应该正常处理，没有异常
        assertTrue(breakthroughs.isEmpty(), "没有修炼者时不应该有突破事件")
    }

    /**
     * 查询上下文 - 修炼者
     */
    class CultivatorQueryContext(world: World) : EntityQueryContext(world) {
        val cultivation: CultivationProgress by component()
        val talent: Talent by component()
        val position: SectPositionInfo by component()
    }

    private object WorldSetupInstallHelper {
        @Suppress("UNCHECKED_CAST")
        fun install(ws: WorldSetup, addon: Addon<*, *>) {
            ws.install(addon as Addon<Any, Any>) {}
        }
    }
}
