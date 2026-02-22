package cn.jzl.sect.core.sect

import cn.jzl.ecs.*
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.*
import kotlin.test.*

class SectPositionInfoTest : EntityRelationContext {
    override lateinit var world: World

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<SectPositionInfo>()
        }
    }

    @BeforeTest
    fun setup() {
        world = world { install(testAddon) }
    }

    @Test
    fun testSectPositionTypeEnumValues() {
        assertEquals(SectPositionType.LEADER, SectPositionType.valueOf("LEADER"))
        assertEquals(SectPositionType.ELDER, SectPositionType.valueOf("ELDER"))
    }

    @Test
    fun testSectPositionInfoCreation() {
        val entity = world.entity {
            it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
        }

        val pos = entity.getComponent<SectPositionInfo>()
        assertEquals(SectPositionType.DISCIPLE_OUTER, pos.position)
    }
}
