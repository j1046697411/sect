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
 * 团队组建系统测试
 */
class TeamFormationSystemTest : EntityRelationContext {
    override lateinit var world: World
    private lateinit var system: TeamFormationSystem

    @OptIn(ECSDsl::class)
    private fun createTestWorld(): World {
        val testWorld = world {
            WorldSetupInstallHelper.install(this, createAddon<Unit>("test") {
                components {
                    world.componentId<SectPositionInfo>()
                    world.componentId<CultivationProgress>()
                    world.componentId<Talent>()
                    world.componentId<QuestComponent>()
                    world.componentId<QuestExecutionComponent>()
                }
            })
        }
        return testWorld
    }

    @BeforeTest
    fun setup() {
        world = createTestWorld()
        system = TeamFormationSystem(world)
    }

    @Test
    fun testFindAvailableElder_ReturnsElder() {
        // Given: 创建一个长老实体
        val elderEntity = world.entity {
            it.addComponent(SectPositionInfo(position = SectPositionType.ELDER))
            it.addComponent(CultivationProgress(realm = Realm.FOUNDATION, layer = 3))
            it.addComponent(Talent(physique = 60, comprehension = 60, fortune = 50))
        }

        // When: 查找可用长老
        val foundElder = system.findAvailableElder(world)

        // Then: 应该找到该长老
        assertNotNull(foundElder, "应该找到可用长老")
        assertEquals(elderEntity, foundElder, "应该返回正确的长老实体")
    }

    @Test
    fun testFindAvailableElder_ReturnsNullWhenNoElder() {
        // Given: 没有长老实体

        // When: 查找可用长老
        val foundElder = system.findAvailableElder(world)

        // Then: 应该返回null
        assertNull(foundElder, "没有长老时应该返回null")
    }

    @Test
    fun testFindAvailableElder_PrefersHigherRealm() {
        // Given: 创建两个不同境界的长老
        val lowerElder = world.entity {
            it.addComponent(SectPositionInfo(position = SectPositionType.ELDER))
            it.addComponent(CultivationProgress(realm = Realm.QI_REFINING, layer = 5))
            it.addComponent(Talent(physique = 50, comprehension = 50, fortune = 50))
        }
        val higherElder = world.entity {
            it.addComponent(SectPositionInfo(position = SectPositionType.ELDER))
            it.addComponent(CultivationProgress(realm = Realm.FOUNDATION, layer = 1))
            it.addComponent(Talent(physique = 70, comprehension = 70, fortune = 60))
        }

        // When: 查找可用长老
        val foundElder = system.findAvailableElder(world)

        // Then: 应该优先选择境界更高的长老
        assertNotNull(foundElder, "应该找到长老")
    }

