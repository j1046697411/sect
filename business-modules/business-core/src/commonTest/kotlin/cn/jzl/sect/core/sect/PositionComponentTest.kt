package cn.jzl.sect.core.sect

import cn.jzl.ecs.*
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.*
import kotlin.test.*

class PositionTest : EntityRelationContext {
    override lateinit var world: World

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<Position>()
        }
    }

    @BeforeTest
    fun setup() {
        world = world { install(testAddon) }
    }

    @Test
    fun testPositionEnumValues() {
        assertEquals(SectPosition.LEADER, SectPosition.valueOf("LEADER"))
        assertEquals(SectPosition.ELDER, SectPosition.valueOf("ELDER"))
    }

    @Test
    fun testPositionCreation() {
        val entity = world.entity {
            it.addComponent(Position(position = SectPosition.DISCIPLE_OUTER))
        }

        val pos = entity.getComponent<Position>()
        assertEquals(SectPosition.DISCIPLE_OUTER, pos.position)
    }
}
