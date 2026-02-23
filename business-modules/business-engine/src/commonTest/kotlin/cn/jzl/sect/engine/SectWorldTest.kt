package cn.jzl.sect.engine

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.family.component
import cn.jzl.ecs.query
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.cultivation.components.CultivationProgress
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.Sect
import cn.jzl.sect.core.sect.SectTreasury
import cn.jzl.sect.facility.components.Facility
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.core.vitality.Vitality
import cn.jzl.sect.core.vitality.Spirit
import cn.jzl.sect.cultivation.components.Talent
import cn.jzl.sect.core.disciple.SectLoyalty
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
            if (it.position.position == SectPositionType.LEADER) count++
        }
        assertEquals(1, count, "Should have one leader")
    }

    @Test
    fun testInitialResources() {
        val query = world.query { ResourceQueryContext(world) }
        var count = 0
        query.forEach {
            count++
            assertEquals(1000L, it.sectTreasury.spiritStones)
        }
        assertEquals(1, count, "Should have treasury component")
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
        val sectComponent: Sect by component()
    }

    class DiscipleQueryContext(world: World) : EntityQueryContext(world) {
        val position: SectPositionInfo by component()
        val cultivation: CultivationProgress by component()
    }

    class LeaderQueryContext(world: World) : EntityQueryContext(world) {
        val position: SectPositionInfo by component()
        override fun FamilyBuilder.configure() {
            component<SectPositionInfo>()
        }
    }

    class ResourceQueryContext(world: World) : EntityQueryContext(world) {
        val sectTreasury: SectTreasury by component()
    }

    class FacilityQueryContext(world: World) : EntityQueryContext(world) {
        val facility: Facility by component()
    }
}
