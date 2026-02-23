/**
 * 长老评估服务
 *
 * 提供长老评估管理功能：
 * - 评估单个弟子
 * - 提名候选人
 * - 计算最终分数（应用性格修正）
 */
package cn.jzl.sect.quest.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.id
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.cultivation.components.Talent
import cn.jzl.sect.quest.components.CandidateScore
import cn.jzl.sect.quest.components.ElderPersonality
import cn.jzl.sect.quest.components.ElderPersonalityType
import cn.jzl.sect.quest.components.ElderPreference
import cn.jzl.sect.quest.components.EvaluationDimension
import cn.jzl.sect.quest.components.ExecutionResult
import cn.jzl.sect.quest.components.QuestExecutionComponent

/**
 * 长老评估服务
 *
 * 提供长老评估管理功能的核心服务：
 * - 评估单个弟子
 * - 提名候选人
 * - 计算最终分数（应用性格修正）
 *
 * 使用方式：
 * ```kotlin
 * val elderEvaluationService by world.di.instance<ElderEvaluationService>()
 * val candidates = elderEvaluationService.nominateCandidates(outerDisciples, quota, elderId)
 * ```
 *
 * @property world ECS 世界实例
 */
class ElderEvaluationService(override val world: World) : EntityRelationContext {

    /**
     * 评估单个弟子
     *
     * @param elderPersonality 长老性格
     * @param executionResult 任务执行结果
     * @param discipleTalent 弟子天赋
     * @return 评估得分（0.0 - 1.0）
     */
    fun evaluateDisciple(
        elderPersonality: ElderPersonality,
        executionResult: ExecutionResult,
        discipleTalent: Talent
    ): Double {
        // 计算基础分数
        val baseScore = calculateBaseScore(elderPersonality, executionResult)

        // 计算最终分数（应用性格修正）
        return calculateFinalScore(baseScore, elderPersonality, discipleTalent)
    }

    /**
     * 提名候选人
     *
     * @param outerDisciples 外门弟子列表
     * @param quota 名额
     * @param elderId 长老实体ID
     * @return 候选人评分列表（按分数降序）
     */
    fun nominateCandidates(
        outerDisciples: List<Entity>,
        quota: Int,
        elderId: Entity
    ): List<CandidateScore> {
        // 获取长老性格
        val elderPersonality = getElderPersonality(elderId)
            ?: ElderPersonality.impartial() // 默认公正型

        // 获取长老的执行结果（这里假设长老有一个代表性的执行结果）
        // 实际场景中可能需要根据具体任务获取
        val executionResult = getRepresentativeExecutionResult(elderId)
            ?: ExecutionResult(
                completionRate = 0.8f,
                efficiency = 0.7f,
                quality = 0.75f,
                survivalRate = 0.9f,
                casualties = 0
            )

        // 计算每个弟子的得分
        val candidateScores = outerDisciples.mapNotNull { disciple ->
            val talent = getDiscipleTalent(disciple) ?: Talent()
            val score = evaluateDisciple(elderPersonality, executionResult, talent)

            // 转换为 CandidateScore
            CandidateScore(
                discipleId = disciple,
                totalScore = score,
                dimensionScores = mapOf(
                    EvaluationDimension.CULTIVATION to (talent.physique / 100.0),
                    EvaluationDimension.COMBAT to (talent.comprehension / 100.0),
                    EvaluationDimension.LOYALTY to (talent.fortune / 100.0),
                    EvaluationDimension.EXPERIENCE to (talent.charm / 100.0),
                    EvaluationDimension.SPECIALTY to score
                )
            )
        }

        // 按分数降序排序，取前150%名额的候选人
        val targetCount = (quota * 1.5).toInt().coerceAtLeast(quota)
        return candidateScores
            .sortedByDescending { it.totalScore }
            .take(targetCount)
    }

