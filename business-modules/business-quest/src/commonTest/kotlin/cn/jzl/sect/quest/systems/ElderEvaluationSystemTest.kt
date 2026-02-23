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
import cn.jzl.sect.cultivation.components.Talent
import cn.jzl.sect.core.quest.*
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.SectPositionType
import kotlin.test.*

/**
 * 长老评估系统测试
 */
class ElderEvaluationSystemTest : EntityRelationContext {
    override lateinit var world: World
    private lateinit var system: ElderEvaluationSystem

    @OptIn(ECSDsl::class)
    private fun createTestWorld(): World {
        val testWorld = world {
            WorldSetupInstallHelper.install(this, createAddon<Unit>("test") {
                components {
                    world.componentId<ElderPersonality>()
                    world.componentId<Talent>()
                    world.componentId<QuestExecutionComponent>()
                    world.componentId<SectPositionInfo>()
                }
            })
        }
        return testWorld
    }

    @BeforeTest
    fun setup() {
        world = createTestWorld()
        system = ElderEvaluationSystem(world)
    }

    @Test
    fun testEvaluateDisciple_ImpartialPersonality() {
        // Given: 公正型长老和弟子
        val elderPersonality = ElderPersonality.impartial()
        val executionResult = ExecutionResult(
            completionRate = 0.8f,
            efficiency = 0.7f,
            quality = 0.75f,
            survivalRate = 0.9f,
            casualties = 0
        )
        val talent = Talent(physique = 50, comprehension = 50, fortune = 50, charm = 50)

        // When: 评估弟子
        val score = system.evaluateDisciple(elderPersonality, executionResult, talent)

        // Then: 分数应该在合理范围内
        // 基础分 = 0.8*0.4 + 0.7*0.25 + 0.75*0.2 + 0.9*0.15 = 0.32 + 0.175 + 0.15 + 0.135 = 0.78
        assertTrue(score in 0.7..0.85, "公正型长老评估分数应该在0.7-0.85之间，实际为$score")
    }

    @Test
    fun testEvaluateDisciple_BiasedPersonality_HighPhysique() {
        // Given: 偏私型长老（偏爱高根骨）和高根骨弟子
        val elderPersonality = ElderPersonality.biased(ElderPreference.HIGH_PHYSIQUE)
        val executionResult = ExecutionResult(
            completionRate = 0.8f,
            efficiency = 0.7f,
            quality = 0.75f,
            survivalRate = 0.9f,
            casualties = 0
        )
        val highPhysiqueTalent = Talent(physique = 80, comprehension = 50, fortune = 50, charm = 50)
        val lowPhysiqueTalent = Talent(physique = 50, comprehension = 50, fortune = 50, charm = 50)

        // When: 评估两个弟子
        val highPhysiqueScore = system.evaluateDisciple(elderPersonality, executionResult, highPhysiqueTalent)
        val lowPhysiqueScore = system.evaluateDisciple(elderPersonality, executionResult, lowPhysiqueTalent)

        // Then: 高根骨弟子应该得分更高（有10%加成）
        assertTrue(highPhysiqueScore > lowPhysiqueScore, "偏爱高根骨的长老应该给高根骨弟子更高分")
        assertEquals(0.10, highPhysiqueScore - lowPhysiqueScore, 0.01, "分数差应该约为0.10")
    }

    @Test
    fun testEvaluateDisciple_BiasedPersonality_HighDiligence() {
        // Given: 偏私型长老（偏爱高勤勉）和高福缘弟子（代表勤勉）
        val elderPersonality = ElderPersonality.biased(ElderPreference.HIGH_DILIGENCE)
        val executionResult = ExecutionResult(
            completionRate = 0.8f,
            efficiency = 0.7f,
            quality = 0.75f,
            survivalRate = 0.9f,
            casualties = 0
        )
        val highFortuneTalent = Talent(physique = 50, comprehension = 50, fortune = 80, charm = 50)
        val lowFortuneTalent = Talent(physique = 50, comprehension = 50, fortune = 50, charm = 50)

        // When: 评估两个弟子
        val highFortuneScore = system.evaluateDisciple(elderPersonality, executionResult, highFortuneTalent)
        val lowFortuneScore = system.evaluateDisciple(elderPersonality, executionResult, lowFortuneTalent)

        // Then: 高福缘弟子应该得分更高（有10%加成）
        assertTrue(highFortuneScore > lowFortuneScore, "偏爱高勤勉的长老应该给高福缘弟子更高分")
        assertEquals(0.10, highFortuneScore - lowFortuneScore, 0.01, "分数差应该约为0.10")
    }

