package cn.jzl.ecs

import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.GeneralComponentStore
import cn.jzl.ecs.component.ShadedComponentService
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.relation.kind
import cn.jzl.ecs.relation.target
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class GeneralComponentStoreTest : EntityRelationContext {
    
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
    fun testGeneralComponentStore_size() {
        val store = GeneralComponentStore<Position>()
        
        assertEquals(0, store.size)
        
        store.add(Position(1, 2))
        assertEquals(1, store.size)
        
        store.add(Position(3, 4))
        assertEquals(2, store.size)
    }

    @Test
    fun testGeneralComponentStore_get() {
        val store = GeneralComponentStore<Position>()
        store.add(Position(1, 2))
        store.add(Position(3, 4))
        
        assertEquals(Position(1, 2), store.get(0))
        assertEquals(Position(3, 4), store.get(1))
    }

    @Test
    fun testGeneralComponentStore_set() {
        val store = GeneralComponentStore<Position>()
        store.add(Position(1, 2))
        
        store.set(0, Position(10, 20))
        
        assertEquals(Position(10, 20), store.get(0))
    }

    @Test
    fun testGeneralComponentStore_removeAt() {
        val store = GeneralComponentStore<Position>()
        store.add(Position(1, 2))
        store.add(Position(3, 4))
        store.add(Position(5, 6))
        
        val removed = store.removeAt(1)
        
        assertEquals(Position(3, 4), removed)
        assertEquals(2, store.size)
        assertEquals(Position(1, 2), store.get(0))
        assertEquals(Position(5, 6), store.get(1))
    }

    @Test
    fun testShadedComponentService_get() {
        val shadedService = ShadedComponentService(world)
        
        val positionId = world.components.id<Position>()
        val relation = Relation(positionId, world.components.sharedOf)
        
        shadedService[relation] = Position(100, 200)
        
        assertEquals(Position(100, 200), shadedService[relation])
    }

    @Test
    fun testShadedComponentService_getNotSet() {
        val shadedService = ShadedComponentService(world)
        
        val positionId = world.components.id<Position>()
        val relation = Relation(positionId, world.components.sharedOf)
        
        assertNull(shadedService[relation])
    }

    @Test
    fun testShadedComponentService_set() {
        val shadedService = ShadedComponentService(world)
        
        val positionId = world.components.id<Position>()
        val relation = Relation(positionId, world.components.sharedOf)
        
        shadedService[relation] = Position(50, 60)
        shadedService[relation] = Position(70, 80)
        
        assertEquals(Position(70, 80), shadedService[relation])
    }

    private data class Position(val x: Int, val y: Int)
}
