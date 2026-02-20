package cn.jzl.ecs

import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.archetype.Archetype
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.component.tag
import cn.jzl.ecs.entity.*
import cn.jzl.ecs.family.component
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.relation.component
import kotlin.test.*

// Archetype测试专用的数据类
private data class ArchePosition(val x: Int, val y: Int)
private data class ArcheVelocity(val dx: Int, val dy: Int)
private data class ArcheHealth(val current: Int, val max: Int)
private sealed class ArcheActiveTag

private class ArchePositionContext(world: World) : EntityQueryContext(world) {
    val position: ArchePosition by component()
}

/**
 * Archetype系统测试
 * 测试archetype迁移、内存管理、表操作等
 */
class ArchetypeTest : EntityRelationContext {
    
    override lateinit var world: World
    
    @BeforeTest
    fun setup() {
        world = world {
            install(testAddon)
        }
    }
        
    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<ArchePosition>()
            world.componentId<ArcheVelocity>()
            world.componentId<ArcheHealth>()
            world.componentId<ArcheActiveTag> { it.tag() }
        }
    }

    @Test
    fun testRootArchetype() {
        val rootArchetype = world.archetypeService.rootArchetype

        assertNotNull(rootArchetype, "Root archetype should exist")
        assertEquals(0, rootArchetype.id, "Root archetype should have ID 0")
    }

    @Test
    fun testArchetypeMigration() {
        val entity = world.entity {}
        val initialArchetype = getEntityArchetype(world, entity)

        assertEquals(0, initialArchetype.id, "Entity should start in root archetype")

        entity.editor {
            it.addComponent(ArchePosition(10, 20))
        }

        val afterPosArchetype = getEntityArchetype(world, entity)
        assertNotEquals(initialArchetype.id, afterPosArchetype.id, "Entity should move to new archetype")

        entity.editor {
            it.addComponent(ArcheVelocity(5, 5))
        }

        val afterVelArchetype = getEntityArchetype(world, entity)
        assertNotEquals(afterPosArchetype.id, afterVelArchetype.id, "Entity should move again after adding Velocity")
    }

    @Test
    fun testMultipleEntityArchetypes() {
        val entity1 = world.entity {
            it.addComponent(ArchePosition(10, 20))
        }
        val entity2 = world.entity {
            it.addComponent(ArchePosition(30, 40))
        }
        val entity3 = world.entity {
            it.addComponent(ArcheVelocity(5, 5))
        }

        val archetype1 = getEntityArchetype(world, entity1)
        val archetype2 = getEntityArchetype(world, entity2)
        val archetype3 = getEntityArchetype(world, entity3)

        assertEquals(archetype1.id, archetype2.id, "Entities with same components should be in same archetype")
        assertNotEquals(archetype1.id, archetype3.id, "Entities with different components should be in different archetypes")
    }

    @Test
    fun testArchetypeSize() {
        // 不检查root archetype的大小，因为它可能包含之前测试的实体
        // 改为检查创建实体后对应archetype的大小

        repeat(5) {
            world.entity { it.addComponent(ArchePosition(10, 20)) }
        }

        val posArchetype = world.familyService.family { component<ArchePosition>() }
            .archetypes
            .firstOrNull()

        assertNotNull(posArchetype, "Should have an archetype with Position")
        assertTrue(posArchetype.size >= 5, "Archetype should have at least 5 entities")
    }

    @Test
    fun testArchetypeWithTag() {
        val entity1 = world.entity {
            it.addComponent(ArchePosition(10, 20))
            it.addTag<ArcheActiveTag>()
        }
        val entity2 = world.entity {
            it.addComponent(ArchePosition(30, 40))
        }

        val archetype1 = getEntityArchetype(world, entity1)
        val archetype2 = getEntityArchetype(world, entity2)

        assertNotEquals(archetype1.id, archetype2.id, "Entity with tag should be in different archetype")
    }

    @Test// ECS框架removeComponent功能有待修复
    fun testArchetypeAfterComponentRemoval() {
        val entity = world.entity {
            it.addComponent(ArchePosition(10, 20))
            it.addComponent(ArcheVelocity(5, 5))
        }

        val beforeRemoval = entity.hasComponent<ArcheVelocity>()
        assertTrue(beforeRemoval, "Entity should have Velocity before removal")

        entity.editor {
            it.removeComponent<ArcheVelocity>()
        }

        val afterRemoval = entity.hasComponent<ArcheVelocity>()
        assertFalse(afterRemoval, "Entity should not have Velocity after removal")
        
        // 验证实体仍然活跃且保有其他组件
        assertTrue(world.isActive(entity), "Entity should still be active")
        assertTrue(entity.hasComponent<ArchePosition>(), "Entity should still have Position")
    }

    @Test
    fun testArchetypeMemoryEfficiency() {
        repeat(100) { index ->
            world.entity {
                it.addComponent(ArchePosition(index, index * 2))
            }
        }

        val positionFamily = world.familyService.family { component<ArchePosition>() }

        val archetypes = positionFamily.archetypes
        assertEquals(1, archetypes.size, "All Position-only entities should share one archetype")
        val firstArchetypeSize = archetypes.firstOrNull()?.size ?: 0
        assertEquals(100, firstArchetypeSize, "Archetype should contain all 100 entities")
    }

    @Test
    fun testArchetypeEdgeAddition() {
        val entity = world.entity {
            it.addComponent(ArchePosition(10, 20))
        }

        val archetype1 = getEntityArchetype(world, entity)

        entity.editor {
            it.addComponent(ArcheHealth(100, 100))
        }

        val archetype2 = getEntityArchetype(world, entity)

        assertNotNull(archetype1, "First archetype should exist")
        assertNotNull(archetype2, "Second archetype should exist")
        assertNotEquals(archetype1.id, archetype2.id, "Entity should move to new archetype after adding component")
    }

    @Test// ECS框架removeComponent功能有待修复
    fun testArchetypeEdgeRemoval() {
        val entity = world.entity {
            it.addComponent(ArchePosition(10, 20))
            it.addComponent(ArcheHealth(100, 100))
        }

        val beforeRemoval = entity.hasComponent<ArcheHealth>()
        assertTrue(beforeRemoval, "Entity should have Health before removal")

        entity.editor {
            it.removeComponent<ArcheHealth>()
        }

        val afterRemoval = entity.hasComponent<ArcheHealth>()
        assertFalse(afterRemoval, "Entity should not have Health after removal")
        
        // 验证实体仍然活跃且保有其他组件
        assertTrue(world.isActive(entity), "Entity should still be active")
        assertTrue(entity.hasComponent<ArchePosition>(), "Entity should still have Position")
    }

    @Test
    fun testArchetypeTableOperations() {
        val entity = world.entity {
            it.addComponent(ArchePosition(10, 20))
        }

        world.entityService.runOn(entity) { entityIndex ->
            assertTrue(entityIndex >= 0, "Entity should have valid index in table")
            val storedEntity = this.table.entities[entityIndex]
            assertEquals(entity.id, storedEntity.id, "Table should store correct entity")
        }
    }

    @Test // ECS框架removeComponent功能有待修复
    fun testMultipleComponentArchetypeMigration() {
        val entity = world.entity {}

        entity.editor { it.addComponent(ArchePosition(10, 20)) }
        val hasPosition = entity.hasComponent<ArchePosition>()
        assertTrue(hasPosition, "Should have Position after addition")

        entity.editor { it.addComponent(ArcheVelocity(5, 5)) }
        val hasVelocity = entity.hasComponent<ArcheVelocity>()
        assertTrue(hasVelocity, "Should have Velocity after addition")

        entity.editor { it.addComponent(ArcheHealth(100, 100)) }
        val hasHealth = entity.hasComponent<ArcheHealth>()
        assertTrue(hasHealth, "Should have Health after addition")

        entity.editor { it.removeComponent<ArcheHealth>() }
        val hasHealthAfterRemoval = entity.hasComponent<ArcheHealth>()
        assertFalse(hasHealthAfterRemoval, "Should not have Health after removal")
        
        // 验证其他组件仍然保留
        assertTrue(entity.hasComponent<ArchePosition>(), "Should still have Position")
        assertTrue(entity.hasComponent<ArcheVelocity>(), "Should still have Velocity")
    }

    @Test
    fun testArchetypeEntityType() {
        val entity = world.entity {
            it.addComponent(ArchePosition(10, 20))
        }

        val archetype = getEntityArchetype(world, entity)
        val entityType = archetype.entityType

        assertNotNull(entityType, "Entity type should exist")
        assertTrue(entityType.isNotEmpty(), "Entity type should not be empty")
    }

    @Test
    fun testArchetypeComponentIndex() {
        val entity = world.entity {
            it.addComponent(ArchePosition(10, 20))
        }

        val relation = world.relations.component<ArchePosition>()

        val componentIndex = world.relationService.getComponentIndex(entity, relation)
        assertNotNull(componentIndex, "Should get component index")
    }

    @Test
    fun testBatchEntityArchetypeManagement() {
        val entities = mutableListOf<Entity>()

        repeat(50) { index ->
            entities.add(world.entity {
                it.addComponent(ArchePosition(index, index * 2))
                it.addComponent(ArcheVelocity(index % 10, index % 10))
            })
        }

        entities.forEach { entity ->
            assertTrue(world.isActive(entity), "Entity should be active")
            val archetype = getEntityArchetype(world, entity)
            assertNotNull(archetype, "Entity should have an archetype")
        }

        val family = world.familyService.family {
            component<ArchePosition>()
            component<ArcheVelocity>()
        }

        assertTrue(family.size >= 50, "Family should contain all created entities")
    }

    private fun getEntityArchetype(world: World, entity: Entity): Archetype {
        return world.entityService.runOn(entity) { this }
    }
}
