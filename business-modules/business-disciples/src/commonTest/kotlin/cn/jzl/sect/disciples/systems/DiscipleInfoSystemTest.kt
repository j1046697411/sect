package cn.jzl.sect.disciples.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.sect.core.ai.CurrentBehavior
import cn.jzl.sect.core.ai.BehaviorType
import cn.jzl.sect.core.cultivation.CultivationProgress
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.cultivation.Talent
import cn.jzl.sect.core.vitality.Vitality
import cn.jzl.sect.core.vitality.Spirit
import cn.jzl.sect.core.disciple.Age
import cn.jzl.sect.core.disciple.SectLoyalty
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.engine.SectWorld
import kotlin.test.*

/**
 * 弟子信息系统测试
 */
class DiscipleInfoSystemTest : EntityRelationContext {
    override lateinit var world: World
    private lateinit var discipleInfoSystem: DiscipleInfoSystem

    @BeforeTest
    fun setup() {
        world = SectWorld.create("测试宗门")
        discipleInfoSystem = DiscipleInfoSystem(world)
    }

    @Test
    fun testGetAllDisciples() {
        // Given: 已初始化的世界包含弟子

        // When: 获取所有弟子
        val disciples = discipleInfoSystem.getAllDisciples()

        // Then: 应该返回所有弟子（1掌门 + 2长老 + 5外门 = 8人）
        assertEquals(8, disciples.size, "应该返回8个弟子")
    }

    @Test
    fun testDisciplesSortedByPosition() {
        // Given: 已初始化的世界

        // When: 获取所有弟子
        val disciples = discipleInfoSystem.getAllDisciples()

        // Then: 弟子应该按职位排序（掌门在前，外门在后）
        assertEquals(SectPositionType.LEADER, disciples[0].position, "第一个应该是掌门")
        assertEquals(SectPositionType.LEADER.sortOrder, disciples[0].position.sortOrder)

        // 验证排序顺序
        for (i in 1 until disciples.size) {
            assertTrue(
                disciples[i].position.sortOrder >= disciples[i - 1].position.sortOrder,
                "弟子应该按职位排序"
            )
        }
    }

    @Test
    fun testDiscipleInfoContent() {
        // Given: 已初始化的世界

        // When: 获取所有弟子
        val disciples = discipleInfoSystem.getAllDisciples()
        val leader = disciples.first { it.position == SectPositionType.LEADER }

        // Then: 掌门信息应该正确
        assertEquals(SectPositionType.LEADER, leader.position)
        assertEquals(Realm.FOUNDATION, leader.realm)
        assertEquals(5, leader.layer)
        assertEquals(5000L, leader.cultivation)
        assertEquals(10000L, leader.maxCultivation)
        assertEquals(70, leader.physique)
        assertEquals(65, leader.comprehension)
        assertEquals(80, leader.age)
        assertEquals(100, leader.loyalty)
        assertEquals(LoyaltyLevel.DEVOTED, leader.loyaltyLevel)
    }

    @Test
    fun testCalculateProgress() {
        // Given: 已初始化的世界

        // When: 获取所有弟子
        val disciples = discipleInfoSystem.getAllDisciples()
        val leader = disciples.first { it.position == SectPositionType.LEADER }

        // Then: 修炼进度应该正确计算（5000/10000 = 0.5）
        assertEquals(0.5f, leader.progress, 0.01f, "掌门修炼进度应该是50%")
    }

    @Test
    fun testGetDisciplesByPosition() {
        // Given: 已初始化的世界

        // When: 按职位筛选弟子
        val leaders = discipleInfoSystem.getDisciplesByPosition(SectPositionType.LEADER)
        val elders = discipleInfoSystem.getDisciplesByPosition(SectPositionType.ELDER)
        val outerDisciples = discipleInfoSystem.getDisciplesByPosition(SectPositionType.DISCIPLE_OUTER)

        // Then: 各职位人数应该正确
        assertEquals(1, leaders.size, "应该有1个掌门")
        assertEquals(2, elders.size, "应该有2个长老")
        assertEquals(5, outerDisciples.size, "应该有5个外门弟子")
    }

