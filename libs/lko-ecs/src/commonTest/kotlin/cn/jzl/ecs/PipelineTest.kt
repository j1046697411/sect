package cn.jzl.ecs

import cn.jzl.ecs.addon.Phase
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.world
import kotlin.test.Test
import kotlin.test.assertEquals

class PipelineTest : EntityRelationContext {
    override val world: World by lazy {
        world {  }
    }

    @Test
    fun testPipelineRunOnOrAfter() {
        val pipeline = PipelineImpl(world)
        var executed = false
        
        pipeline.runOnOrAfter(Phase.INIT_ENTITIES) {
            executed = true
        }
        
        // Task is registered but not yet executed
        assertEquals(false, executed)
    }

    @Test
    fun testPipelineRunStartupTasks() {
        val pipeline = PipelineImpl(world)
        var executionCount = 0
        
        pipeline.runOnOrAfter(Phase.INIT_ENTITIES) {
            executionCount++
        }
        
        pipeline.runStartupTasks()
        
        assertEquals(1, executionCount)
    }

    @Test
    fun testPipelineMultipleTasksInSamePhase() {
        val pipeline = PipelineImpl(world)
        var count1 = 0
        var count2 = 0
        
        pipeline.runOnOrAfter(Phase.INIT_SYSTEMS) {
            count1++
        }
        
        pipeline.runOnOrAfter(Phase.INIT_SYSTEMS) {
            count2++
        }
        
        pipeline.runStartupTasks()
        
        assertEquals(1, count1)
        assertEquals(1, count2)
    }

    @Test
    fun testPipelineTasksExecutedInPhaseOrder() {
        val pipeline = PipelineImpl(world)
        val executionOrder = mutableListOf<Int>()
        
        pipeline.runOnOrAfter(Phase.INIT_COMPONENTS) {
            executionOrder.add(1)
        }
        
        pipeline.runOnOrAfter(Phase.INIT_ENTITIES) {
            executionOrder.add(2)
        }
        
        pipeline.runStartupTasks()
        
        assertEquals(2, executionOrder.size)
        assertEquals(1, executionOrder[0])
        assertEquals(2, executionOrder[1])
    }

    @Test
    fun testPipelineEmptyPhaseSkipped() {
        val pipeline = PipelineImpl(world)
        var executed = false
        
        // Register task in a phase with no tasks before it
        pipeline.runOnOrAfter(Phase.ENABLE) {
            executed = true
        }
        
        pipeline.runStartupTasks()
        
        assertEquals(true, executed)
    }
}
