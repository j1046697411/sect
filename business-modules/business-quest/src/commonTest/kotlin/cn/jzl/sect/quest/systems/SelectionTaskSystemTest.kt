package cn.jzl.sect.quest.systems

import cn.jzl.ecs.ECSDsl
import cn.jzl.ecs.World
import cn.jzl.ecs.addon.Addon
import cn.jzl.ecs.addon.WorldSetup
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.ecs.world
import cn.jzl.sect.core.quest.*
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.SectPositionType
import kotlin.test.*

/**
 * 选拔任务系统测试
 */
class SelectionTaskSystemTest : EntityRelationContext {
    override lateinit var world: World
    private lateinit var system: SelectionTaskSystem

    @OptIn(ECSDsl::class)
    private fun createTestWorld(): World {
        val testWorld = world {
            WorldSetupInstallHelper.install(this, createAddon<Unit>("test") {
                components {
                    world.componentId<QuestComponent>()
                    world.componentId<QuestExecutionComponent>()
                    world.componentId<PolicyComponent>()
                    world.componentId<SectPositionInfo>()
                }
            })
        }
        return testWorld
    }

    @BeforeTest
    fun setup() {
        world = createTestWorld()
        system = SelectionTaskSystem(world)
    }

    @Test
    fun testCheckSelectionCycle_ReturnsTrueWhenCycleReached() {
        // Given: 当前年份和上次选拔年份
        val currentYear = 10
        val lastSelectionYear = 5
        val cycleYears = 5

        // When: 检查选拔周期
        val result = system.checkSelectionCycle(currentYear, lastSelectionYear, cycleYears)

        // Then: 应该返回true（正好达到周期）
        assertTrue(result, "正好达到选拔周期时应该返回true")
    }

    @Test
    fun testCheckSelectionCycle_ReturnsFalseWhenCycleNotReached() {
        // Given: 当前年份和上次选拔年份
        val currentYear = 8
        val lastSelectionYear = 5
        val cycleYears = 5

        // When: 检查选拔周期
        val result = system.checkSelectionCycle(currentYear, lastSelectionYear, cycleYears)

        // Then: 应该返回false（未达到周期）
        assertFalse(result, "未达到选拔周期时应该返回false")
    }

    @Test
    fun testCheckSelectionCycle_ReturnsTrueWhenOverdue() {
        // Given: 当前年份和上次选拔年份
        val currentYear = 15
        val lastSelectionYear = 5
        val cycleYears = 5

        // When: 检查选拔周期
        val result = system.checkSelectionCycle(currentYear, lastSelectionYear, cycleYears)

        // Then: 应该返回true（超过周期）
        assertTrue(result, "超过选拔周期时应该返回true")
    }

    @Test
    fun testCheckSelectionCycle_WithZeroCycle() {
        // Given: 周期为0
        val currentYear = 10
        val lastSelectionYear = 5
        val cycleYears = 0

        // When: 检查选拔周期
        val result = system.checkSelectionCycle(currentYear, lastSelectionYear, cycleYears)

        // Then: 应该返回true（避免除零）
        assertTrue(result, "周期为0时应该返回true")
    }

    @Test
    fun testCalculateSelectionQuota_BasicCalculation() {
        // Given: 外门弟子数量和比例
        val outerDiscipleCount = 100
        val ratio = 0.2

        // When: 计算选拔名额
        val quota = system.calculateSelectionQuota(outerDiscipleCount, ratio)

        // Then: 应该计算出正确的名额
        assertEquals(20, quota, "100名外门弟子，20%比例应该选拔20人")
    }

    @Test
    fun testCalculateSelectionQuota_MinimumOne() {
        // Given: 很少的外门弟子
        val outerDiscipleCount = 5
        val ratio = 0.1

        // When: 计算选拔名额
        val quota = system.calculateSelectionQuota(outerDiscipleCount, ratio)

        // Then: 至少应该有1个名额
        assertEquals(1, quota, "最少应该有1个选拔名额")
    }

    @Test
    fun testCalculateSelectionQuota_ZeroDisciples() {
        // Given: 没有外门弟子
        val outerDiscipleCount = 0
        val ratio = 0.2

        // When: 计算选拔名额
        val quota = system.calculateSelectionQuota(outerDiscipleCount, ratio)

        // Then: 应该返回0
        assertEquals(0, quota, "没有外门弟子时应该返回0名额")
    }

    @Test
    fun testCalculateSelectionQuota_HighRatio() {
        // Given: 很高的选拔比例
        val outerDiscipleCount = 50
        val ratio = 0.5

        // When: 计算选拔名额
        val quota = system.calculateSelectionQuota(outerDiscipleCount, ratio)

        // Then: 应该计算出50%的名额
        assertEquals(25, quota, "50名外门弟子，50%比例应该选拔25人")
    }

