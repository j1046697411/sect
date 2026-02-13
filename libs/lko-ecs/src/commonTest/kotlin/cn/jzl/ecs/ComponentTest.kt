package cn.jzl.ecs

import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.component.tag
import cn.jzl.ecs.entity.*
import cn.jzl.ecs.family.component
import cn.jzl.ecs.query.EntityQueryContext
import kotlin.jvm.JvmInline
import kotlin.test.*

// Component测试专用的数据类
private data class CompPosition(val x: Int, val y: Int)
private data class CompVelocity(val dx: Int, val dy: Int)
private data class CompHealth(val current: Int, val max: Int)
@JvmInline private value class CompLevel(val value: Int)
private sealed class CompActiveTag
private sealed class CompEnemyTag

private class CompPositionContext(world: World) : EntityQueryContext(world) {
    val position: CompPosition by component()
}

/**
 * 组件系统测试
 * 测试组件注册、添加、移除、更新等操作
 */
class ComponentTest : EntityRelationContext {
    
    override lateinit var world: World
    
    @BeforeTest
    fun setup() {
        world = world {
            install(testAddon)
        }
    }

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<CompPosition>()
            world.componentId<CompVelocity>()
            world.componentId<CompHealth>()
            world.componentId<CompLevel>()
            world.componentId<CompActiveTag> { it.tag() }
            world.componentId<CompEnemyTag> { it.tag() }
        }
    }

    @Test
    fun testComponentRegistration() {
        val posId = world.components.id<CompPosition>()
        val velocityId = world.components.id<CompVelocity>()
        val healthId = world.components.id<CompHealth>()

        assertTrue(posId.data > 0, "Position component should be registered")
        assertTrue(velocityId.data > 0, "Velocity component should be registered")
        assertTrue(healthId.data > 0, "Health component should be registered")
        assertNotEquals(posId.data, velocityId.data, "Component IDs should be unique")
    }

    @Test
    fun testTagComponentRegistration() {
        val activeTagId = world.components.id<CompActiveTag>()
        val enemyTagId = world.components.id<CompEnemyTag>()

        assertTrue(activeTagId.data > 0, "ActiveTag should be registered")
        assertTrue(enemyTagId.data > 0, "EnemyTag should be registered")
    }

    @Test
    fun testComponentAddition() {
        val entity = world.entity {
            it.addComponent(CompPosition(10, 20))
        }

        val pos = entity.getComponent<CompPosition>()
        assertEquals(CompPosition(10, 20), pos, "Component should be added correctly")
    }

    @Test
    fun testMultipleComponentsAddition() {
        val entity = world.entity {
            it.addComponent(CompPosition(10, 20))
            it.addComponent(CompVelocity(5, -3))
            it.addComponent(CompHealth(100, 100))
        }

        assertEquals(CompPosition(10, 20), entity.getComponent<CompPosition>())
        assertEquals(CompVelocity(5, -3), entity.getComponent<CompVelocity>())
        assertEquals(CompHealth(100, 100), entity.getComponent<CompHealth>())
    }

    @Test
    fun testTagAddition() {
        val entity = world.entity {
            it.addTag<CompActiveTag>()
        }

        assertTrue(entity.hasTag<CompActiveTag>(), "Tag should be added to entity")
    }

    @Test
    fun testMultipleTagsAddition() {
        val entity = world.entity {
            it.addTag<CompActiveTag>()
            it.addTag<CompEnemyTag>()
        }

        assertTrue(entity.hasTag<CompActiveTag>(), "ActiveTag should be present")
        assertTrue(entity.hasTag<CompEnemyTag>(), "EnemyTag should be present")
    }

    @Test
    fun testComponentUpdate() {
        val entity = world.entity {
            it.addComponent(CompPosition(10, 20))
        }

        entity.editor {
            it.addComponent(CompPosition(30, 40))
        }

        val updatedPos = entity.getComponent<CompPosition>()
        assertEquals(CompPosition(30, 40), updatedPos, "Component should be updated")
    }

    @Test
    fun testMultipleComponentsUpdate() {
        val entity = world.entity {
            it.addComponent(CompPosition(10, 20))
            it.addComponent(CompHealth(100, 100))
        }

        entity.editor {
            it.addComponent(CompPosition(50, 60))
            it.addComponent(CompHealth(80, 100))
        }

        assertEquals(CompPosition(50, 60), entity.getComponent<CompPosition>())
        assertEquals(CompHealth(80, 100), entity.getComponent<CompHealth>())
    }

    @Test
    fun testComponentRemoval() {
        val entity = world.entity {
            it.addComponent(CompPosition(10, 20))
            it.addComponent(CompVelocity(5, 5))
        }

        assertTrue(entity.hasComponent<CompPosition>(), "Position should exist before removal")

        entity.editor {
            it.removeComponent<CompPosition>()
        }

        assertFalse(entity.hasComponent<CompPosition>(), "Position should be removed")
        assertTrue(entity.hasComponent<CompVelocity>(), "Velocity should still exist")
    }

    @Test
    fun testTagRemoval() {
        val entity = world.entity {
            it.addTag<CompActiveTag>()
            it.addTag<CompEnemyTag>()
        }

        entity.editor {
            it.removeTag<CompActiveTag>()
        }

        assertFalse(entity.hasTag<CompActiveTag>(), "ActiveTag should be removed")
        assertTrue(entity.hasTag<CompEnemyTag>(), "EnemyTag should still exist")
    }

    @Test
    fun testComponentReplacement() {
        val entity = world.entity {
            it.addComponent(CompPosition(10, 20))
        }

        entity.editor {
            it.addComponent(CompPosition(100, 200))
        }

        assertEquals(CompPosition(100, 200), entity.getComponent<CompPosition>(), "Component should be replaced")
    }

    @Test
    fun testComponentQuery() {
        world.entity {
            it.addComponent(CompPosition(10, 20))
            it.addComponent(CompHealth(100, 100))
        }
        world.entity {
            it.addComponent(CompPosition(30, 40))
        }
        world.entity {
            it.addComponent(CompHealth(50, 100))
        }

        val posQuery = world.familyService.family { component<CompPosition>() }
        val healthQuery = world.familyService.family { component<CompHealth>() }

        assertEquals(2, posQuery.size, "Should have 2 entities with Position")
        assertEquals(2, healthQuery.size, "Should have 2 entities with Health")
    }

    @Test
    fun testMixedComponentAndTag() {
        val entity = world.entity {
            it.addComponent(CompPosition(10, 20))
            it.addComponent(CompHealth(100, 100))
            it.addTag<CompActiveTag>()
        }

        assertTrue(entity.hasComponent<CompPosition>(), "Should have Position component")
        assertTrue(entity.hasComponent<CompHealth>(), "Should have Health component")
        assertTrue(entity.hasTag<CompActiveTag>(), "Should have ActiveTag")
    }

    @Test
    fun testValueClassComponent() {
        val entity = world.entity {
            it.addComponent(CompLevel(5))
        }

        val level = entity.getComponent<CompLevel>()
        assertEquals(CompLevel(5), level, "Value class component should work correctly")
    }
}