    /**
     * 计算最终分数（应用性格修正）
     *
     * @param baseScore 基础分数
     * @param elderPersonality 长老性格
     * @param discipleTalent 弟子天赋
     * @return 最终分数
     */
    fun calculateFinalScore(
        baseScore: Double,
        elderPersonality: ElderPersonality,
        discipleTalent: Talent
    ): Double {
        var finalScore = baseScore

        when (elderPersonality.type) {
            ElderPersonalityType.IMPARTIAL -> {
                // 公正型：各维度权重均衡，不额外修正
                finalScore = baseScore
            }

            ElderPersonalityType.BIASED -> {
                // 偏私型：根据偏好属性加分
                val bonus = when (elderPersonality.preference) {
                    ElderPreference.HIGH_PHYSIQUE -> {
                        // 偏爱高根骨：根骨>70加10%
                        if (discipleTalent.physique > 70) 0.10 else 0.0
                    }
                    ElderPreference.HIGH_DILIGENCE -> {
                        // 偏爱高勤勉：需要通过Personality8判断，这里简化处理
                        // 假设福缘代表勤勉程度
                        if (discipleTalent.fortune > 70) 0.10 else 0.0
                    }
                    ElderPreference.NONE -> 0.0
                }
                finalScore = baseScore + bonus
            }

            ElderPersonalityType.STRICT -> {
                // 严苛型：评分标准×1.2（意味着更难得高分）
                // 这里理解为：严苛长老对分数要求更高，需要乘以1.2的系数来"提升"标准
                // 即：同样的表现，严苛长老给的分数 = 基础分 / 1.2
                finalScore = baseScore / 1.2
            }

            ElderPersonalityType.LENIENT -> {
                // 宽松型：评分标准×0.8（意味着更容易得高分）
                // 同样的表现，宽松长老给的分数 = 基础分 / 0.8
                finalScore = baseScore / 0.8
            }
        }

        // 确保分数在0-1范围内
        return finalScore.coerceIn(0.0, 1.0)
    }

    /**
     * 计算基础分数
     */
    private fun calculateBaseScore(
        elderPersonality: ElderPersonality,
        executionResult: ExecutionResult
    ): Double {
        val weights = elderPersonality.baseWeights

        return (
            executionResult.completionRate * weights.completionRate +
            executionResult.efficiency * weights.efficiency +
            executionResult.quality * weights.quality +
            executionResult.survivalRate * weights.survivalRate
        ).toDouble()
    }

    /**
     * 获取长老的性格
     */
    private fun getElderPersonality(elderId: Entity): ElderPersonality? {
        val query = world.query { ElderPersonalityQueryContext(world) }
        var personality: ElderPersonality? = null

        query.forEach { ctx ->
            if (ctx.entity == elderId) {
                personality = ctx.elderPersonality
            }
        }

        return personality
    }

    /**
     * 获取弟子的天赋
     */
    private fun getDiscipleTalent(disciple: Entity): Talent? {
        val query = world.query { TalentQueryContext(world) }
        var talent: Talent? = null

        query.forEach { ctx ->
            if (ctx.entity == disciple) {
                talent = ctx.talent
            }
        }

        return talent
    }

    /**
     * 获取长老的代表性执行结果
     * 这里简化处理，实际可能需要根据历史任务记录计算
     */
    private fun getRepresentativeExecutionResult(elderId: Entity): ExecutionResult? {
        // 查询该长老相关的任务执行记录
        val query = world.query { QuestExecutionQueryContext(world) }
        var result: ExecutionResult? = null

        query.forEach { ctx ->
            if (ctx.execution.elderId == elderId.id.toLong()) {
                // 简化：返回一个基于任务执行组件构造的结果
                // 实际应该根据历史记录计算平均值
                result = ExecutionResult(
                    completionRate = 0.75f,
                    efficiency = 0.70f,
                    quality = 0.72f,
                    survivalRate = 0.85f,
                    casualties = 0
                )
            }
        }

        return result
    }

    /**
     * 查询上下文 - 长老性格
     */
    class ElderPersonalityQueryContext(world: World) : EntityQueryContext(world) {
        val elderPersonality: ElderPersonality by component()
    }

    /**
     * 查询上下文 - 天赋
     */
    class TalentQueryContext(world: World) : EntityQueryContext(world) {
        val talent: Talent by component()
    }

    /**
     * 查询上下文 - 任务执行
     */
    class QuestExecutionQueryContext(world: World) : EntityQueryContext(world) {
        val execution: QuestExecutionComponent by component()
    }
}

/**
 * 批量提名候选人（多个长老）
 *
 * @param elderDisciplePairs 长老和对应外门弟子的配对列表
 * @param quotaPerElder 每个长老的名额
 * @return 提名结果列表
 */
fun ElderEvaluationService.nominateCandidatesBatch(
    elderDisciplePairs: List<Pair<Entity, List<Entity>>>,
    quotaPerElder: Int
): List<NominationResult> {
    return elderDisciplePairs.map { (elder, disciples) ->
        val candidates = nominateCandidates(disciples, quotaPerElder, elder)
        NominationResult(
            elderId = elder.id.toLong(),
            candidates = candidates,
            quota = quotaPerElder,
            actualCount = candidates.size
        )
    }
}
