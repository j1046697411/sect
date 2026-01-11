package cn.jzl.ecs.relation

import cn.jzl.ecs.world
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RelationSystemTest {

    @Test
    fun `test relation creation and addition`() {
        val world = world {}
        val entity1 = world.entity {}
        val entity2 = world.entity {}

        val relation = Relation(comps.id<TestComponent>(), entity2)
        world.entity(entity1) {
            addRelation(relation)
        }

        assertTrue(world.relationService.hasRelation(entity1, relation))
    }

    @Test
    fun `test relation removal`() {
        val world = world {}
        val entity1 = world.entity {}
        val entity2 = world.entity {}

        val relation = Relation(comps.id<TestComponent>(), entity2)
        world.entity(entity1) {
            addRelation(relation)
        }

        assertTrue(world.relationService.hasRelation(entity1, relation))

        world.entity(entity1) {
            removeRelation(relation)
        }

        assertFalse(world.relationService.hasRelation(entity1, relation))
    }

    @Test
    fun `test single relation replacement`() {
        val world = world {}
        val entity1 = world.entity {}
        val entity2 = world.entity {}
        val entity3 = world.entity {}

        val relation1 = Relation(comps.id<TestComponent>(), entity2)
        val relation2 = Relation(comps.id<TestComponent>(), entity3)

        world.entity(entity1) {
            addRelation(relation1)
        }

        assertTrue(world.relationService.hasRelation(entity1, relation1))

        world.entity(entity1) {
            addRelation(relation2)
        }

        assertFalse(world.relationService.hasRelation(entity1, relation1))
        assertTrue(world.relationService.hasRelation(entity1, relation2))
    }

    @Test
    fun `test relation with data`() {
        val world = world {}
        val entity1 = world.entity {}
        val entity2 = world.entity {}

        val relation = Relation(comps.id<TestComponent>(), entity2)
        val testData = TestData("test data")

        world.entity(entity1) {
            addRelation(relation, testData)
        }

        val retrievedData = world.relationService.getRelation(entity1, relation)
        assertNotNull(retrievedData)
        assertTrue(retrievedData is TestData)
        assertEquals("test data", (retrievedData as TestData).value)
    }

    @Test
    fun `test multiple relations`() {
        val world = world {}
        val entity1 = world.entity {}
        val entity2 = world.entity {}
        val entity3 = world.entity {}

        val relation1 = Relation(comps.id<TestComponent>(), entity2)
        val relation2 = Relation(comps.id<AnotherComponent>(), entity3)

        world.entity(entity1) {
            addRelation(relation1)
            addRelation(relation2)
        }

        assertTrue(world.relationService.hasRelation(entity1, relation1))
        assertTrue(world.relationService.hasRelation(entity1, relation2))
    }

    @Test
    fun `test archetype migration`() {
        val world = world {}
        val entity1 = world.entity {}
        val entity2 = world.entity {}
        val entity3 = world.entity {}

        val relation1 = Relation(comps.id<TestComponent>(), entity2)

        world.entity(entity1) {
            addRelation(relation1)
        }

        assertTrue(world.relationService.hasRelation(entity1, relation1))

        val relation2 = Relation(comps.id<AnotherComponent>(), entity3)
        world.entity(entity1) {
            addRelation(relation2)
        }

        assertTrue(world.relationService.hasRelation(entity1, relation1))
        assertTrue(world.relationService.hasRelation(entity1, relation2))
    }

    @Test
    fun `test component index retrieval`() {
        val world = world {}
        val entity1 = world.entity {}
        val entity2 = world.entity {}

        val relation = Relation(comps.id<TestComponent>(), entity2)
        world.entity(entity1) {
            addRelation(relation)
        }

        val componentIndex = world.relationService.getComponentIndex(entity1, relation)
        assertNotNull(componentIndex)
        assertEquals(entity1, componentIndex.entity)
    }

    @Test
    fun `test relation data update`() {
        val world = world {}
        val entity1 = world.entity {}
        val entity2 = world.entity {}

        val relation = Relation(comps.id<TestComponent>(), entity2)
        val testData1 = TestData("data1")

        world.entity(entity1) {
            addRelation(relation, testData1)
        }

        var retrievedData = world.relationService.getRelation(entity1, relation)
        assertEquals("data1", (retrievedData as TestData).value)

        val testData2 = TestData("data2")
        world.entity(entity1) {
            addRelation(relation, testData2)
        }

        retrievedData = world.relationService.getRelation(entity1, relation)
        assertEquals("data2", (retrievedData as TestData).value)
    }

    @Test
    fun `test non-existent relation`() {
        val world = world {}
        val entity1 = world.entity {}
        val entity2 = world.entity {}

        val relation = Relation(comps.id<TestComponent>(), entity2)

        assertFalse(world.relationService.hasRelation(entity1, relation))
        assertNull(world.relationService.getRelation(entity1, relation))
        assertNull(world.relationService.getComponentIndex(entity1, relation))
    }

    @Test
    fun `test empty relation operations`() {
        val world = world {}
        val entity1 = world.entity {}

        val editor = cn.jzl.ecs.entity.BatchEntityEditor(world, entity1)
        editor.apply(world, true)

        assertTrue(world.isActive(entity1))
    }

    @Test
    fun `test batch relation operations`() {
        val world = world {}
        val entity1 = world.entity {}
        val entity2 = world.entity {}
        val entity3 = world.entity {}

        val relation1 = Relation(comps.id<TestComponent>(), entity2)
        val relation2 = Relation(comps.id<AnotherComponent>(), entity3)

        val editor = cn.jzl.ecs.entity.BatchEntityEditor(world, entity1)
        editor.addRelation(entity1, relation1)
        editor.addRelation(entity1, relation2)
        editor.apply(world, true)

        assertTrue(world.relationService.hasRelation(entity1, relation1))
        assertTrue(world.relationService.hasRelation(entity1, relation2))
    }

    @Test
    fun `test relation data migration`() {
        val world = world {}
        val entity1 = world.entity {}
        val entity2 = world.entity {}
        val entity3 = world.entity {}

        val relation1 = Relation(comps.id<TestComponent>(), entity2)
        val testData1 = TestData("data1")

        world.entity(entity1) {
            addRelation(relation1, testData1)
        }

        val relation2 = Relation(comps.id<AnotherComponent>(), entity3)
        val testData2 = TestData("data2")

        world.entity(entity1) {
            addRelation(relation2, testData2)
        }

        val retrievedData1 = world.relationService.getRelation(entity1, relation1)
        val retrievedData2 = world.relationService.getRelation(entity1, relation2)

        assertEquals("data1", (retrievedData1 as TestData).value)
        assertEquals("data2", (retrievedData2 as TestData).value)
    }

    @Test
    fun `test relation removal with data migration`() {
        val world = world {}
        val entity1 = world.entity {}
        val entity2 = world.entity {}
        val entity3 = world.entity {}

        val relation1 = Relation(comps.id<TestComponent>(), entity2)
        val testData1 = TestData("data1")

        val relation2 = Relation(comps.id<AnotherComponent>(), entity3)
        val testData2 = TestData("data2")

        world.entity(entity1) {
            addRelation(relation1, testData1)
            addRelation(relation2, testData2)
        }

        world.entity(entity1) {
            removeRelation(relation1)
        }

        assertFalse(world.relationService.hasRelation(entity1, relation1))
        assertTrue(world.relationService.hasRelation(entity1, relation2))

        val retrievedData2 = world.relationService.getRelation(entity1, relation2)
        assertEquals("data2", (retrievedData2 as TestData).value)
    }

    @Test
    fun `test relation ordering`() {
        val world = world {}
        val entity1 = world.entity {}
        val entity2 = world.entity {}
        val entity3 = world.entity {}

        val relation1 = Relation(comps.id<TestComponent>(), entity2)
        val relation2 = Relation(comps.id<AnotherComponent>(), entity3)

        world.entity(entity1) {
            addRelation(relation2)
            addRelation(relation1)
        }

        val componentIndex1 = world.relationService.getComponentIndex(entity1, relation1)
        val componentIndex2 = world.relationService.getComponentIndex(entity1, relation2)

        assertNotNull(componentIndex1)
        assertNotNull(componentIndex2)
    }

    @Test
    fun `test relation with same kind different target`() {
        val world = world {}
        val entity1 = world.entity {}
        val entity2 = world.entity {}
        val entity3 = world.entity {}

        val relation1 = Relation(comps.id<TestComponent>(), entity2)
        val relation2 = Relation(comps.id<TestComponent>(), entity3)

        world.entity(entity1) {
            addRelation(relation1)
            addRelation(relation2)
        }

        assertTrue(world.relationService.hasRelation(entity1, relation1))
        assertTrue(world.relationService.hasRelation(entity1, relation2))
    }

    @Test
    fun `test relation data preservation during migration`() {
        val world = world {}
        val entity1 = world.entity {}
        val entity2 = world.entity {}

        val relation = Relation(comps.id<TestComponent>(), entity2)
        val testData = TestData("test data")

        world.entity(entity1) {
            addRelation(relation, testData)
        }

        val editor = cn.jzl.ecs.entity.BatchEntityEditor(world, entity1)
        editor.addRelation(entity1, relation, TestData("updated data"))
        editor.apply(world, true)

        val retrievedData = world.relationService.getRelation(entity1, relation)
        assertEquals("updated data", (retrievedData as TestData).value)
    }

    @Test
    fun `test relation with null target`() {
        val world = world {}
        val entity1 = world.entity {}

        val relation = Relation(comps.id<TestComponent>(), cn.jzl.ecs.entity.Entity.ENTITY_INVALID)
        world.entity(entity1) {
            addRelation(relation)
        }

        assertTrue(world.relationService.hasRelation(entity1, relation))
    }

    @Test
    fun `test relation index bounds`() {
        val world = world {}
        val entity1 = world.entity {}
        val entity2 = world.entity {}
        val entity3 = world.entity {}

        val relation1 = Relation(comps.id<TestComponent>(), entity2)
        val relation2 = Relation(comps.id<AnotherComponent>(), entity3)

        world.entity(entity1) {
            addRelation(relation1)
            addRelation(relation2)
        }

        val componentIndex1 = world.relationService.getComponentIndex(entity1, relation1)
        val componentIndex2 = world.relationService.getComponentIndex(entity1, relation2)

        assertNotNull(componentIndex1)
        assertNotNull(componentIndex2)
        assertTrue(componentIndex1.index >= 0)
        assertTrue(componentIndex2.index >= 0)
    }

    @Test
    fun `test relation with multiple entities`() {
        val world = world {}
        val entities = (0..10).map { world.entity {} }
        val targetEntity = world.entity {}

        val relation = Relation(comps.id<TestComponent>(), targetEntity)

        entities.forEach { entity ->
            world.entity(entity) {
                addRelation(relation)
            }
        }

        entities.forEach { entity ->
            assertTrue(world.relationService.hasRelation(entity, relation))
        }
    }

    @Test
    fun `test relation removal from multiple entities`() {
        val world = world {}
        val entities = (0..10).map { world.entity {} }
        val targetEntity = world.entity {}

        val relation = Relation(comps.id<TestComponent>(), targetEntity)

        entities.forEach { entity ->
            world.entity(entity) {
                addRelation(relation)
            }
        }

        entities.forEach { entity ->
            world.entity(entity) {
                removeRelation(relation)
            }
        }

        entities.forEach { entity ->
            assertFalse(world.relationService.hasRelation(entity, relation))
        }
    }

    data class TestData(val value: String)
    data class AnotherComponent(val value: Int = 0)
    data class TestComponent(val value: String = "")
}
