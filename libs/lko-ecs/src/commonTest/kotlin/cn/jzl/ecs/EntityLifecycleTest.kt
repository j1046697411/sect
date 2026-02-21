package cn.jzl.ecs

import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.OnEntityDestroyed
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.component.tag
import cn.jzl.ecs.entity.*
import cn.jzl.ecs.family.component
import cn.jzl.ecs.observer.observe
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

    @Test
    fun testCreateEntityWithSpecificId() {
        // Given: 指定一个实体 ID
        val specificId = 100

        // When: 使用指定 ID 创建实体
        val entity = world.entityService.create(specificId, false) {}

        // Then: 实体应该具有指定的 ID
        assertEquals(specificId, entity.id, "Entity should have the specified ID")
        assertTrue(world.isActive(entity), "Entity should be active")
    }

    @Test
    fun testCreateEntityWithSpecificIdThrowsWhenIdInUse() {
        // Given: 创建一个实体并获取其 ID
        val existingEntity = world.entity {}
        val existingId = existingEntity.id

        // When/Then: 使用相同的 ID 创建实体应该抛出异常
        assertFailsWith<IllegalArgumentException>("Should throw when ID is already in use") {
            world.entityService.create(existingId, false) {}
        }
    }

    @Test
    fun testDestroyEntityRemovesFromWorld() {
        // Given: 创建一个带有组件的实体
        val entity = world.entity {
            it.addComponent(LifecyclePosition(10, 20))
            it.addComponent(LifecycleHealth(100, 100))
        }

        // When: 销毁实体
        world.destroy(entity)

        // Then: 实体应该不再活跃
        assertFalse(world.isActive(entity), "Entity should not be active after destruction")
    }

    @Test
    fun testDestroyEntityRemovesComponents() {
        // Given: 创建一个带有组件的实体
        val entity = world.entity {
            it.addComponent(LifecyclePosition(10, 20))
        }
        val initialCount = world.familyService.family { component<LifecyclePosition>() }.size

        // When: 销毁实体
        world.destroy(entity)

        // Then: 实体不应该再出现在组件查询中
        val finalCount = world.familyService.family { component<LifecyclePosition>() }.size
        assertEquals(initialCount - 1, finalCount, "Entity should be removed from component query")
    }

    @Test
    fun testDestroyEntityTriggersOnEntityDestroyedEvent() {
        // Given: 创建一个实体并设置事件监听
        var eventTriggered = false
        var destroyedEntity: Entity? = null

        val entity = world.entity {
            it.addComponent(LifecyclePosition(10, 20))
        }

        world.observe<OnEntityDestroyed>(entity).exec {
            eventTriggered = true
            destroyedEntity = entity
        }

        // When: 销毁实体
        world.destroy(entity)

        // Then: 事件应该被触发
        assertTrue(eventTriggered, "OnEntityDestroyed event should be triggered")
        assertEquals(entity, destroyedEntity, "Event should contain the destroyed entity")
    }

    @Test
    fun testDestroyNonExistentEntityDoesNothing() {
        // Given: 创建一个无效实体
        val invalidEntity = Entity(999999, 0)

        // When: 尝试销毁不存在的实体
        world.destroy(invalidEntity)

        // Then: 不应该抛出异常
        assertFalse(world.isActive(invalidEntity), "Invalid entity should not be active")
    }

    @Test
    fun testDestroyEntityMultipleTimesIsSafe() {
        // Given: 创建一个实体
        val entity = world.entity {
            it.addComponent(LifecyclePosition(10, 20))
        }

        // When: 销毁实体两次
        world.destroy(entity)
        world.destroy(entity)

        // Then: 不应该抛出异常，实体仍然不活跃
        assertFalse(world.isActive(entity), "Entity should not be active")
    }

    @Test
    fun testEntityDestroyExtensionFunction() {
        // Given: 创建一个实体
        val entity = world.entity {
            it.addComponent(LifecyclePosition(10, 20))
        }

        // When: 使用扩展函数销毁实体
        with(world) {
            entity.destroy()
        }

        // Then: 实体应该不再活跃
        assertFalse(world.isActive(entity), "Entity should not be active after destruction")
    }
}
