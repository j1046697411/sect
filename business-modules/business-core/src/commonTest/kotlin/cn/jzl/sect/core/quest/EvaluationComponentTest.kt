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
            EvaluationDimension.CULTIVATION to 0.8,
            EvaluationDimension.COMBAT to 0.9,
            EvaluationDimension.LOYALTY to 0.7,
            EvaluationDimension.EXPERIENCE to 0.6,
            EvaluationDimension.SPECIALTY to 0.85
        )

        val entity = world.entity {}
        val candidate = CandidateScore(
            discipleId = entity,
            totalScore = 0.77,
            dimensionScores = dimensionScores
        )

        assertEquals(entity, candidate.discipleId)
        assertEquals(0.77, candidate.totalScore, 0.001)
        assertEquals(dimensionScores, candidate.dimensionScores)
    }

    @Test
    fun testEvaluationComponentCreation() {
        val entity1 = world.entity {}
        val entity2 = world.entity {}
        
        val candidates = listOf(
            CandidateScore(
                discipleId = entity1,
                totalScore = 0.85,
                dimensionScores = mapOf(
                    EvaluationDimension.CULTIVATION to 0.8,
                    EvaluationDimension.COMBAT to 0.9
                )
            ),
            CandidateScore(
                discipleId = entity2,
                totalScore = 0.75,
                dimensionScores = mapOf(
                    EvaluationDimension.CULTIVATION to 0.7,
                    EvaluationDimension.COMBAT to 0.8
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
        val entity = world.entity {}
        val candidate = CandidateScore(
            discipleId = entity,
            totalScore = 0.8,
            dimensionScores = mapOf(
                EvaluationDimension.CULTIVATION to 0.9,
                EvaluationDimension.COMBAT to 0.8
            )
        )

        assertEquals(0.9, candidate.getDimensionScore(EvaluationDimension.CULTIVATION), 0.001)
        assertEquals(0.8, candidate.getDimensionScore(EvaluationDimension.COMBAT), 0.001)
        assertEquals(0.0, candidate.getDimensionScore(EvaluationDimension.LOYALTY), 0.001)
    }

    @Test
    fun testGetTopCandidate() {
        val entity1 = world.entity {}
        val entity2 = world.entity {}
        val entity3 = world.entity {}
        
        val candidates = listOf(
            CandidateScore(
                discipleId = entity1,
                totalScore = 0.75
            ),
            CandidateScore(
                discipleId = entity2,
                totalScore = 0.95
            ),
            CandidateScore(
                discipleId = entity3,
                totalScore = 0.85
            )
        )

        val evaluation = EvaluationComponent(
            questId = 1L,
            candidates = candidates
        )

        val topCandidate = evaluation.getTopCandidate()
        assertNotNull(topCandidate)
        assertEquals(entity2, topCandidate.discipleId)
        assertEquals(0.95, topCandidate.totalScore, 0.001)
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
        val entity1 = world.entity {}
        val entity2 = world.entity {}
        val entity3 = world.entity {}
        val entity4 = world.entity {}
        
        val candidates = listOf(
            CandidateScore(
                discipleId = entity1,
                totalScore = 0.75
            ),
            CandidateScore(
                discipleId = entity2,
                totalScore = 0.95
            ),
            CandidateScore(
                discipleId = entity3,
                totalScore = 0.85
            ),
            CandidateScore(
                discipleId = entity4,
                totalScore = 0.65
            )
        )

        val evaluation = EvaluationComponent(
            questId = 1L,
            candidates = candidates
        )

        val top2 = evaluation.getTopNCandidates(2)
        assertEquals(2, top2.size)
        assertEquals(entity2, top2[0].discipleId)
        assertEquals(entity3, top2[1].discipleId)
    }

    @Test
    fun testGetTopNCandidatesWithNGreaterThanSize() {
        val entity1 = world.entity {}
        
        val candidates = listOf(
            CandidateScore(
                discipleId = entity1,
                totalScore = 0.75
            )
        )

        val evaluation = EvaluationComponent(
            questId = 1L,
            candidates = candidates
        )

        val top5 = evaluation.getTopNCandidates(5)
        assertEquals(1, top5.size)
        assertEquals(entity1, top5[0].discipleId)
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
        val entity1 = world.entity {}
        
        val candidates = listOf(
            CandidateScore(
                discipleId = entity1,
                totalScore = 0.8,
                dimensionScores = mapOf(EvaluationDimension.CULTIVATION to 0.8)
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
        assertEquals(entity1, evaluation.getTopCandidate()?.discipleId)
    }
}
