package cn.jzl.sect.core.quest

import cn.jzl.ecs.*
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.*
import kotlin.test.*

class EvaluationComponentTest : EntityRelationContext {
    override lateinit var world: World

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<EvaluationComponent>()
        }
    }

    @BeforeTest
    fun setup() {
        world = world { install(testAddon) }
    }

    @Test
    fun testEvaluationDimensionEnumValues() {
        assertEquals(EvaluationDimension.CULTIVATION, EvaluationDimension.valueOf("CULTIVATION"))
        assertEquals(EvaluationDimension.COMBAT, EvaluationDimension.valueOf("COMBAT"))
        assertEquals(EvaluationDimension.LOYALTY, EvaluationDimension.valueOf("LOYALTY"))
        assertEquals(EvaluationDimension.EXPERIENCE, EvaluationDimension.valueOf("EXPERIENCE"))
        assertEquals(EvaluationDimension.SPECIALTY, EvaluationDimension.valueOf("SPECIALTY"))
    }

    @Test
    fun testCandidateScoreCreation() {
        val dimensionScores = mapOf(
            EvaluationDimension.CULTIVATION to 0.8f,
            EvaluationDimension.COMBAT to 0.9f,
            EvaluationDimension.LOYALTY to 0.7f,
            EvaluationDimension.EXPERIENCE to 0.6f,
            EvaluationDimension.SPECIALTY to 0.85f
        )

        val candidate = CandidateScore(
            discipleId = 101L,
            totalScore = 0.77f,
            dimensionScores = dimensionScores
        )

        assertEquals(101L, candidate.discipleId)
        assertEquals(0.77f, candidate.totalScore)
        assertEquals(dimensionScores, candidate.dimensionScores)
    }

    @Test
    fun testEvaluationComponentCreation() {
        val candidates = listOf(
            CandidateScore(
                discipleId = 101L,
                totalScore = 0.85f,
                dimensionScores = mapOf(
                    EvaluationDimension.CULTIVATION to 0.8f,
                    EvaluationDimension.COMBAT to 0.9f
                )
            ),
            CandidateScore(
                discipleId = 102L,
                totalScore = 0.75f,
                dimensionScores = mapOf(
                    EvaluationDimension.CULTIVATION to 0.7f,
                    EvaluationDimension.COMBAT to 0.8f
                )
            )
        )

        val entity = world.entity {
            it.addComponent(EvaluationComponent(
                questId = 1L,
                candidates = candidates
            ))
        }

        val evaluation = entity.getComponent<EvaluationComponent>()
        assertEquals(1L, evaluation.questId)
        assertEquals(2, evaluation.candidates.size)
    }

    @Test
    fun testGetDimensionScore() {
        val candidate = CandidateScore(
            discipleId = 101L,
            totalScore = 0.8f,
            dimensionScores = mapOf(
                EvaluationDimension.CULTIVATION to 0.9f,
                EvaluationDimension.COMBAT to 0.8f
            )
        )

        assertEquals(0.9f, candidate.getDimensionScore(EvaluationDimension.CULTIVATION))
        assertEquals(0.8f, candidate.getDimensionScore(EvaluationDimension.COMBAT))
        assertEquals(0.0f, candidate.getDimensionScore(EvaluationDimension.LOYALTY))
    }

    @Test
    fun testGetTopCandidate() {
        val candidates = listOf(
            CandidateScore(
                discipleId = 101L,
                totalScore = 0.75f,
                dimensionScores = emptyMap()
            ),
            CandidateScore(
                discipleId = 102L,
                totalScore = 0.95f,
                dimensionScores = emptyMap()
            ),
            CandidateScore(
                discipleId = 103L,
                totalScore = 0.85f,
                dimensionScores = emptyMap()
            )
        )

        val evaluation = EvaluationComponent(
            questId = 1L,
            candidates = candidates
        )

        val topCandidate = evaluation.getTopCandidate()
        assertNotNull(topCandidate)
        assertEquals(102L, topCandidate.discipleId)
        assertEquals(0.95f, topCandidate.totalScore)
    }

    @Test
    fun testGetTopCandidateWithEmptyList() {
        val evaluation = EvaluationComponent(
            questId = 1L,
            candidates = emptyList()
        )

        val topCandidate = evaluation.getTopCandidate()
        assertNull(topCandidate)
    }

    @Test
    fun testGetTopNCandidates() {
        val candidates = listOf(
            CandidateScore(
                discipleId = 101L,
                totalScore = 0.75f,
                dimensionScores = emptyMap()
            ),
            CandidateScore(
                discipleId = 102L,
                totalScore = 0.95f,
                dimensionScores = emptyMap()
            ),
            CandidateScore(
                discipleId = 103L,
                totalScore = 0.85f,
                dimensionScores = emptyMap()
            ),
            CandidateScore(
                discipleId = 104L,
                totalScore = 0.65f,
                dimensionScores = emptyMap()
            )
        )

        val evaluation = EvaluationComponent(
            questId = 1L,
            candidates = candidates
        )

        val top2 = evaluation.getTopNCandidates(2)
        assertEquals(2, top2.size)
        assertEquals(102L, top2[0].discipleId)
        assertEquals(103L, top2[1].discipleId)
    }

    @Test
    fun testGetTopNCandidatesWithNGreaterThanSize() {
        val candidates = listOf(
            CandidateScore(
                discipleId = 101L,
                totalScore = 0.75f,
                dimensionScores = emptyMap()
            )
        )

        val evaluation = EvaluationComponent(
            questId = 1L,
            candidates = candidates
        )

        val top5 = evaluation.getTopNCandidates(5)
        assertEquals(1, top5.size)
        assertEquals(101L, top5[0].discipleId)
    }

    @Test
    fun testEvaluationDimensionDisplayName() {
        assertEquals("修为", EvaluationDimension.CULTIVATION.displayName)
        assertEquals("战斗力", EvaluationDimension.COMBAT.displayName)
        assertEquals("忠诚度", EvaluationDimension.LOYALTY.displayName)
        assertEquals("经验", EvaluationDimension.EXPERIENCE.displayName)
        assertEquals("专长匹配度", EvaluationDimension.SPECIALTY.displayName)
    }

    @Test
    fun testSingleCandidate() {
        val candidates = listOf(
            CandidateScore(
                discipleId = 101L,
                totalScore = 0.8f,
                dimensionScores = mapOf(EvaluationDimension.CULTIVATION to 0.8f)
            )
        )

        val entity = world.entity {
            it.addComponent(EvaluationComponent(
                questId = 1L,
                candidates = candidates
            ))
        }

        val evaluation = entity.getComponent<EvaluationComponent>()
        assertEquals(1, evaluation.candidates.size)
        assertEquals(101L, evaluation.getTopCandidate()?.discipleId)
    }
}