    @Test
    fun testCreateSelectionTask_CreatesEntityWithComponents() {
        // Given: 选拔名额
        val quota = 10

        // When: 创建选拔任务
        val questEntity = system.createSelectionTask(world, quota)

        // Then: 应该创建带有正确组件的实体
        assertNotNull(questEntity, "应该创建任务实体")

        val query = world.query { QuestQueryContext(world) }
        var found = false
        query.forEach { ctx ->
            if (ctx.entity == questEntity) {
                found = true
                assertEquals(QuestType.RUIN_EXPLORATION, ctx.quest.type, "选拔任务类型应该是遗迹探索")
                assertEquals(QuestDifficulty.NORMAL, ctx.quest.difficulty, "选拔任务难度应该是普通")
                assertEquals(QuestStatus.PENDING_APPROVAL, ctx.quest.status, "任务状态应该是待审批")
                assertEquals(quota, ctx.quest.maxParticipants, "名额应该等于选拔人数")
                assertTrue(ctx.quest.description.contains("选拔"), "任务描述应该包含选拔")
            }
        }
        assertTrue(found, "应该能找到创建的任务实体")
    }

    @Test
    fun testCreateSelectionTask_GeneratesUniqueQuestId() {
        // Given: 创建多个任务
        val quota1 = 10
        val quota2 = 15

        // When: 创建两个选拔任务
        val questEntity1 = system.createSelectionTask(world, quota1)
        val questEntity2 = system.createSelectionTask(world, quota2)

        // Then: 两个任务应该有不同ID
        val query = world.query { QuestQueryContext(world) }
        var id1: Long? = null
        var id2: Long? = null
        query.forEach { ctx ->
            when (ctx.entity) {
                questEntity1 -> id1 = ctx.quest.questId
                questEntity2 -> id2 = ctx.quest.questId
            }
        }
        assertNotNull(id1, "第一个任务应该有ID")
        assertNotNull(id2, "第二个任务应该有ID")
        assertNotEquals(id1, id2, "两个任务的ID应该不同")
    }

    @Test
    fun testCreateSelectionTask_WithExecutionComponent() {
        // Given: 选拔名额
        val quota = 5

        // When: 创建选拔任务
        val questEntity = system.createSelectionTask(world, quota)

        // Then: 应该同时创建执行组件
        val query = world.query { QuestExecutionQueryContext(world) }
        var found = false
        query.forEach { ctx ->
            if (ctx.entity == questEntity) {
                found = true
                assertEquals(0L, ctx.execution.elderId, "初始长老ID应该为0")
                assertTrue(ctx.execution.innerDiscipleIds.isEmpty(), "初始内门弟子列表应该为空")
                assertTrue(ctx.execution.outerDiscipleIds.isEmpty(), "初始外门弟子列表应该为空")
                assertEquals(0.0f, ctx.execution.progress, "初始进度应该为0")
            }
        }
        assertTrue(found, "应该能找到任务的执行组件")
    }

    @Test
    fun testSystemInitialization() {
        // Then: 系统应该正确初始化
        assertNotNull(system, "选拔任务系统应该能正确初始化")
    }

    @Test
    fun testCreateSelectionTask_WithPolicy() {
        // Given: 创建策略组件
        val policy = PolicyComponent(
            selectionCycleYears = 3,
            selectionRatio = 0.15f,
            resourceAllocationRatio = 0.25f
        )
        world.entity {
            it.addComponent(policy)
        }

        // When: 使用策略创建任务
        val quota = system.calculateSelectionQuota(100, policy.selectionRatio.toDouble())
        val questEntity = system.createSelectionTask(world, quota)

        // Then: 应该正确应用策略
        assertEquals(15, quota, "应该根据策略比例计算名额")
        assertNotNull(questEntity, "应该成功创建任务")
    }

    /**
     * 查询上下文 - 任务
     */
    class QuestQueryContext(world: World) : EntityQueryContext(world) {
        val quest: QuestComponent by component()
    }

    /**
     * 查询上下文 - 任务执行
     */
    class QuestExecutionQueryContext(world: World) : EntityQueryContext(world) {
        val execution: QuestExecutionComponent by component()
    }

    private object WorldSetupInstallHelper {
        @Suppress("UNCHECKED_CAST")
        fun install(ws: WorldSetup, addon: Addon<*, *>) {
            ws.install(addon as Addon<Any, Any>) {}
        }
    }
}
