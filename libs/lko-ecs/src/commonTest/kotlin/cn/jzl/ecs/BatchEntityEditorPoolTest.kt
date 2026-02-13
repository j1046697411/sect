package cn.jzl.ecs

import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.BatchEntityEditor
import cn.jzl.ecs.entity.BatchEntityEditorPool
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.entity.hasComponent
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchEntityEditorPoolTest : EntityRelationContext {
    
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
    fun testBatchEntityEditorPool_obtain() {
        val pool = BatchEntityEditorPool(world)
        val entity = world.entity { }
        
        val editor = pool.obtain(entity)
        
        assertTrue(editor is BatchEntityEditor)
    }

    @Test
    fun testBatchEntityEditorPool_release() {
        val pool = BatchEntityEditorPool(world)
        val entity = world.entity { }
        
        val editor = pool.obtain(entity)
        pool.release(editor)
    }

    @Test
    fun testBatchEntityEditorPool_reuse() {
        val pool = BatchEntityEditorPool(world)
        val entity1 = world.entity { }
        val entity2 = world.entity { }
        
        val editor1 = pool.obtain(entity1)
        pool.release(editor1)
        
        val editor2 = pool.obtain(entity2)
        
        assertTrue(editor2 is BatchEntityEditor)
    }

    @Test
    fun testBatchEntityEditor_apply() {
        val entity = world.entity {
            it.addComponent(Position(1, 2))
        }
        
        assertTrue(entity.hasComponent<Position>())
    }
    
    private data class Position(val x: Int, val y: Int)
}
