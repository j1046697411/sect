package cn.jzl.sect.core.quest

import cn.jzl.ecs.*
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.*
import kotlin.test.*

class QuestExecutionComponentTest : EntityRelationContext {
    override lateinit var world: World

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<QuestExecutionComponent>()
        }
    }

    @BeforeTest
    fun setup() {
        world = world { install(testAddon) }
    }

    @Test
    fun testQuestExecutionComponentCreation() {
        val entity = world.entity {
            it.addComponent(QuestExecutionComponent(
                questId = 1L,
                elderId = 100L,
                innerDiscipleIds = listOf(101L, 102L),
                outerDiscipleIds = listOf(201L, 202L, 203L),
                progress = 50.0f,
                startTime = 1000L,
                estimatedEndTime = 5000L
            ))
        }

        val execution = entity.getComponent<QuestExecutionComponent>()
        assertEquals(1L, execution.questId)
        assertEquals(100L, execution.elderId)
        assertEquals(listOf(101L, 102L), execution.innerDiscipleIds)
        assertEquals(listOf(201L, 202L, 203L), execution.outerDiscipleIds)
        assertEquals(50.0f, execution.progress)
        assertEquals(1000L, execution.startTime)
        assertEquals(5000L, execution.estimatedEndTime)
    }

    @Test
    fun testExecutionResultCreation() {
        val result = ExecutionResult(
            completionRate = 0.85f,
            efficiency = 0.9f,
            quality = 0.8f,
            survivalRate = 0.95f,
            casualties = 1
        )

        assertEquals(0.85f, result.completionRate)
        assertEquals(0.9f, result.efficiency)
        assertEquals(0.8f, result.quality)
        assertEquals(0.95f, result.survivalRate)
        assertEquals(1, result.casualties)
    }

    @Test
    fun testCalculateTotalScore() {
        val result = ExecutionResult(
            completionRate = 1.0f,
            efficiency = 1.0f,
            quality = 1.0f,
            survivalRate = 1.0f,
            casualties = 0
        )

        val score = result.calculateTotalScore()
        assertEquals(1.0f, score, 0.001f)
    }

    @Test
    fun testCalculateTotalScoreWithWeightedValues() {
        val result = ExecutionResult(
            completionRate = 0.5f,
            efficiency = 0.5f,
            quality = 0.5f,
            survivalRate = 0.5f,
            casualties = 5
        )

        val score = result.calculateTotalScore()
        assertEquals(0.5f, score, 0.001f)
    }

    @Test
    fun testGetRatingS() {
        val result = ExecutionResult(
            completionRate = 1.0f,
            efficiency = 1.0f,
            quality = 1.0f,
            survivalRate = 0.9f,
            casualties = 0
        )

        assertEquals("S", result.getRating())
    }

    @Test
    fun testGetRatingA() {
        val result = ExecutionResult(
            completionRate = 0.9f,
            efficiency = 0.85f,
            quality = 0.8f,
            survivalRate = 0.85f,
            casualties = 1
        )

        assertEquals("A", result.getRating())
    }

    @Test
    fun testGetRatingB() {
        val result = ExecutionResult(
            completionRate = 0.8f,
            efficiency = 0.7f,
            quality = 0.7f,
            survivalRate = 0.7f,
            casualties = 2
        )

        assertEquals("B", result.getRating())
    }

    @Test
    fun testGetRatingC() {
        val result = ExecutionResult(
            completionRate = 0.7f,
            efficiency = 0.6f,
            quality = 0.6f,
            survivalRate = 0.6f,
            casualties = 3
        )

        assertEquals("C", result.getRating())
    }

    @Test
    fun testGetRatingD() {
        val result = ExecutionResult(
            completionRate = 0.5f,
            efficiency = 0.5f,
            quality = 0.5f,
            survivalRate = 0.5f,
            casualties = 5
        )

        assertEquals("D", result.getRating())
    }

    @Test
    fun testEmptyDiscipleLists() {
        val entity = world.entity {
            it.addComponent(QuestExecutionComponent(
                questId = 2L,
                elderId = 100L,
                innerDiscipleIds = emptyList(),
                outerDiscipleIds = emptyList(),
                progress = 0.0f,
                startTime = 1000L,
                estimatedEndTime = 2000L
            ))
        }

        val execution = entity.getComponent<QuestExecutionComponent>()
        assertTrue(execution.innerDiscipleIds.isEmpty())
        assertTrue(execution.outerDiscipleIds.isEmpty())
    }
}