    @Test
    fun testFindAvailableInnerDisciples_ReturnsCorrectCount() {
        // Given: 创建5个内门弟子
        repeat(5) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_INNER))
                it.addComponent(CultivationProgress(realm = Realm.QI_REFINING, layer = 3))
                it.addComponent(Talent(physique = 50, comprehension = 50, fortune = 50))
            }
        }

        // When: 查找3-5个内门弟子
        val disciples = system.findAvailableInnerDisciples(world, min = 3, max = 5)

        // Then: 应该返回3-5个弟子
        assertTrue(disciples.size in 3..5, "应该返回3-5个内门弟子，实际返回${disciples.size}个")
    }

    @Test
    fun testFindAvailableInnerDisciples_ReturnsEmptyWhenInsufficient() {
        // Given: 只创建2个内门弟子
        repeat(2) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_INNER))
                it.addComponent(CultivationProgress(realm = Realm.QI_REFINING, layer = 3))
                it.addComponent(Talent(physique = 50, comprehension = 50, fortune = 50))
            }
        }

        // When: 查找3-5个内门弟子
        val disciples = system.findAvailableInnerDisciples(world, min = 3, max = 5)

        // Then: 应该返回空列表（人数不足）
        assertTrue(disciples.isEmpty(), "人数不足时应该返回空列表")
    }

    @Test
    fun testFindAvailableInnerDisciples_DefaultRange() {
        // Given: 创建5个内门弟子
        repeat(5) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_INNER))
                it.addComponent(CultivationProgress(realm = Realm.QI_REFINING, layer = 3))
                it.addComponent(Talent(physique = 50, comprehension = 50, fortune = 50))
            }
        }

        // When: 使用默认范围查找内门弟子
        val disciples = system.findAvailableInnerDisciples(world)

        // Then: 应该返回3-5个弟子（默认范围）
        assertTrue(disciples.size in 3..5, "默认应该返回3-5个内门弟子")
    }

    @Test
    fun testFindAvailableOuterDisciples_ReturnsCorrectCount() {
        // Given: 创建15个外门弟子
        repeat(15) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
                it.addComponent(CultivationProgress(realm = Realm.MORTAL, layer = 5))
                it.addComponent(Talent(physique = 40, comprehension = 40, fortune = 40))
            }
        }

        // When: 查找10-20个外门弟子
        val disciples = system.findAvailableOuterDisciples(world, min = 10, max = 20)

        // Then: 应该返回10-15个弟子
        assertTrue(disciples.size in 10..15, "应该返回10-15个外门弟子，实际返回${disciples.size}个")
    }

    @Test
    fun testFindAvailableOuterDisciples_ReturnsEmptyWhenInsufficient() {
        // Given: 只创建5个外门弟子
        repeat(5) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
                it.addComponent(CultivationProgress(realm = Realm.MORTAL, layer = 5))
                it.addComponent(Talent(physique = 40, comprehension = 40, fortune = 40))
            }
        }

        // When: 查找10-20个外门弟子
        val disciples = system.findAvailableOuterDisciples(world, min = 10, max = 20)

        // Then: 应该返回空列表（人数不足）
        assertTrue(disciples.isEmpty(), "人数不足时应该返回空列表")
    }

    @Test
    fun testFindAvailableOuterDisciples_DefaultRange() {
        // Given: 创建20个外门弟子
        repeat(20) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
                it.addComponent(CultivationProgress(realm = Realm.MORTAL, layer = 5))
                it.addComponent(Talent(physique = 40, comprehension = 40, fortune = 40))
            }
        }

        // When: 使用默认范围查找外门弟子
        val disciples = system.findAvailableOuterDisciples(world)

        // Then: 应该返回10-20个弟子（默认范围）
        assertTrue(disciples.size in 10..20, "默认应该返回10-20个外门弟子")
    }

    @Test
    fun testFormTeam_Success() {
        // Given: 创建完整的人员配置
        val elder = world.entity {
            it.addComponent(SectPositionInfo(position = SectPositionType.ELDER))
            it.addComponent(CultivationProgress(realm = Realm.FOUNDATION, layer = 3))
            it.addComponent(Talent(physique = 60, comprehension = 60, fortune = 50))
        }
        repeat(5) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_INNER))
                it.addComponent(CultivationProgress(realm = Realm.QI_REFINING, layer = 3))
                it.addComponent(Talent(physique = 50, comprehension = 50, fortune = 50))
            }
        }
        repeat(15) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
                it.addComponent(CultivationProgress(realm = Realm.MORTAL, layer = 5))
                it.addComponent(Talent(physique = 40, comprehension = 40, fortune = 40))
            }
        }

        // 创建任务
        val questEntity = world.entity {
            it.addComponent(QuestComponent(
                questId = 1L,
                type = QuestType.RUIN_EXPLORATION,
                difficulty = QuestDifficulty.NORMAL,
                status = QuestStatus.PENDING_APPROVAL,
                createdAt = System.currentTimeMillis(),
                maxParticipants = 20
            ))
            it.addComponent(QuestExecutionComponent(
                questId = 1L,
                elderId = 0L,
                innerDiscipleIds = emptyList(),
                outerDiscipleIds = emptyList(),
                progress = 0.0f,
                startTime = 0L,
                estimatedEndTime = 0L
            ))
        }

        // When: 组建团队
        val result = system.formTeam(world, 1L)

        // Then: 应该成功组建团队
        assertTrue(result.success, "应该成功组建团队")
        assertNotNull(result.elder, "应该有长老")
        assertTrue(result.innerDisciples.size in 3..5, "应该有3-5个内门弟子")
        assertTrue(result.outerDisciples.size in 10..20, "应该有10-20个外门弟子")
    }

    @Test
    fun testFormTeam_FailureWhenNoElder() {
        // Given: 没有长老，只有弟子
        repeat(5) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_INNER))
                it.addComponent(CultivationProgress(realm = Realm.QI_REFINING, layer = 3))
                it.addComponent(Talent(physique = 50, comprehension = 50, fortune = 50))
            }
        }
        repeat(15) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
                it.addComponent(CultivationProgress(realm = Realm.MORTAL, layer = 5))
                it.addComponent(Talent(physique = 40, comprehension = 40, fortune = 40))
            }
        }

        // 创建任务
        world.entity {
            it.addComponent(QuestComponent(
                questId = 1L,
                type = QuestType.RUIN_EXPLORATION,
                difficulty = QuestDifficulty.NORMAL,
                status = QuestStatus.PENDING_APPROVAL,
                createdAt = System.currentTimeMillis(),
                maxParticipants = 20
            ))
            it.addComponent(QuestExecutionComponent(
                questId = 1L,
                elderId = 0L,
                innerDiscipleIds = emptyList(),
                outerDiscipleIds = emptyList(),
                progress = 0.0f,
                startTime = 0L,
                estimatedEndTime = 0L
            ))
        }

        // When: 组建团队
        val result = system.formTeam(world, 1L)

        // Then: 应该失败（没有长老）
        assertFalse(result.success, "没有长老时应该失败")
        assertNull(result.elder, "长老应该为null")
    }

    @Test
    fun testFormTeam_FailureWhenInsufficientInnerDisciples() {
        // Given: 只有2个内门弟子（不足最少3个）
        world.entity {
            it.addComponent(SectPositionInfo(position = SectPositionType.ELDER))
            it.addComponent(CultivationProgress(realm = Realm.FOUNDATION, layer = 3))
            it.addComponent(Talent(physique = 60, comprehension = 60, fortune = 50))
        }
        repeat(2) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_INNER))
                it.addComponent(CultivationProgress(realm = Realm.QI_REFINING, layer = 3))
                it.addComponent(Talent(physique = 50, comprehension = 50, fortune = 50))
            }
        }
        repeat(15) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
                it.addComponent(CultivationProgress(realm = Realm.MORTAL, layer = 5))
                it.addComponent(Talent(physique = 40, comprehension = 40, fortune = 40))
            }
        }

        // 创建任务
        world.entity {
            it.addComponent(QuestComponent(
                questId = 1L,
                type = QuestType.RUIN_EXPLORATION,
                difficulty = QuestDifficulty.NORMAL,
                status = QuestStatus.PENDING_APPROVAL,
                createdAt = System.currentTimeMillis(),
                maxParticipants = 20
            ))
            it.addComponent(QuestExecutionComponent(
                questId = 1L,
                elderId = 0L,
                innerDiscipleIds = emptyList(),
                outerDiscipleIds = emptyList(),
                progress = 0.0f,
                startTime = 0L,
                estimatedEndTime = 0L
            ))
        }

        // When: 组建团队
        val result = system.formTeam(world, 1L)

        // Then: 应该失败（内门弟子不足）
        assertFalse(result.success, "内门弟子不足时应该失败")
    }

    @Test
    fun testFormTeam_FailureWhenInsufficientOuterDisciples() {
        // Given: 只有5个外门弟子（不足最少10个）
        world.entity {
            it.addComponent(SectPositionInfo(position = SectPositionType.ELDER))
            it.addComponent(CultivationProgress(realm = Realm.FOUNDATION, layer = 3))
            it.addComponent(Talent(physique = 60, comprehension = 60, fortune = 50))
        }
        repeat(5) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_INNER))
                it.addComponent(CultivationProgress(realm = Realm.QI_REFINING, layer = 3))
                it.addComponent(Talent(physique = 50, comprehension = 50, fortune = 50))
            }
        }
        repeat(5) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
                it.addComponent(CultivationProgress(realm = Realm.MORTAL, layer = 5))
                it.addComponent(Talent(physique = 40, comprehension = 40, fortune = 40))
            }
        }

        // 创建任务
        world.entity {
            it.addComponent(QuestComponent(
                questId = 1L,
                type = QuestType.RUIN_EXPLORATION,
                difficulty = QuestDifficulty.NORMAL,
                status = QuestStatus.PENDING_APPROVAL,
                createdAt = System.currentTimeMillis(),
                maxParticipants = 20
            ))
            it.addComponent(QuestExecutionComponent(
                questId = 1L,
                elderId = 0L,
                innerDiscipleIds = emptyList(),
                outerDiscipleIds = emptyList(),
                progress = 0.0f,
                startTime = 0L,
                estimatedEndTime = 0L
            ))
        }

        // When: 组建团队
        val result = system.formTeam(world, 1L)

        // Then: 应该失败（外门弟子不足）
        assertFalse(result.success, "外门弟子不足时应该失败")
    }

    @Test
    fun testTeamFormationResult_TotalCount() {
        // Given: 创建团队组建结果
        val elder = world.entity {
            it.addComponent(SectPositionInfo(position = SectPositionType.ELDER))
        }
        val innerList = List(4) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_INNER))
            }
        }
        val outerList = List(15) {
            world.entity {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
            }
        }

        val result = TeamFormationResult(
            success = true,
            elder = elder,
            innerDisciples = innerList,
            outerDisciples = outerList
        )

        // Then: 总人数计算正确
        assertEquals(20, result.totalCount, "总人数应该是1+4+15=20")
    }

    private object WorldSetupInstallHelper {
        @Suppress("UNCHECKED_CAST")
        fun install(ws: WorldSetup, addon: Addon<*, *>) {
            ws.install(addon as Addon<Any, Any>) {}
        }
    }
}
