package cn.jzl.sect.core.cultivation

import cn.jzl.ecs.*
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.*
import kotlin.test.*

class CultivationProgressTest : EntityRelationContext {
    override lateinit var world: World

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<CultivationProgress>()
        }
    }

    @BeforeTest
    fun setup() {
        world = world { install(testAddon) }
    }

    @Test
    fun testRealmEnumValues() {
        assertEquals(Realm.MORTAL, Realm.valueOf("MORTAL"))
        assertEquals(Realm.QI_REFINING, Realm.valueOf("QI_REFINING"))
        assertEquals(Realm.FOUNDATION, Realm.valueOf("FOUNDATION"))
    }

    @Test
    fun testCultivationProgressCreation() {
        val entity = world.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.QI_REFINING,
                layer = 5,
                cultivation = 5000L,
                maxCultivation = 10000L
            ))
        }

        val cultivation = entity.getComponent<CultivationProgress>()
        assertEquals(Realm.QI_REFINING, cultivation.realm)
        assertEquals(5, cultivation.layer)
        assertEquals(5000L, cultivation.cultivation)
        assertEquals(10000L, cultivation.maxCultivation)
    }
}
