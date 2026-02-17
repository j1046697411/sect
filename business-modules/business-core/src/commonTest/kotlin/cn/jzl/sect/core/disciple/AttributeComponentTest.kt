package cn.jzl.sect.core.disciple

import cn.jzl.ecs.*
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.*
import kotlin.test.*

class AttributeComponentTest : EntityRelationContext {
    override lateinit var world: World

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<AttributeComponent>()
        }
    }

    @BeforeTest
    fun setup() {
        world = world { install(testAddon) }
    }

    @Test
    fun testAttributeComponentCreation() {
        val entity = world.entity {
            it.addComponent(AttributeComponent(
                physique = 50,
                comprehension = 60,
                fortune = 40,
                charm = 45,
                strength = 30,
                agility = 35,
                intelligence = 40,
                endurance = 25,
                health = 100,
                maxHealth = 100,
                spirit = 50,
                maxSpirit = 50,
                age = 20
            ))
        }

        val attr = entity.getComponent<AttributeComponent>()
        assertEquals(50, attr.physique)
        assertEquals(60, attr.comprehension)
        assertEquals(20, attr.age)
    }
}
