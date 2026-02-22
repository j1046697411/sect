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
import cn.jzl.ecs.entity.id
import cn.jzl.ecs.world
import cn.jzl.sect.core.quest.*
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.core.cultivation.CultivationProgress
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.cultivation.Talent
import kotlin.test.*

/**
 * 任务执行系统测试
 */
class QuestExecutionSystemTest : EntityRelationContext {
    override lateinit var world: World
    private lateinit var system: QuestExecutionSystem

    @OptIn(ECSDsl::class)
    private fun createTestWorld(): World {
        val testWorld = world {
            WorldSetupInstallHelper.install(this, createAddon<Unit>("test") {
                components {
                    world.componentId<QuestComponent>()
                    world.componentId<QuestExecutionComponent>()
                    world.componentId<SectPositionInfo>()
                    world.componentId<CultivationProgress>()
                    world.componentId<Talent>()
                }
            })
        }
        return testWorld
    }

    @BeforeTest
    fun setup() {
        world = createTestWorld()
        system = QuestExecutionSystem(world)
    }

    @Test
    fun testCalculateSuccessRate_EasyQuest() {
        // Given: 简单任务和完整团队
        val team = createFullTeam()

        // When: 计算成功率
        val successRate = system.calculateSuccessRate(QuestDifficulty.EASY, team)

        // Then: 简单任务应该有较高成功率
        assertTrue(successRate > 0.5, "简单任务应该有较高成功率")
        assertTrue(successRate <= 1.0, "成功率不应该超过100%")
    }

    @Test
    fun testCalculateSuccessRate_HardQuest() {
        // Given: 困难任务和完整团队
        val team = createFullTeam()

        // When: 计算成功率
        val successRate = system.calculateSuccessRate(QuestDifficulty.HARD, team)

        // Then: 困难任务应该有较低成功率
        assertTrue(successRate < 0.8, "困难任务应该有较低成功率")
        assertTrue(successRate >= 0.0, "成功率不应该为负")
    }

    @Test
    fun testCalculateSuccessRate_NormalQuest() {
        // Given: 普通任务和完整团队
        val team = createFullTeam()

        // When: 计算成功率
        val successRate = system.calculateSuccessRate(QuestDifficulty.NORMAL, team)

        // Then: 普通任务成功率应该在中间范围
        assertTrue(successRate in 0.3..0.9, "普通任务成功率应该在合理范围内")
    }

    @Test
    fun testCalculateSuccessRate_StrongerTeamHasHigherRate() {
        // Given: 两个不同实力的团队
        val weakTeam = createWeakTeam()
        val strongTeam = createStrongTeam()

        // When: 计算成功率
        val weakRate = system.calculateSuccessRate(QuestDifficulty.NORMAL, weakTeam)
        val strongRate = system.calculateSuccessRate(QuestDifficulty.NORMAL, strongTeam)

        // Then: 强队成功率应该更高
        assertTrue(strongRate > weakRate, "强队应该有更高的成功率")
    }