    @Test
    fun testGetDisciplesByPositionWithNoResult() {
        // Given: 已初始化的世界（没有内门弟子）

        // When: 查询不存在的职位
        val innerDisciples = discipleInfoSystem.getDisciplesByPosition(SectPositionType.DISCIPLE_INNER)

        // Then: 应该返回空列表
        assertTrue(innerDisciples.isEmpty(), "应该返回空列表")
    }

    @Test
    fun testGetDiscipleStatistics() {
        // Given: 已初始化的世界

        // When: 获取弟子统计
        val stats = discipleInfoSystem.getDiscipleStatistics()

        // Then: 统计数据应该正确
        assertEquals(8, stats.totalCount, "总人数应该是8")
        assertEquals(1, stats.leaderCount, "掌门数应该是1")
        assertEquals(2, stats.elderCount, "长老数应该是2")
        assertEquals(0, stats.innerCount, "内门弟子数应该是0")
        assertEquals(5, stats.outerCount, "外门弟子数应该是5")
    }

    @Test
    fun testStatisticsRealmCounts() {
        // Given: 已初始化的世界

        // When: 获取弟子统计
        val stats = discipleInfoSystem.getDiscipleStatistics()

        // Then: 境界统计应该正确（掌门+2长老=3筑基，5外门=5炼气）
        assertEquals(0, stats.mortalCount, "凡人数量应该是0")
        assertEquals(5, stats.qiRefiningCount, "炼气期数量应该是5")
        assertEquals(3, stats.foundationCount, "筑基期数量应该是3")
    }

    @Test
    fun testStatisticsAverageLoyalty() {
        // Given: 已初始化的世界
        // 掌门: 100, 长老: 90*2=180, 外门: 80*5=400
        // 总和: 680, 平均: 680/8 = 85

        // When: 获取弟子统计
        val stats = discipleInfoSystem.getDiscipleStatistics()

        // Then: 平均忠诚度应该正确
        assertEquals(85, stats.averageLoyalty, "平均忠诚度应该是85")
    }

    @Test
    fun testStatisticsRebelliousCount() {
        // Given: 已初始化的世界（所有弟子忠诚度都>=80，没有叛逆的）

        // When: 获取弟子统计
        val stats = discipleInfoSystem.getDiscipleStatistics()

        // Then: 叛逆弟子数量应该是0
        assertEquals(0, stats.rebelliousCount, "叛逆弟子数量应该是0")
    }

