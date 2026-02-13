package cn.jzl.ecs

import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.Entities
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.hasComponent
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EntitiesTest : EntityRelationContext {
    
    override lateinit var world: World
    
    @BeforeTest
    fun setup() {
        world = world {
            install(testAddon)
        }
    }

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<Position>()
        }
    }

    @Test
    fun testEntities_empty() {
        val entities = Entities()
        
        assertTrue(entities.isEmpty())
        assertEquals(0, entities.size)
    }

    @Test
    fun testEntities_add() {
        val entities = Entities()
        val entity = world.entity { }
        
        entities.add(entity)
        
        assertEquals(1, entities.size)
        assertFalse(entities.isEmpty())
    }

    @Test
    fun testEntities_get() {
        val entities = Entities()
        val entity1 = world.entity { }
        val entity2 = world.entity { }
        
        entities.add(entity1)
        entities.add(entity2)
        
        assertEquals(entity1.data, entities[0].data)
        assertEquals(entity2.data, entities[1].data)
    }

    @Test
    fun testEntities_set() {
        val entities = Entities()
        val entity1 = world.entity { }
        val entity2 = world.entity { }
        
        entities.add(entity1)
        entities[0] = entity2
        
        assertEquals(entity2.data, entities[0].data)
    }

    @Test
    fun testEntities_contains() {
        val entities = Entities()
        val entity = world.entity { }
        
        entities.add(entity)
        
        assertTrue(entities.contains(entity))
    }

    @Test
    fun testEntities_containsAll() {
        val entities = Entities()
        val entity1 = world.entity { }
        val entity2 = world.entity { }
        
        entities.add(entity1)
        entities.add(entity2)
        
        assertTrue(entities.containsAll(listOf(entity1, entity2)))
    }

    @Test
    fun testEntities_removeAt() {
        val entities = Entities()
        val entity1 = world.entity { }
        val entity2 = world.entity { }
        
        entities.add(entity1)
        entities.add(entity2)
        
        val removed = entities.removeAt(0)
        
        assertEquals(1, entities.size)
    }

    @Test
    fun testEntities_iterator() {
        val entities = Entities()
        val entity1 = world.entity { }
        val entity2 = world.entity { }
        
        entities.add(entity1)
        entities.add(entity2)
        
        var count = 0
        entities.forEach { count++ }
        
        assertEquals(2, count)
    }
    
    private data class Position(val x: Int, val y: Int)
}