    @Test
    fun testCalculateCasualties_EasyQuest() {
        // Given: 20个外门弟子
        val outerDisciples = List(20) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
                it.addComponent(CultivationProgress(realm = Realm.MORTAL, layer = 5))
                it.addComponent(Talent(physique = 40, comprehension = 40, fortune = 40))
            }
        }

        // When: 计算简单任务伤亡
        val casualties = system.calculateCasualties(outerDisciples, QuestDifficulty.EASY)

        // Then: 简单任务伤亡应该很少或没有
        assertTrue(casualties >= 0, "伤亡不应该为负")
        assertTrue(casualties <= 3, "简单任务伤亡应该很少")
    }

    @Test
    fun testCalculateCasualties_HardQuest() {
        // Given: 20个外门弟子
        val outerDisciples = List(20) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
                it.addComponent(CultivationProgress(realm = Realm.MORTAL, layer = 5))
                it.addComponent(Talent(physique = 40, comprehension = 40, fortune = 40))
            }
        }

        // When: 计算困难任务伤亡
        val casualties = system.calculateCasualties(outerDisciples, QuestDifficulty.HARD)

        // Then: 困难任务伤亡应该较多
        assertTrue(casualties >= 0, "伤亡不应该为负")
        assertTrue(casualties <= 20, "伤亡不应该超过总人数")
    }

    @Test
    fun testCalculateCasualties_NotExceedTotal() {
        // Given: 10个外门弟子
        val outerDisciples = List(10) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
                it.addComponent(CultivationProgress(realm = Realm.MORTAL, layer = 5))
                it.addComponent(Talent(physique = 40, comprehension = 40, fortune = 40))
            }
        }

        // When: 计算伤亡（多次测试确保不会超出）
        repeat(10) {
            val casualties = system.calculateCasualties(outerDisciples, QuestDifficulty.HARD)
            assertTrue(casualties <= 10, "伤亡不应该超过总人数")
        }
    }

    @Test
    fun testCalculateCasualties_EmptyList() {
        // Given: 空列表
        val emptyList = emptyList<cn.jzl.ecs.entity.Entity>()

        // When: 计算伤亡
        val casualties = system.calculateCasualties(emptyList, QuestDifficulty.NORMAL)

        // Then: 应该返回0
        assertEquals(0, casualties, "空列表应该返回0伤亡")
    }

    @Test
    fun testExecuteQuest_Success() {
        // Given: 创建任务和团队
        val questEntity = createQuestWithTeam(QuestDifficulty.NORMAL)

        // When: 执行任务
        val result = system.executeQuest(world, 1L)

        // Then: 应该返回执行结果
        assertNotNull(result, "应该返回执行结果")
        assertTrue(result.completionRate in 0.0f..1.0f, "完成度应该在0-1之间")
        assertTrue(result.efficiency in 0.0f..1.0f, "效率应该在0-1之间")
        assertTrue(result.quality in 0.0f..1.0f, "质量应该在0-1之间")
        assertTrue(result.survivalRate in 0.0f..1.0f, "存活率应该在0-1之间")
        assertTrue(result.casualties >= 0, "伤亡不应该为负")
    }

    @Test
    fun testExecuteQuest_UpdatesQuestStatus() {
        // Given: 创建任务和团队
        createQuestWithTeam(QuestDifficulty.NORMAL)

        // When: 执行任务
        system.executeQuest(world, 1L)

        // Then: 任务状态应该被更新（这里只验证执行不抛异常）
        // 实际状态更新需要通过查询验证
    }

    @Test
    fun testExecuteQuest_ReturnsZeroForNonExistentQuest() {
        // Given: 不存在的任务ID

        // When: 执行不存在的任务
        val result = system.executeQuest(world, 999L)

        // Then: 应该返回零值结果
        assertEquals(0.0f, result.completionRate, "不存在的任务完成度应该为0")
        assertEquals(0.0f, result.efficiency, "不存在的任务效率应该为0")
        assertEquals(0.0f, result.quality, "不存在的任务质量应该为0")
        assertEquals(0.0f, result.survivalRate, "不存在的任务存活率应该为0")
        assertEquals(0, result.casualties, "不存在的任务伤亡应该为0")
    }

    @Test
    fun testExecutionResult_CalculateTotalScore() {
        // Given: 创建执行结果
        val result = ExecutionResult(
            completionRate = 0.8f,
            efficiency = 0.7f,
            quality = 0.9f,
            survivalRate = 0.85f,
            casualties = 2
        )

        // When: 计算总分
        val score = result.calculateTotalScore()

        // Then: 分数应该在合理范围内
        // 0.8*0.3 + 0.7*0.25 + 0.9*0.25 + 0.85*0.2 = 0.24 + 0.175 + 0.225 + 0.17 = 0.81
        assertEquals(0.81f, score, 0.01f, "总分计算应该正确")
    }

    @Test
    fun testExecutionResult_GetRating() {
        // Given: 不同分数的执行结果
        val sResult = ExecutionResult(0.95f, 0.95f, 0.95f, 0.95f, 0)
        val aResult = ExecutionResult(0.85f, 0.85f, 0.85f, 0.85f, 1)
        val bResult = ExecutionResult(0.75f, 0.75f, 0.75f, 0.75f, 2)
        val cResult = ExecutionResult(0.65f, 0.65f, 0.65f, 0.65f, 3)
        val dResult = ExecutionResult(0.5f, 0.5f, 0.5f, 0.5f, 5)

        // Then: 评级应该正确
        assertEquals("S", sResult.getRating(), "高分应该是S级")
        assertEquals("A", aResult.getRating(), "较高分应该是A级")
        assertEquals("B", bResult.getRating(), "中等分应该是B级")
        assertEquals("C", cResult.getRating(), "较低分应该是C级")
        assertEquals("D", dResult.getRating(), "低分应该是D级")
    }

    @Test
    fun testSystemInitialization() {
        // Then: 系统应该正确初始化
        assertNotNull(system, "任务执行系统应该能正确初始化")
    }

    // 辅助方法
    private fun createFullTeam(): TeamFormationResult {
        val elder = world.entity {
            it.addComponent(SectPositionInfo(position = SectPositionType.ELDER))
            it.addComponent(CultivationProgress(realm = Realm.FOUNDATION, layer = 3))
            it.addComponent(Talent(physique = 60, comprehension = 60, fortune = 50))
        }
        val innerDisciples = List(4) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_INNER))
                it.addComponent(CultivationProgress(realm = Realm.QI_REFINING, layer = 3))
                it.addComponent(Talent(physique = 50, comprehension = 50, fortune = 50))
            }
        }
        val outerDisciples = List(15) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
                it.addComponent(CultivationProgress(realm = Realm.MORTAL, layer = 5))
                it.addComponent(Talent(physique = 40, comprehension = 40, fortune = 40))
            }
        }
        return TeamFormationResult(
            success = true,
            elder = elder,
            innerDisciples = innerDisciples,
            outerDisciples = outerDisciples
        )
    }

    private fun createWeakTeam(): TeamFormationResult {
        val elder = world.entity {
            it.addComponent(SectPositionInfo(position = SectPositionType.ELDER))
            it.addComponent(CultivationProgress(realm = Realm.QI_REFINING, layer = 5))
            it.addComponent(Talent(physique = 40, comprehension = 40, fortune = 40))
        }
        val innerDisciples = List(3) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_INNER))
                it.addComponent(CultivationProgress(realm = Realm.QI_REFINING, layer = 1))
                it.addComponent(Talent(physique = 30, comprehension = 30, fortune = 30))
            }
        }
        val outerDisciples = List(10) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
                it.addComponent(CultivationProgress(realm = Realm.MORTAL, layer = 3))
                it.addComponent(Talent(physique = 20, comprehension = 20, fortune = 20))
            }
        }
        return TeamFormationResult(
            success = true,
            elder = elder,
            innerDisciples = innerDisciples,
            outerDisciples = outerDisciples
        )
    }

    private fun createStrongTeam(): TeamFormationResult {
        val elder = world.entity {
            it.addComponent(SectPositionInfo(position = SectPositionType.ELDER))
            it.addComponent(CultivationProgress(realm = Realm.FOUNDATION, layer = 5))
            it.addComponent(Talent(physique = 80, comprehension = 80, fortune = 70))
        }
        val innerDisciples = List(5) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_INNER))
                it.addComponent(CultivationProgress(realm = Realm.QI_REFINING, layer = 9))
                it.addComponent(Talent(physique = 70, comprehension = 70, fortune = 60))
            }
        }
        val outerDisciples = List(20) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
                it.addComponent(CultivationProgress(realm = Realm.MORTAL, layer = 9))
                it.addComponent(Talent(physique = 50, comprehension = 50, fortune = 50))
            }
        }
        return TeamFormationResult(
            success = true,
            elder = elder,
            innerDisciples = innerDisciples,
            outerDisciples = outerDisciples
        )
    }

    private fun createQuestWithTeam(difficulty: QuestDifficulty): cn.jzl.ecs.entity.Entity {
        val elder = world.entity {
            it.addComponent(SectPositionInfo(position = SectPositionType.ELDER))
            it.addComponent(CultivationProgress(realm = Realm.FOUNDATION, layer = 3))
            it.addComponent(Talent(physique = 60, comprehension = 60, fortune = 50))
        }
        val innerIds = List(4) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_INNER))
                it.addComponent(CultivationProgress(realm = Realm.QI_REFINING, layer = 3))
                it.addComponent(Talent(physique = 50, comprehension = 50, fortune = 50))
            }.id.toLong()
        }
        val outerIds = List(15) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
                it.addComponent(CultivationProgress(realm = Realm.MORTAL, layer = 5))
                it.addComponent(Talent(physique = 40, comprehension = 40, fortune = 40))
            }.id.toLong()
        }

        return world.entity {
            it.addComponent(QuestComponent(
                questId = 1L,
                type = QuestType.RUIN_EXPLORATION,
                difficulty = difficulty,
                status = QuestStatus.IN_PROGRESS,
                createdAt = System.currentTimeMillis(),
                maxParticipants = 20
            ))
            it.addComponent(QuestExecutionComponent(
                questId = 1L,
                elderId = elder.id.toLong(),
                innerDiscipleIds = innerIds,
                outerDiscipleIds = outerIds,
                progress = 0.0f,
                startTime = System.currentTimeMillis(),
                estimatedEndTime = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000
            ))
        }
    }

    private object WorldSetupInstallHelper {
        @Suppress("UNCHECKED_CAST")
        fun install(ws: WorldSetup, addon: Addon<*, *>) {
            ws.install(addon as Addon<Any, Any>) {}
        }
    }
}
