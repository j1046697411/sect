package cn.jzl.sect.core.ai

import cn.jzl.ecs.*
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.*
import kotlin.test.*

class CurrentBehaviorTest : EntityRelationContext {
    override lateinit var world: World

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<CurrentBehavior>()
        }
    }

    @BeforeTest
    fun setup() {
        world = world { install(testAddon) }
    }

    @Test
    fun testBehaviorTypeEnum() {
        assertEquals(BehaviorType.CULTIVATE, BehaviorType.CULTIVATE)
    }

    @Test
    fun testCurrentBehaviorCreation() {
        val entity = world.entity {
            it.addComponent(CurrentBehavior(
                type = BehaviorType.CULTIVATE
            ))
        }

        val behavior = entity.getComponent<CurrentBehavior>()
        assertEquals(BehaviorType.CULTIVATE, behavior.type)
    }
}
