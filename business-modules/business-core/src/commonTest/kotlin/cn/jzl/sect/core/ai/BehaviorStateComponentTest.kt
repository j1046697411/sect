package cn.jzl.sect.core.ai

import cn.jzl.ecs.*
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.*
import kotlin.test.*

class BehaviorStateComponentTest : EntityRelationContext {
    override lateinit var world: World

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<BehaviorStateComponent>()
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
    fun testBehaviorStateComponentCreation() {
        val entity = world.entity {
            it.addComponent(BehaviorStateComponent(
                currentBehavior = BehaviorType.CULTIVATE
            ))
        }

        val behavior = entity.getComponent<BehaviorStateComponent>()
        assertEquals(BehaviorType.CULTIVATE, behavior.currentBehavior)
    }
}