    @Test
    fun testEvaluateDisciple_StrictPersonality() {
        // Given: 严苛型长老
        val strictPersonality = ElderPersonality.strict()
        val impartialPersonality = ElderPersonality.impartial()
        val executionResult = ExecutionResult(
            completionRate = 0.8f,
            efficiency = 0.7f,
            quality = 0.75f,
            survivalRate = 0.9f,
            casualties = 0
        )
        val talent = Talent(physique = 50, comprehension = 50, fortune = 50, charm = 50)

        // When: 同一弟子被不同性格长老评估
        val strictScore = system.evaluateDisciple(strictPersonality, executionResult, talent)
        val impartialScore = system.evaluateDisciple(impartialPersonality, executionResult, talent)

        // Then: 严苛型长老应该给更低分（标准×1.2）
        assertTrue(strictScore < impartialScore, "严苛型长老应该给更低分")
        // 0.78 / 1.2 = 0.65
        assertEquals(0.65, strictScore, 0.01, "严苛型长老评估分数应该约为0.65")
    }

    @Test
    fun testEvaluateDisciple_LenientPersonality() {
        // Given: 宽松型长老
        val lenientPersonality = ElderPersonality.lenient()
        val impartialPersonality = ElderPersonality.impartial()
        val executionResult = ExecutionResult(
            completionRate = 0.8f,
            efficiency = 0.7f,
            quality = 0.75f,
            survivalRate = 0.9f,
            casualties = 0
        )
        val talent = Talent(physique = 50, comprehension = 50, fortune = 50, charm = 50)

        // When: 同一弟子被不同性格长老评估
        val lenientScore = system.evaluateDisciple(lenientPersonality, executionResult, talent)
        val impartialScore = system.evaluateDisciple(impartialPersonality, executionResult, talent)

        // Then: 宽松型长老应该给更高分（标准×0.8）
        assertTrue(lenientScore > impartialScore, "宽松型长老应该给更高分")
        // 0.78 / 0.8 = 0.975，但上限为1.0
        assertTrue(lenientScore > 0.9, "宽松型长老评估分数应该很高")
    }

    @Test
    fun testCalculateFinalScore_Impartial() {
        // Given
        val elderPersonality = ElderPersonality.impartial()
        val talent = Talent(physique = 50, comprehension = 50, fortune = 50, charm = 50)

        // When
        val score = system.calculateFinalScore(0.75, elderPersonality, talent)

        // Then
        assertEquals(0.75, score, 0.001, "公正型长老不应该修改基础分")
    }

    @Test
    fun testCalculateFinalScore_Strict() {
        // Given
        val elderPersonality = ElderPersonality.strict()
        val talent = Talent(physique = 50, comprehension = 50, fortune = 50, charm = 50)

        // When
        val score = system.calculateFinalScore(0.60, elderPersonality, talent)

        // Then: 0.60 / 1.2 = 0.50
        assertEquals(0.50, score, 0.001, "严苛型长老应该降低分数")
    }

    @Test
    fun testCalculateFinalScore_Lenient() {
        // Given
        val elderPersonality = ElderPersonality.lenient()
        val talent = Talent(physique = 50, comprehension = 50, fortune = 50, charm = 50)

        // When
        val score = system.calculateFinalScore(0.60, elderPersonality, talent)

        // Then: 0.60 / 0.8 = 0.75
        assertEquals(0.75, score, 0.001, "宽松型长老应该提高分数")
    }

