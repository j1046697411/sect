package cn.jzl.sect.engine

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.family.component
import cn.jzl.ecs.query
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.cultivation.CultivationComponent
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.sect.Position
import cn.jzl.sect.core.sect.PositionComponent
import cn.jzl.sect.core.sect.SectComponent
import cn.jzl.sect.core.sect.SectResourceComponent
import cn.jzl.sect.core.facility.FacilityComponent
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.core.disciple.AttributeComponent
import cn.jzl.ecs.family.FamilyBuilder
import kotlin.test.*

class SectWorldTest : EntityRelationContext {
    override lateinit var world: World

    @BeforeTest
    fun setup() {
        world = SectWorld.create("青云宗")
    }

    @Test
    fun testSectCreation() {
        val query = world.query { SectQueryContext(world) }
        var count = 0
        query.forEach { count++ }
        assertEquals(1, count, "Should have one sect entity")
    }

    @Test
    fun testInitialDisciples() {
        val query = world.query { DiscipleQueryContext(world) }
        var count = 0
        query.forEach { count++ }
        assertTrue(count >= 5, "Should have at least 5 initial disciples, found $count")
    }

    @Test
    fun testLeaderExists() {
        val query = world.query { LeaderQueryContext(world) }
        var count = 0
        query.forEach {
            if (it.position.position == Position.LEADER) count++
        }
        assertEquals(1, count, "Should have one leader")
    }

    @Test
    fun testInitialResources() {
        val query = world.query { ResourceQueryContext(world) }
        var count = 0
        query.forEach {
            count++
            assertEquals(1000L, it.sectResource.spiritStones)
        }
        assertEquals(1, count, "Should have resource component")
    }

    @Test
    fun testInitialFacilities() {
        val query = world.query { FacilityQueryContext(world) }
        var cultivationRoom = 0
        var dormitory = 0
        query.forEach {
            when (it.facility.type) {
                FacilityType.CULTIVATION_ROOM -> cultivationRoom++
                FacilityType.DORMITORY -> dormitory++
                else -> {}
            }
        }
        assertEquals(1, cultivationRoom, "Should have 1 cultivation room")
        assertEquals(1, dormitory, "Should have 1 dormitory")
    }

    class SectQueryContext(world: World) : EntityQueryContext(world) {
        val sectComponent: SectComponent by component()
    }

    class DiscipleQueryContext(world: World) : EntityQueryContext(world) {
        val position: PositionComponent by component()
        val cultivation: CultivationComponent by component()
    }

    class LeaderQueryContext(world: World) : EntityQueryContext(world) {
        val position: PositionComponent by component()
        override fun FamilyBuilder.configure() {
            component<PositionComponent>()
        }
    }

    class ResourceQueryContext(world: World) : EntityQueryContext(world) {
        val sectResource: SectResourceComponent by component()
    }

    class FacilityQueryContext(world: World) : EntityQueryContext(world) {
        val facility: FacilityComponent by component()
    }
}
