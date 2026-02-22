package cn.jzl.sect.facility.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.engine.SectWorld
import kotlin.test.*

/**
 * 宗门状态系统测试
 */
class SectStatusSystemTest : EntityRelationContext {
    override lateinit var world: World

    @BeforeTest
    fun setup() {
        world = SectWorld.create("测试宗门")
    }

    @Test
    fun testStatusIsOperational() {
        // Given: 使用默认创建的宗门
        val system = SectStatusSystem(world)

        // When: 检查宗门状态
        val status = system.checkSectStatus()

        // Then: 应该返回可运营状态（正常或警告）
        assertTrue(status.isOperational(), "新创建的宗门应该处于可运营状态，实际状态为: $status")
    }

    @Test
    fun testFinancialSummary() {
        // Given: 使用默认创建的宗门
        val system = SectStatusSystem(world)

        // When: 获取财务摘要
        val summary = system.getFinancialSummary()

        // Then: 财务数据应该存在
        assertTrue(summary.spiritStones >= 0, "灵石数量应该非负")
        assertTrue(summary.totalDisciples >= 0, "弟子总数应该非负")
    }

    @Test
    fun testSectStatusEnumValues() {
        // Then: 状态枚举值应该正确
        assertEquals(SectStatus.NORMAL, SectStatus.valueOf("NORMAL"))
        assertEquals(SectStatus.WARNING, SectStatus.valueOf("WARNING"))
        assertEquals(SectStatus.CRITICAL, SectStatus.valueOf("CRITICAL"))
        assertEquals(SectStatus.DISSOLVED, SectStatus.valueOf("DISSOLVED"))
        assertEquals(SectStatus.NO_SECT, SectStatus.valueOf("NO_SECT"))
    }

    @Test
    fun testSectStatusIsOperational() {
        // Then: 运营状态判断应该正确
        assertTrue(SectStatus.NORMAL.isOperational(), "正常状态应该可运营")
        assertTrue(SectStatus.WARNING.isOperational(), "警告状态应该可运营")
        assertFalse(SectStatus.CRITICAL.isOperational(), "危急状态不可运营")
        assertFalse(SectStatus.DISSOLVED.isOperational(), "已解散状态不可运营")
        assertFalse(SectStatus.NO_SECT.isOperational(), "无宗门状态不可运营")
    }

    @Test
    fun testSectStatusDisplayNames() {
        // Then: 状态显示名称应该正确
        assertEquals("正常", SectStatus.NORMAL.displayName)
        assertEquals("警告", SectStatus.WARNING.displayName)
        assertEquals("危急", SectStatus.CRITICAL.displayName)
        assertEquals("已解散", SectStatus.DISSOLVED.displayName)
        assertEquals("无宗门", SectStatus.NO_SECT.displayName)
    }

    @Test
    fun testFinancialSummaryToDisplayString() {
        // Given: 创建一个财务摘要
        val summary = FinancialSummary(
            spiritStones = 10000L,
            contributionPoints = 500L,
            monthlyCost = 150L,
            canSurviveMonths = 66L,
            totalDisciples = 10,
            rebelliousDisciples = 2
        )

        // When: 转换为显示字符串
        val displayString = summary.toDisplayString()

        // Then: 应该包含关键信息
        assertTrue(displayString.contains("宗门财务摘要"), "应该包含标题")
        assertTrue(displayString.contains("10000"), "应该包含灵石数量")
        assertTrue(displayString.contains("10"), "应该包含弟子总数")
        assertTrue(displayString.contains("2"), "应该包含叛逆弟子数量")
    }

    @Test
    fun testFinancialSummaryEmpty() {
        // Given: 空财务摘要
        val summary = FinancialSummary.EMPTY

        // Then: 所有值应该为0
        assertEquals(0L, summary.spiritStones)
        assertEquals(0L, summary.contributionPoints)
        assertEquals(0, summary.totalDisciples)
    }
}
