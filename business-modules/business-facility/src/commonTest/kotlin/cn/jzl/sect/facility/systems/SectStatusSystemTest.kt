package cn.jzl.sect.facility.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.entity.entity
import cn.jzl.sect.core.ai.BehaviorState
import cn.jzl.sect.core.ai.BehaviorType
import cn.jzl.sect.core.cultivation.Cultivation
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.disciple.Loyalty
import cn.jzl.sect.core.sect.Position
import cn.jzl.sect.core.sect.Sect
import cn.jzl.sect.core.sect.SectPosition
import cn.jzl.sect.core.sect.SectResource
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

    // ==================== 宗门状态检测测试 ====================

    @Test
    fun testNormalStatus() {
        // Given: 正常状态的宗门（有足够资源，弟子忠诚）
        val system = SectStatusSystem(world)

        // When: 检查宗门状态
        val status = system.checkSectStatus()

        // Then: 应该返回正常状态
        assertEquals(SectStatus.NORMAL, status, "资源充足且弟子忠诚时应该为正常状态")
    }

    @Test
    fun testWarningStatusByRebelliousRatio() {
        // Given: 创建一个超过1/4弟子有叛逃风险的宗门
        world = createWorldWithRebelliousDisciples(3, 8) // 3/8 > 0.25
        val system = SectStatusSystem(world)

        // When: 检查宗门状态
        val status = system.checkSectStatus()

        // Then: 应该返回警告状态
        assertEquals(SectStatus.WARNING, status, "超过1/4弟子有叛逃风险时应该为警告状态")
    }

    @Test
    fun testCriticalStatusByHighRebelliousRatio() {
        // Given: 超过半数弟子有叛逃风险的宗门
        world = createWorldWithRebelliousDisciples(5, 8) // 5/8 > 0.5
        val system = SectStatusSystem(world)

        // When: 检查宗门状态
        val status = system.checkSectStatus()

        // Then: 应该返回危急状态
        assertEquals(SectStatus.CRITICAL, status, "超过半数弟子有叛逃风险时应该为危急状态")
    }

    @Test
    fun testNoSectStatus() {
        // Given: 没有宗门实体的世界
        world = createWorldWithoutSect()
        val system = SectStatusSystem(world)

        // When: 检查宗门状态
        val status = system.checkSectStatus()

        // Then: 应该返回无宗门状态
        assertEquals(SectStatus.NO_SECT, status, "没有宗门实体时应该为无宗门状态")
    }

    // ==================== 财务摘要测试 ====================

    @Test
    fun testFinancialSummaryBasic() {
        // Given: 正常宗门
        val system = SectStatusSystem(world)

        // When: 获取财务摘要
        val summary = system.getFinancialSummary()

        // Then: 验证基本字段
        assertEquals(1000L, summary.spiritStones, "灵石储备应该正确")
        assertEquals(0L, summary.contributionPoints, "贡献点应该正确")
        assertEquals(8, summary.totalDisciples, "弟子总数应该正确")
        assertEquals(0, summary.rebelliousDisciples, "叛逆弟子数应该为0")
    }

    @Test
    fun testFinancialSummaryMonthlyCost() {
        // Given: 正常宗门
        val system = SectStatusSystem(world)

        // When: 获取财务摘要
        val summary = system.getFinancialSummary()

        // Then: 验证月度支出
        // 掌门500 + 2名长老300*2 + 5名外门弟子30*5 = 500 + 600 + 150 = 1250
        assertEquals(1250L, summary.monthlyCost, "月度支出计算应该正确")
    }

    @Test
    fun testFinancialSummarySurvivalMonths() {
        // Given: 正常宗门
        val system = SectStatusSystem(world)

        // When: 获取财务摘要
        val summary = system.getFinancialSummary()

        // Then: 验证可维持月数
        // 1000 / 1250 = 0个月（整数除法）
        assertEquals(0L, summary.canSurviveMonths, "可维持月数应该正确")
    }

    @Test
    fun testFinancialSummaryWithRebelliousDisciples() {
        // Given: 有叛逆弟子的宗门
        world = createWorldWithRebelliousDisciples(2, 6)
        val system = SectStatusSystem(world)

        // When: 获取财务摘要
        val summary = system.getFinancialSummary()

        // Then: 验证叛逆弟子数
        assertEquals(6, summary.totalDisciples, "弟子总数应该正确")
        assertEquals(2, summary.rebelliousDisciples, "叛逆弟子数应该正确")
    }

    @Test
    fun testFinancialSummaryEmpty() {
        // Given: 没有宗门实体的世界
        world = createWorldWithoutSect()
        val system = SectStatusSystem(world)

        // When: 获取财务摘要
        val summary = system.getFinancialSummary()

        // Then: 应该返回空摘要
        assertEquals(FinancialSummary.EMPTY, summary, "没有宗门时应该返回空摘要")
    }

    @Test
    fun testFinancialSummaryDisplayString() {
        // Given: 正常宗门
        val summary = FinancialSummary(
            spiritStones = 1000L,
            contributionPoints = 500L,
            monthlyCost = 100L,
            canSurviveMonths = 10L,
            totalDisciples = 8,
            rebelliousDisciples = 2
        )

        // When: 获取显示字符串
        val display = summary.toDisplayString()

        // Then: 验证显示内容
        assertTrue(display.contains("灵石储备: 1000"), "应该包含灵石储备")
        assertTrue(display.contains("贡献点: 500"), "应该包含贡献点")
        assertTrue(display.contains("月度支出: 100"), "应该包含月度支出")
        assertTrue(display.contains("可维持: 10个月"), "应该包含可维持月数")
        assertTrue(display.contains("弟子总数: 8"), "应该包含弟子总数")
        assertTrue(display.contains("叛逆风险: 2"), "应该包含叛逆风险")
    }

    @Test
    fun testFinancialSummaryDisplayStringInfinite() {
        // Given: 无支出的宗门
        val summary = FinancialSummary(
            spiritStones = 1000L,
            contributionPoints = 0L,
            monthlyCost = 0L,
            canSurviveMonths = Long.MAX_VALUE,
            totalDisciples = 0,
            rebelliousDisciples = 0
        )

        // When: 获取显示字符串
        val display = summary.toDisplayString()

        // Then: 验证显示无限期
        assertTrue(display.contains("无限期"), "无支出时应该显示无限期")
    }

    // ==================== 状态枚举测试 ====================

    @Test
    fun testSectStatusIsOperational() {
        // Then: 验证运营状态判断
        assertTrue(SectStatus.NORMAL.isOperational(), "正常状态应该可运营")
        assertTrue(SectStatus.WARNING.isOperational(), "警告状态应该可运营")
        assertFalse(SectStatus.CRITICAL.isOperational(), "危急状态不可运营")
        assertFalse(SectStatus.DISSOLVED.isOperational(), "已解散状态不可运营")
        assertFalse(SectStatus.NO_SECT.isOperational(), "无宗门状态不可运营")
    }

    @Test
    fun testSectStatusDisplayNames() {
        // Then: 验证显示名称
        assertEquals("正常", SectStatus.NORMAL.displayName)
        assertEquals("警告", SectStatus.WARNING.displayName)
        assertEquals("危急", SectStatus.CRITICAL.displayName)
        assertEquals("已解散", SectStatus.DISSOLVED.displayName)
        assertEquals("无宗门", SectStatus.NO_SECT.displayName)
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建有叛逆弟子的测试世界
     */
    private fun createWorldWithRebelliousDisciples(rebelliousCount: Int, totalCount: Int): World {
        val testWorld = SectWorld.create("测试宗门")

        // 添加有叛逃风险的弟子
        repeat(rebelliousCount) {
            testWorld.entity {
                it.addComponent(Cultivation(realm = Realm.QI_REFINING, layer = 1, cultivation = 100L, maxCultivation = 1000L))
                it.addComponent(Position(position = SectPosition.DISCIPLE_OUTER))
                it.addComponent(BehaviorState(currentBehavior = BehaviorType.CULTIVATE))
                // 忠诚度<=10 会叛逃
                it.addComponent(Loyalty(value = 5, consecutiveUnpaidMonths = 0))
            }
        }

        // 添加正常弟子
        repeat(totalCount - rebelliousCount) {
            testWorld.entity {
                it.addComponent(Cultivation(realm = Realm.QI_REFINING, layer = 1, cultivation = 100L, maxCultivation = 1000L))
                it.addComponent(Position(position = SectPosition.DISCIPLE_OUTER))
                it.addComponent(BehaviorState(currentBehavior = BehaviorType.CULTIVATE))
                it.addComponent(Loyalty(value = 80, consecutiveUnpaidMonths = 0))
            }
        }

        return testWorld
    }

    /**
     * 创建无宗门实体的世界
     */
    private fun createWorldWithoutSect(): World {
        // 创建一个没有宗门实体的世界
        // 使用 SectWorld.create 创建后再清除宗门实体
        val testWorld = SectWorld.create("测试宗门")
        // 由于 ECS 不支持删除实体，我们创建一个新世界但不添加宗门实体
        return cn.jzl.ecs.world {
            // 不安装任何 addon，创建一个空世界
        }
    }
}