    @Test
    fun testCalculateFinalScore_BiasedWithHighPhysique() {
        // Given
        val elderPersonality = ElderPersonality.biased(ElderPreference.HIGH_PHYSIQUE)
        val highPhysiqueTalent = Talent(physique = 80, comprehension = 50, fortune = 50, charm = 50)

        // When
        val score = system.calculateFinalScore(0.70, elderPersonality, highPhysiqueTalent)

        // Then: 0.70 + 0.10 = 0.80
        assertEquals(0.80, score, 0.001, "偏爱高根骨的长老应该给高根骨弟子加分")
    }

    @Test
    fun testCalculateFinalScore_BiasedWithLowPhysique() {
        // Given
        val elderPersonality = ElderPersonality.biased(ElderPreference.HIGH_PHYSIQUE)
        val lowPhysiqueTalent = Talent(physique = 50, comprehension = 50, fortune = 50, charm = 50)

        // When
        val score = system.calculateFinalScore(0.70, elderPersonality, lowPhysiqueTalent)

        // Then: 没有加成
        assertEquals(0.70, score, 0.001, "偏爱高根骨的长老不应该给低根骨弟子加分")
    }

    @Test
    fun testCalculateFinalScore_ScoreWithinBounds() {
        // Given: 宽松型长老和已经很高的基础分
        val lenientPersonality = ElderPersonality.lenient()
        val talent = Talent(physique = 50, comprehension = 50, fortune = 50, charm = 50)

        // When: 基础分已经很高
        val score = system.calculateFinalScore(0.95, lenientPersonality, talent)

        // Then: 分数不应该超过1.0
        assertTrue(score <= 1.0, "分数不应该超过1.0")
    }

    @Test
    fun testNominateCandidates_Returns150PercentQuota() {
        // Given: 长老和20个外门弟子
        val elder = createElderWithPersonality(ElderPersonality.impartial())
        val outerDisciples = List(20) { index ->
            createOuterDiscipleWithTalent(
                Talent(
                    physique = 40 + index * 2,
                    comprehension = 40 + index,
                    fortune = 40 + index,
                    charm = 40 + index
                )
            )
        }
        val quota = 10

        // When: 提名候选人
        val candidates = system.nominateCandidates(outerDisciples, quota, elder)

        // Then: 应该返回150%名额的候选人（15个）
        assertEquals(15, candidates.size, "应该返回150%名额的候选人")
    }

    @Test
    fun testNominateCandidates_SortedByScore() {
        // Given: 长老和多个外门弟子
        val elder = createElderWithPersonality(ElderPersonality.impartial())
        val outerDisciples = List(10) { index ->
            createOuterDiscipleWithTalent(
                Talent(
                    physique = 50 + index * 5,
                    comprehension = 50,
                    fortune = 50,
                    charm = 50
                )
            )
        }
        val quota = 5

        // When: 提名候选人
        val candidates = system.nominateCandidates(outerDisciples, quota, elder)

        // Then: 应该按分数降序排列
        for (i in 0 until candidates.size - 1) {
            assertTrue(
                candidates[i].totalScore >= candidates[i + 1].totalScore,
                "候选人应该按分数降序排列"
            )
        }
    }

    @Test
    fun testNominateCandidates_WithQuota1() {
        // Given: 长老和多个外门弟子，名额为1
        val elder = createElderWithPersonality(ElderPersonality.impartial())
        val outerDisciples = List(10) {
            createOuterDiscipleWithTalent(Talent(physique = 50, comprehension = 50, fortune = 50, charm = 50))
        }
        val quota = 1

        // When: 提名候选人
        val candidates = system.nominateCandidates(outerDisciples, quota, elder)

        // Then: 应该返回至少1个候选人（150%即1.5，取整为1或2）
        assertTrue(candidates.size >= 1, "应该返回至少1个候选人")
    }

