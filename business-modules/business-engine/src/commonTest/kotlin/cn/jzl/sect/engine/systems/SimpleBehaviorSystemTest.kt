package cn.jzl.sect.engine.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.engine.SectWorld
import kotlin.test.*

class SimpleBehaviorSystemTest : EntityRelationContext {
    override lateinit var world: World

    @BeforeTest
    fun setup() {
        world = SectWorld.create("Test Sect")
    }

    @Test
    fun testUpdateRuns() {
        val system = SimpleBehaviorSystem(world)
        system.update(1.0f)
        // 只验证不抛异常
        assert(true)
    }
}
