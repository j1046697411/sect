package cn.jzl.sect.core.sect

import cn.jzl.ecs.*
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.*
import kotlin.test.*

class SectTest : EntityRelationContext {
    override lateinit var world: World

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<Sect>()
            world.componentId<SectTreasury>()
        }
    }

    @BeforeTest
    fun setup() {
        world = world { install(testAddon) }
    }

    @Test
    fun testSectCreation() {
        val entity = world.entity {
            it.addComponent(Sect(
                name = "青云宗",
                leaderId = 1,
                foundedYear = 1
            ))
        }

        val sect = entity.getComponent<Sect>()
        assertEquals("青云宗", sect.name)
        assertEquals(1, sect.leaderId)
    }

    @Test
    fun testSectTreasuryComponent() {
        val entity = world.entity {
            it.addComponent(SectTreasury(
                spiritStones = 1000L,
                contributionPoints = 500L
            ))
        }

        val treasury = entity.getComponent<SectTreasury>()
        assertEquals(1000L, treasury.spiritStones)
    }
}