    @Test
    fun testNominateCandidates_EmptyDiscipleList() {
        // Given: 长老和空弟子列表
        val elder = createElderWithPersonality(ElderPersonality.impartial())
        val emptyList = emptyList<cn.jzl.ecs.entity.Entity>()
        val quota = 5

        // When: 提名候选人
        val candidates = system.nominateCandidates(emptyList, quota, elder)

        // Then: 应该返回空列表
        assertTrue(candidates.isEmpty(), "空弟子列表应该返回空候选人列表")
    }

    @Test
    fun testNominateCandidates_BiasedElderPrefersHighPhysique() {
        // Given: 偏爱高根骨的长老
        val biasedElder = createElderWithPersonality(
            ElderPersonality.biased(ElderPreference.HIGH_PHYSIQUE)
        )
        val impartialElder = createElderWithPersonality(ElderPersonality.impartial())

        // 创建两个弟子，一个高根骨，一个低根骨
        val highPhysiqueDisciple = createOuterDiscipleWithTalent(
            Talent(physique = 90, comprehension = 50, fortune = 50, charm = 50)
        )
        val lowPhysiqueDisciple = createOuterDiscipleWithTalent(
            Talent(physique = 40, comprehension = 50, fortune = 50, charm = 50)
        )

        val disciples = listOf(highPhysiqueDisciple, lowPhysiqueDisciple)
        val quota = 1

        // When: 两个长老分别提名
        val biasedCandidates = system.nominateCandidates(disciples, quota, biasedElder)
        val impartialCandidates = system.nominateCandidates(disciples, quota, impartialElder)

        // Then: 偏爱高根骨的长老应该给高根骨弟子更高排名
        if (biasedCandidates.isNotEmpty() && impartialCandidates.isNotEmpty()) {
            val biasedTopId = biasedCandidates.first().discipleId
            val impartialTopId = impartialCandidates.first().discipleId

            // 偏爱高根骨的长老更可能选择高根骨弟子
            assertEquals(
                highPhysiqueDisciple,
                biasedTopId,
                "偏爱高根骨的长老应该选择高根骨弟子作为首选"
            )
        }
    }

    @Test
    fun testEvaluationWeights_DefaultWeights() {
        // Given
        val weights = EvaluationWeights(
            completionRate = 0.40f,
            efficiency = 0.25f,
            quality = 0.20f,
            survivalRate = 0.15f
        )

        // Then: 权重之和应该为1.0
        assertTrue(EvaluationWeights.isValid(weights), "默认权重之和应该为1.0")
    }

    @Test
    fun testEvaluationWeights_InvalidWeights() {
        // Given: 权重之和不等于1.0
        val invalidWeights = EvaluationWeights(
            completionRate = 0.50f,
            efficiency = 0.25f,
            quality = 0.20f,
            survivalRate = 0.15f
        )

        // Then: 验证应该失败
        assertFalse(EvaluationWeights.isValid(invalidWeights), "权重之和不等于1.0时验证应该失败")
    }

    @Test
    fun testEvaluationWeights_ApplyStrictFactor() {
        // Given
        val weights = EvaluationWeights(
            completionRate = 0.40f,
            efficiency = 0.25f,
            quality = 0.20f,
            survivalRate = 0.15f
        )

        // When
        val strictWeights = weights.applyStrictFactor()

        // Then: 各权重应该乘以1.2
        assertEquals(0.48f, strictWeights.completionRate, 0.001f)
        assertEquals(0.30f, strictWeights.efficiency, 0.001f)
        assertEquals(0.24f, strictWeights.quality, 0.001f)
        assertEquals(0.18f, strictWeights.survivalRate, 0.001f)
    }

    @Test
    fun testEvaluationWeights_ApplyLenientFactor() {
        // Given
        val weights = EvaluationWeights(
            completionRate = 0.40f,
            efficiency = 0.25f,
            quality = 0.20f,
            survivalRate = 0.15f
        )

        // When
        val lenientWeights = weights.applyLenientFactor()

        // Then: 各权重应该乘以0.8
        assertEquals(0.32f, lenientWeights.completionRate, 0.001f)
        assertEquals(0.20f, lenientWeights.efficiency, 0.001f)
        assertEquals(0.16f, lenientWeights.quality, 0.001f)
        assertEquals(0.12f, lenientWeights.survivalRate, 0.001f)
    }

