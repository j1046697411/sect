package cn.jzl.ecs

import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.BucketedLongArray
import cn.jzl.ecs.entity.EntityRelationContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BucketedLongArrayTest : EntityRelationContext {
    
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
    fun testBucketedLongArray_setAndGet() {
        val array = BucketedLongArray(1024)
        
        array[0] = 100L
        array[1] = 200L
        
        assertEquals(100L, array[0])
        assertEquals(200L, array[1])
    }

    @Test
    fun testBucketedLongArray_size() {
        val array = BucketedLongArray(1024)
        
        assertEquals(0, array.size)
        
        array[0] = 1L
        assertEquals(1, array.size)
        
        array[5] = 5L
        assertEquals(6, array.size)
    }

    @Test
    fun testBucketedLongArray_bucketOverflow() {
        val array = BucketedLongArray(3)
        
        array[0] = 0L
        array[1] = 1L
        array[2] = 2L
        array[3] = 3L
        array[4] = 4L
        
        assertEquals(5, array.size)
        assertEquals(0L, array[0])
        assertEquals(4L, array[4])
    }

    @Test
    fun testBucketedLongArray_ensureSize() {
        val array = BucketedLongArray(2)
        
        array[0] = 0L
        array.ensureSize(10)
        
        assertTrue(array.size >= 1)
    }
    
    private data class Position(val x: Int, val y: Int)
}
