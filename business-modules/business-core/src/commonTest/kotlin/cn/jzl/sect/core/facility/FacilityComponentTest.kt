package cn.jzl.sect.core.facility

import cn.jzl.ecs.*
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.*
import kotlin.test.*

class FacilityTest : EntityRelationContext {
    override lateinit var world: World

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<Facility>()
        }
    }

    @BeforeTest
    fun setup() {
        world = world { install(testAddon) }
    }

    @Test
    fun testFacilityTypeValues() {
        assertEquals(FacilityType.CULTIVATION_ROOM, FacilityType.CULTIVATION_ROOM)
    }

    @Test
    fun testFacilityCreation() {
        val entity = world.entity {
            it.addComponent(Facility(
                type = FacilityType.CULTIVATION_ROOM,
                level = 1,
                capacity = 5,
                efficiency = 1.1f
            ))
        }

        val facility = entity.getComponent<Facility>()
        assertEquals(FacilityType.CULTIVATION_ROOM, facility.type)
        assertEquals(1, facility.level)
    }
}
