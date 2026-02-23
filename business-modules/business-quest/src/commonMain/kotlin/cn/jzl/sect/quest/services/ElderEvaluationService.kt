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
import cn.jzl.sect.core.cultivation.Talent
import cn.jzl.sect.core.quest.CandidateScore
import cn.jzl.sect.core.quest.ElderPersonality
import cn.jzl.sect.core.quest.ExecutionResult
import cn.jzl.sect.quest.systems.ElderEvaluationSystem
import cn.jzl.sect.quest.systems.NominationResult

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

    private val elderEvaluationSystem by lazy {
        ElderEvaluationSystem(world)
    }

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
        return elderEvaluationSystem.evaluateDisciple(elderPersonality, executionResult, discipleTalent)
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
        return elderEvaluationSystem.nominateCandidates(outerDisciples, quota, elderId)
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
        return elderEvaluationSystem.calculateFinalScore(baseScore, elderPersonality, discipleTalent)
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