    @Test
    fun testElderPersonality_DisplayNames() {
        // Then: 显示名称应该正确
        assertEquals("公正", ElderPersonalityType.IMPARTIAL.displayName)
        assertEquals("偏私", ElderPersonalityType.BIASED.displayName)
        assertEquals("严苛", ElderPersonalityType.STRICT.displayName)
        assertEquals("宽松", ElderPersonalityType.LENIENT.displayName)
    }

    @Test
    fun testElderPreference_DisplayNames() {
        // Then: 显示名称应该正确
        assertEquals("无偏好", ElderPreference.NONE.displayName)
        assertEquals("偏爱高根骨", ElderPreference.HIGH_PHYSIQUE.displayName)
        assertEquals("偏爱高勤勉", ElderPreference.HIGH_DILIGENCE.displayName)
    }

    @Test
    fun testElderPersonality_FactoryMethods() {
        // When: 使用工厂方法创建
        val impartial = ElderPersonality.impartial()
        val biased = ElderPersonality.biased(ElderPreference.HIGH_PHYSIQUE)
        val strict = ElderPersonality.strict()
        val lenient = ElderPersonality.lenient()

        // Then: 应该创建正确类型的性格
        assertEquals(ElderPersonalityType.IMPARTIAL, impartial.type)
        assertEquals(ElderPersonalityType.BIASED, biased.type)
        assertEquals(ElderPersonalityType.STRICT, strict.type)
        assertEquals(ElderPersonalityType.LENIENT, lenient.type)

        // Then: 偏私型应该有正确的偏好
        assertEquals(ElderPreference.HIGH_PHYSIQUE, biased.preference)
        assertEquals(ElderPreference.NONE, impartial.preference)
    }

    @Test
    fun testSystemInitialization() {
        // Then: 系统应该正确初始化
        assertNotNull(system, "长老评估系统应该能正确初始化")
    }

    @Test
    fun testNominateCandidatesBatch() {
        // Given: 多个长老和各自的弟子
        val elder1 = createElderWithPersonality(ElderPersonality.impartial())
        val elder2 = createElderWithPersonality(ElderPersonality.strict())

        val disciples1 = List(10) { createOuterDiscipleWithTalent(Talent(physique = 50 + it)) }
        val disciples2 = List(10) { createOuterDiscipleWithTalent(Talent(physique = 40 + it)) }

        val pairs = listOf(
            elder1 to disciples1,
            elder2 to disciples2
        )
        val quotaPerElder = 5

        // When: 批量提名
        val results = system.nominateCandidatesBatch(pairs, quotaPerElder)

        // Then: 应该返回每个长老的提名结果
        assertEquals(2, results.size, "应该返回两个长老的提名结果")

        results.forEach { result ->
            assertEquals(quotaPerElder, result.quota, "每个长老的配额应该正确")
            assertTrue(result.actualCount >= quotaPerElder, "每个长老应该返回至少配额数量的候选人")
            assertTrue(result.candidates.isNotEmpty(), "每个长老应该返回候选人")
        }
    }

    // 辅助方法
    private fun createElderWithPersonality(personality: ElderPersonality): cn.jzl.ecs.entity.Entity {
        return world.entity {
            it.addComponent(personality)
            it.addComponent(SectPositionInfo(position = SectPositionType.ELDER))
        }
    }

    private fun createOuterDiscipleWithTalent(talent: Talent): cn.jzl.ecs.entity.Entity {
        return world.entity {
            it.addComponent(talent)
            it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
        }
    }

    private object WorldSetupInstallHelper {
        @Suppress("UNCHECKED_CAST")
        fun install(ws: WorldSetup, addon: Addon<*, *>) {
            ws.install(addon as Addon<Any, Any>) {}
        }
    }
}
