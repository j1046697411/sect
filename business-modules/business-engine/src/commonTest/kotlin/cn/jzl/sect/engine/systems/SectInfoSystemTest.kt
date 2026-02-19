package cn.jzl.sect.engine.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.engine.SectWorld
import kotlin.test.*

/**
 * 宗门信息系统测试
 */
class SectInfoSystemTest : EntityRelationContext {
    override lateinit var world: World

    @BeforeTest
    fun setup() {
        world = SectWorld.create("青云宗")
    }

    @Test
    fun testSystemInitialization() {
        val system = SectInfoSystem(world)
        assertNotNull(system)
    }

    @Test
    fun testGetSectOverview() {
        val system = SectInfoSystem(world)
        val overview = system.getSectOverview()

        assertNotNull(overview)
        assertEquals("青云宗", overview.sectName, "宗门名称应该正确")
        assertEquals(1, overview.foundedYear, "创立年份应该正确")
        assertEquals(1000L, overview.spiritStones, "初始灵石应该为1000")
        assertEquals(0L, overview.contributionPoints, "初始贡献点应该为0")
    }

    @Test
    fun testDiscipleCount() {
        val system = SectInfoSystem(world)
        val overview = system.getSectOverview()

        // 1掌门 + 2长老 + 5外门弟子 = 8人
        assertEquals(8, overview.discipleCount, "弟子总数应该是8人")
        assertEquals(1, overview.leaderCount, "掌门应该是1人")
        assertEquals(2, overview.elderCount, "长老应该是2人")
        assertEquals(5, overview.discipleOuterCount, "外门弟子应该是5人")
    }

    @Test
    fun testGetDiscipleList() {
        val system = SectInfoSystem(world)
        val disciples = system.getDiscipleList()

        assertEquals(8, disciples.size, "弟子列表应该有8人")

        // 验证按职务排序（掌门在前）
        val firstDisciple = disciples.first()
        assertEquals(
            cn.jzl.sect.core.sect.Position.LEADER,
            firstDisciple.position,
            "第一个应该是掌门"
        )
    }

    @Test
    fun testDiscipleInfoContent() {
        val system = SectInfoSystem(world)
        val disciples = system.getDiscipleList()
        val leader = disciples.first { it.position == cn.jzl.sect.core.sect.Position.LEADER }

        assertEquals(cn.jzl.sect.core.cultivation.Realm.FOUNDATION, leader.realm, "掌门应该是筑基期")
        assertEquals(5, leader.layer, "掌门应该是筑基期5层")
        assertEquals(80, leader.age, "掌门年龄应该是80岁")
    }

    @Test
    fun testOverviewDisplayString() {
        val system = SectInfoSystem(world)
        val overview = system.getSectOverview()
        val displayString = overview.toDisplayString()

        assertTrue(displayString.contains("青云宗"), "应该包含宗门名称")
        assertTrue(displayString.contains("资源状况"), "应该包含资源状况")
        assertTrue(displayString.contains("弟子统计"), "应该包含弟子统计")
    }

    @Test
    fun testDiscipleInfoDisplayString() {
        val system = SectInfoSystem(world)
        val disciples = system.getDiscipleList()
        val displayString = disciples.first().toDisplayString()

        assertTrue(displayString.contains("掌门"), "应该包含职务")
        assertTrue(displayString.contains("筑基"), "应该包含境界")
    }
}
