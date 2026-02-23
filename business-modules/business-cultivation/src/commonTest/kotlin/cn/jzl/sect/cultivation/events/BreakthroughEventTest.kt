package cn.jzl.sect.cultivation.events

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
import cn.jzl.ecs.observer.observeAddon
import cn.jzl.ecs.observer.observeWithData
import cn.jzl.ecs.world
import cn.jzl.log.logAddon
import cn.jzl.sect.core.ai.CurrentBehavior
import cn.jzl.sect.core.ai.BehaviorType
import cn.jzl.sect.core.config.GameConfig
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.core.vitality.Spirit
import cn.jzl.sect.core.vitality.Vitality
import cn.jzl.sect.cultivation.cultivationAddon
import cn.jzl.sect.cultivation.components.CultivationProgress
import cn.jzl.sect.cultivation.components.Talent
import cn.jzl.sect.cultivation.services.CultivationService
import kotlin.test.*

class BreakthroughEventTest : EntityRelationContext {
    
    override lateinit var world: World
    private lateinit var cultivationService: CultivationService
    
    @OptIn(ECSDsl::class)
    private fun createTestWorld(): World {
        return world {
            WorldSetupInstallHelper.install(this, cultivationAddon)
            WorldSetupInstallHelper.install(this, observeAddon)
            WorldSetupInstallHelper.install(this, logAddon)
        }
    }
    
    @BeforeTest
    fun setup() {
        world = createTestWorld()
        cultivationService = CultivationService(world)
    }
    
    @Test
    fun testBreakthroughSuccessEventIsEmitted() {
        var receivedEvent: BreakthroughSuccessEvent? = null
        var receivedEntity: cn.jzl.ecs.entity.Entity? = null
        
        world.observeWithData<BreakthroughSuccessEvent>().exec {
            receivedEvent = this.event
            receivedEntity = this.entity
        }
        
        val config = GameConfig
        val maxLayer = config.cultivation.maxLayerPerRealm
        
        val entity = world.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.MORTAL,
                layer = maxLayer,
                cultivation = config.cultivation.getMaxCultivation(Realm.MORTAL, maxLayer) - 1,
                maxCultivation = config.cultivation.getMaxCultivation(Realm.MORTAL, maxLayer)
            ))
            it.addComponent(Talent(comprehension = 100, physique = 100, fortune = 100))
            it.addComponent(SectPositionInfo(SectPositionType.DISCIPLE_OUTER))
            it.addComponent(Vitality(100, 100))
            it.addComponent(Spirit(100, 100))
            it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
        }
        
        cultivationService.update(1)
        
        assertNotNull(receivedEvent, "BreakthroughSuccessEvent should be emitted")
        assertEquals(entity, receivedEntity, "Event should contain correct entity")
        assertEquals(Realm.MORTAL, receivedEvent!!.oldRealm)
        assertEquals(Realm.QI_REFINING, receivedEvent!!.newRealm)
    }
    
    @Test
    fun testBreakthroughFailedEventIsEmittedOnFailure() {
        var failedEventReceived = false
        
        world.observeWithData<BreakthroughFailedEvent>().exec {
            failedEventReceived = true
        }
        
        val config = GameConfig
        val maxLayer = config.cultivation.maxLayerPerRealm
        
        world.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.MORTAL,
                layer = maxLayer,
                cultivation = config.cultivation.getMaxCultivation(Realm.MORTAL, maxLayer) - 1,
                maxCultivation = config.cultivation.getMaxCultivation(Realm.MORTAL, maxLayer)
            ))
            it.addComponent(Talent(comprehension = 1, physique = 1, fortune = 1))
            it.addComponent(SectPositionInfo(SectPositionType.DISCIPLE_OUTER))
            it.addComponent(Vitality(100, 100))
            it.addComponent(Spirit(100, 100))
            it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
        }
        
        cultivationService.update(1)
        
        assertTrue(failedEventReceived, "BreakthroughFailedEvent should be emitted on failure")
    }
    
    @Test
    fun testBreakthroughSuccessEventData() {
        var receivedEvent: BreakthroughSuccessEvent? = null
        
        world.observeWithData<BreakthroughSuccessEvent>().exec {
            receivedEvent = this.event
        }
        
        val config = GameConfig
        val maxLayer = config.cultivation.maxLayerPerRealm
        
        world.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.MORTAL,
                layer = maxLayer,
                cultivation = config.cultivation.getMaxCultivation(Realm.MORTAL, maxLayer) - 1,
                maxCultivation = config.cultivation.getMaxCultivation(Realm.MORTAL, maxLayer)
            ))
            it.addComponent(Talent(comprehension = 100, physique = 100, fortune = 100))
            it.addComponent(SectPositionInfo(SectPositionType.DISCIPLE_OUTER))
            it.addComponent(Vitality(100, 100))
            it.addComponent(Spirit(100, 100))
            it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
        }
        
        cultivationService.update(1)
        
        assertNotNull(receivedEvent)
        assertEquals(maxLayer, receivedEvent!!.oldLayer, "Old layer should be max layer")
        assertEquals(Realm.QI_REFINING, receivedEvent!!.newRealm, "New realm should be QI_REFINING")
        assertEquals(1, receivedEvent!!.newLayer, "New layer should be 1")
    }
    
    @Test
    fun testNoEventWhenNoBreakthrough() {
        var successEventReceived = false
        var failedEventReceived = false
        
        world.observeWithData<BreakthroughSuccessEvent>().exec {
            successEventReceived = true
        }
        world.observeWithData<BreakthroughFailedEvent>().exec {
            failedEventReceived = true
        }
        
        world.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.MORTAL,
                layer = 1,
                cultivation = 0L,
                maxCultivation = 10000L
            ))
            it.addComponent(Talent(comprehension = 50, physique = 50, fortune = 50))
            it.addComponent(SectPositionInfo(SectPositionType.DISCIPLE_OUTER))
            it.addComponent(Vitality(100, 100))
            it.addComponent(Spirit(100, 100))
            it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
        }
        
        cultivationService.update(1)
        
        assertFalse(successEventReceived, "No success event should be emitted when no breakthrough")
        assertFalse(failedEventReceived, "No failed event should be emitted when no breakthrough attempt")
    }
    
    private object WorldSetupInstallHelper {
        @Suppress("UNCHECKED_CAST")
        fun install(ws: WorldSetup, addon: Addon<*, *>) {
            ws.install(addon as Addon<Any, Any>) {}
        }
    }
}