    @Test
    fun testStatisticsWithRebelliousDisciples() {
        // Given: 创建一个包含叛逆弟子的世界
        world.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.MORTAL,
                layer = 1,
                cultivation = 0L,
                maxCultivation = 100L
            ))
            it.addComponent(Talent(
                physique = 30,
                comprehension = 30,
                fortune = 40,
                charm = 45
            ))
            it.addComponent(Vitality(currentHealth = 100, maxHealth = 100))
            it.addComponent(Spirit(currentSpirit = 50, maxSpirit = 50))
            it.addComponent(Age(age = 20))
            it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
            it.addComponent(CurrentBehavior(type = BehaviorType.REST))
            it.addComponent(SectLoyalty(value = 10)) // 叛逆的忠诚度
        }

        // When: 获取弟子统计
        val stats = discipleInfoSystem.getDiscipleStatistics()

        // Then: 叛逆弟子数量应该更新
        assertEquals(9, stats.totalCount, "总人数应该是9")
        assertEquals(1, stats.rebelliousCount, "叛逆弟子数量应该是1")
    }

    @Test
    fun testDiscipleInfoToDisplayString() {
        // Given: 已初始化的世界

        // When: 获取弟子并转换为显示字符串
        val disciples = discipleInfoSystem.getAllDisciples()
        val leader = disciples.first { it.position == SectPositionType.LEADER }
        val displayString = leader.toDisplayString()

        // Then: 显示字符串应该包含关键信息
        assertTrue(displayString.contains("掌门"), "应该包含职位信息")
        assertTrue(displayString.contains("筑基期"), "应该包含境界信息")
        assertTrue(displayString.contains("5层"), "应该包含层数")
        assertTrue(displayString.contains("修为:"), "应该包含修为信息")
        assertTrue(displayString.contains("气血:"), "应该包含气血信息")
        assertTrue(displayString.contains("灵力:"), "应该包含灵力信息")
        assertTrue(displayString.contains("年龄:"), "应该包含年龄信息")
        assertTrue(displayString.contains("忠诚:"), "应该包含忠诚度信息")
    }

    @Test
    fun testStatisticsToDisplayString() {
        // Given: 已初始化的世界

        // When: 获取统计并转换为显示字符串
        val stats = discipleInfoSystem.getDiscipleStatistics()
        val displayString = stats.toDisplayString()

        // Then: 显示字符串应该包含关键统计信息
        assertTrue(displayString.contains("弟子统计:"), "应该包含标题")
        assertTrue(displayString.contains("总人数:"), "应该包含总人数")
        assertTrue(displayString.contains("掌门:"), "应该包含掌门数")
        assertTrue(displayString.contains("长老:"), "应该包含长老数")
        assertTrue(displayString.contains("内门:"), "应该包含内门弟子数")
        assertTrue(displayString.contains("外门:"), "应该包含外门弟子数")
        assertTrue(displayString.contains("平均忠诚:"), "应该包含平均忠诚度")
        assertTrue(displayString.contains("叛逆风险:"), "应该包含叛逆风险")
    }

    @Test
    fun testNewWorldHasInitialDisciples() {
        // Given: 创建一个新世界
        val newWorld = SectWorld.create("新宗门")
        val newSystem = DiscipleInfoSystem(newWorld)

        // When: 获取新世界的弟子统计
        val stats = newSystem.getDiscipleStatistics()

        // Then: 新世界应该包含初始弟子（1掌门 + 2长老 + 5外门 = 8人）
        assertEquals(8, stats.totalCount, "总人数应该是8")
        assertEquals(1, stats.leaderCount, "掌门数应该是1")
        assertEquals(2, stats.elderCount, "长老数应该是2")
        assertEquals(5, stats.outerCount, "外门弟子数应该是5")
    }

    @Test
    fun testLoyaltyLevelEnum() {
        // Given: 不同忠诚度值

        // Then: 应该正确映射到等级
        assertEquals(LoyaltyLevel.DEVOTED, LoyaltyLevel.fromValue(100))
        assertEquals(LoyaltyLevel.DEVOTED, LoyaltyLevel.fromValue(80))
        assertEquals(LoyaltyLevel.LOYAL, LoyaltyLevel.fromValue(79))
        assertEquals(LoyaltyLevel.LOYAL, LoyaltyLevel.fromValue(60))
        assertEquals(LoyaltyLevel.NEUTRAL, LoyaltyLevel.fromValue(59))
        assertEquals(LoyaltyLevel.NEUTRAL, LoyaltyLevel.fromValue(40))
        assertEquals(LoyaltyLevel.DISCONTENT, LoyaltyLevel.fromValue(39))
        assertEquals(LoyaltyLevel.DISCONTENT, LoyaltyLevel.fromValue(20))
        assertEquals(LoyaltyLevel.REBELLIOUS, LoyaltyLevel.fromValue(19))
        assertEquals(LoyaltyLevel.REBELLIOUS, LoyaltyLevel.fromValue(0))
    }

    @Test
    fun testLoyaltyLevelDisplayNames() {
        // Then: 各等级应该有正确的显示名称
        assertEquals("忠心耿耿", LoyaltyLevel.DEVOTED.displayName)
        assertEquals("忠诚", LoyaltyLevel.LOYAL.displayName)
        assertEquals("中立", LoyaltyLevel.NEUTRAL.displayName)
        assertEquals("不满", LoyaltyLevel.DISCONTENT.displayName)
        assertEquals("叛逆", LoyaltyLevel.REBELLIOUS.displayName)
    }
}
