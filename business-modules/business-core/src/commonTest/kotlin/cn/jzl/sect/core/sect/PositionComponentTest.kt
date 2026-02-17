package cn.jzl.sect.core.sect

import cn.jzl.ecs.*
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.*
import kotlin.test.*

class PositionComponentTest : EntityRelationContext {
    override lateinit var world: World

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<PositionComponent>()
        }
    }

    @BeforeTest
    fun setup() {
        world = world { install(testAddon) }
    }

    @Test
    fun testPositionEnumValues() {
        assertEquals(Position.LEADER, Position.valueOf("LEADER"))
        assertEquals(Position.ELDER, Position.valueOf("ELDER"))
        assertEquals(Position.DISCIPLE_CORE, Position.valueOf("DISCIPLE_CORE"))
    }

    @Test
    fun testPositionComponentCreation() {
        val entity = world.entity {
            it.addComponent(PositionComponent(position = Position.DISCIPLE_OUTER))
        }

        val pos = entity.getComponent<PositionComponent>()
        assertEquals(Position.DISCIPLE_OUTER, pos.position)
    }
}
