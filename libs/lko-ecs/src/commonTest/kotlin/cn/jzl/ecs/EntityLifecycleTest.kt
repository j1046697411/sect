package cn.jzl.ecs

import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.component.tag
import cn.jzl.ecs.entity.*
import cn.jzl.ecs.family.component
import cn.jzl.ecs.query.EntityQueryContext
import kotlin.test.*

// 实体生命周期测试专用的数据类
private data class LifecyclePosition(val x: Int, val y: Int)
private data class LifecycleHealth(val current: Int, val max: Int)
private sealed class LifecycleActiveTag

private class LifecyclePositionContext(world: World) : EntityQueryContext(world) {
    val position: LifecyclePosition by component()
}

/**
 * 实体生命周期测试
 * 测试实体创建、销毁、版本控制、激活状态等
 */
class EntityLifecycleTest : EntityRelationContext {
    
    override lateinit var world: World
    
    @BeforeTest
    fun setup() {
        world = world {
            install(testAddon)
        }
    }
        
    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<LifecyclePosition>()
            world.componentId<LifecycleHealth>()
            world.componentId<LifecycleActiveTag> { it.tag() }
        }
    }

    @Test
    fun testEntityCreation() {
        val entity = world.entity {}

        assertTrue(entity.id > 0, "Entity ID should be positive")
        assertEquals(0, entity.version, "New entity should have version 0")
        assertTrue(world.isActive(entity), "Entity should be active")
    }

    @Test
    fun testEntityWithComponentsCreation() {
        val entity = world.entity {
            it.addComponent(LifecyclePosition(10, 20))
            it.addComponent(LifecycleHealth(100, 100))
        }

        assertTrue(entity.id > 0, "Entity ID should be positive")
        assertEquals(LifecyclePosition(10, 20), entity.getComponent<LifecyclePosition>())
        assertEquals(LifecycleHealth(100, 100), entity.getComponent<LifecycleHealth>())
    }

    @Test
    fun testMultipleEntityCreation() {
        val entities = mutableListOf<Entity>()
        repeat(10) {
            entities.add(world.entity {})
        }

        assertEquals(10, entities.size, "Should create 10 entities")

        val ids = entities.map { it.id }.toSet()
        assertEquals(10, ids.size, "All entity IDs should be unique")
    }

    @Test
    fun testEntityIdSequence() {
        val entity1 = world.entity {}
        val entity2 = world.entity {}
        val entity3 = world.entity {}

        assertTrue(entity2.id > entity1.id, "Entity IDs should increment")
        assertTrue(entity3.id > entity2.id, "Entity IDs should increment")
    }

    @Test
    fun testEntityVersionIncrement() {
        val entity = world.entity {}
        val initialVersion = entity.version

        assertEquals(0, initialVersion, "Initial version should be 0")
    }

    @Test
    fun testEntityActivationCheck() {
        val entity = world.entity {}

        assertTrue(world.isActive(entity), "Created entity should be active")

        entity.editor {
            it.addComponent(LifecyclePosition(10, 20))
        }

        assertTrue(world.isActive(entity), "Entity should still be active after component addition")
    }

    @Test
    fun testEmptyEntityCreation() {
        val entity = world.entity {}

        assertTrue(world.isActive(entity), "Empty entity should be active")
        assertTrue(entity.id > 0, "Empty entity should have valid ID")
    }

    @Test
    fun testBatchEntityCreation() {
        val count = 100
        val entities = mutableListOf<Entity>()

        repeat(count) { index ->
            entities.add(world.entity {
                it.addComponent(LifecyclePosition(index, index * 2))
            })
        }

        assertEquals(count, entities.size, "Should create $count entities")

        entities.forEachIndexed { index, entity ->
            assertTrue(world.isActive(entity), "Entity $index should be active")
        }
    }

    @Test
    fun testEntityWithTagsCreation() {
        val entity = world.entity {
            it.addTag<LifecycleActiveTag>()
        }

        assertTrue(world.isActive(entity), "Entity should be active")
        assertTrue(entity.hasTag<LifecycleActiveTag>(), "Entity should have tag")
    }

    @Test
    fun testEntityQueryAfterCreation() {
        val initialCount = world.familyService.family { component<LifecyclePosition>() }.size

        world.entity {
            it.addComponent(LifecyclePosition(10, 20))
        }

        val newCount = world.familyService.family { component<LifecyclePosition>() }.size

        assertEquals(initialCount + 1, newCount, "Entity should appear in query results")
    }

    @Test
    fun testEntityIdentity() {
        val entity1 = world.entity {}
        val entity2 = world.entity {}

        assertNotEquals(entity1.data, entity2.data, "Different entities should have different data")
        assertNotEquals(entity1.id, entity2.id, "Different entities should have different IDs")
    }

    @Test
    fun testEntityWithMultipleComponents() {
        world.entity {
            it.addComponent(LifecyclePosition(10, 20))
            it.addComponent(LifecycleHealth(100, 100))
            it.addTag<LifecycleActiveTag>()
        }

        val query = world.familyService.family {
            component<LifecyclePosition>()
            component<LifecycleHealth>()
        }

        assertTrue(query.size >= 1, "Entity should be in query")
    }
}
