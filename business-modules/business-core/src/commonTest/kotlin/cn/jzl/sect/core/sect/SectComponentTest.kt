package cn.jzl.sect.core.sect

import cn.jzl.ecs.*
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.*
import kotlin.test.*

class SectComponentTest : EntityRelationContext {
    override lateinit var world: World

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<SectComponent>()
            world.componentId<SectResourceComponent>()
        }
    }

    @BeforeTest
    fun setup() {
        world = world { install(testAddon) }
    }

    @Test
    fun testSectComponentCreation() {
        val entity = world.entity {
            it.addComponent(SectComponent(
                name = "青云宗",
                leaderId = 1,
                foundedYear = 1
            ))
        }

        val sect = entity.getComponent<SectComponent>()
        assertEquals("青云宗", sect.name)
        assertEquals(1, sect.leaderId)
    }

    @Test
    fun testResourceComponent() {
        val entity = world.entity {
            it.addComponent(SectResourceComponent(
                spiritStones = 1000L,
                contributionPoints = 500L
            ))
        }

        val resource = entity.getComponent<SectResourceComponent>()
        assertEquals(1000L, resource.spiritStones)
    }
}
