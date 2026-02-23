/**
 * 晋升服务
 *
 * 提供弟子晋升管理功能：
 * - 晋升单个弟子
 * - 批量晋升候选人
 * - 生成6维性格属性
 * - 更新弟子职位
 */
package cn.jzl.sect.quest.services

import cn.jzl.di.instance
import cn.jzl.ecs.World
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.log.Logger
import cn.jzl.sect.core.ai.Personality6
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.cultivation.components.CultivationProgress
import cn.jzl.sect.quest.components.CandidateScore

/**
 * 晋升服务
 *
 * 提供弟子晋升管理功能的核心服务：
 * - 晋升单个弟子
 * - 批量晋升候选人
 * - 生成6维性格属性
 * - 更新弟子职位
 *
 * 使用方式：
 * ```kotlin
 * val promotionService by world.di.instance<PromotionService>()
 * val result = promotionService.promoteDisciple(discipleId)
 * ```
 *
 * @property world ECS 世界实例
 */
class PromotionService(override val world: World) : EntityRelationContext {

    private val log: Logger by world.di.instance(argProvider = { "PromotionService" })

    /**
     * 晋升弟子
     *
     * @param discipleId 要晋升的弟子实体ID
     * @return 晋升结果
     */
    fun promoteDisciple(discipleId: Entity): PromotionResult {
        // 首先检查实体是否存在且有必要的组件
        val query = world.query { DiscipleQueryContext(this) }
        var foundDisciple = false
        var oldPosition: SectPositionType? = null

        query.forEach { ctx ->
            if (ctx.entity == discipleId) {
                foundDisciple = true
                oldPosition = ctx.position.position
            }
        }

        // 如果未找到弟子，返回失败
        if (!foundDisciple || oldPosition == null) {
            return PromotionResult(
                success = false,
                discipleId = discipleId,
                oldPosition = SectPositionType.DISCIPLE_OUTER,
                newPosition = SectPositionType.DISCIPLE_OUTER,
                generatedPersonality = null,
                message = "未找到指定弟子"
            )
        }

        // 只有外门弟子可以晋升为内门弟子
        if (oldPosition != SectPositionType.DISCIPLE_OUTER) {
            return PromotionResult(
                success = false,
                discipleId = discipleId,
                oldPosition = oldPosition!!,
                newPosition = oldPosition!!,
                generatedPersonality = null,
                message = "只有外门弟子可以晋升为内门弟子"
            )
        }

        // 生成6维性格属性
        val personality = generatePersonality6()

        // 更新职位为内门弟子
        return try {
            world.editor(discipleId) {
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_INNER))
                it.addComponent(personality)
            }

            log.debug { "晋升弟子完成: 成功，discipleId=${discipleId}" }
            PromotionResult(
                success = true,
                discipleId = discipleId,
                oldPosition = SectPositionType.DISCIPLE_OUTER,
                newPosition = SectPositionType.DISCIPLE_INNER,
                generatedPersonality = personality,
                message = "晋升成功"
            )
        } catch (e: Exception) {
            log.debug { "晋升弟子完成: 失败，${e.message}" }
            PromotionResult(
                success = false,
                discipleId = discipleId,
                oldPosition = SectPositionType.DISCIPLE_OUTER,
                newPosition = SectPositionType.DISCIPLE_OUTER,
                generatedPersonality = null,
                message = "晋升失败: ${e.message}"
            )
        }
    }

    /**
     * 批量晋升候选人
     *
     * @param candidates 候选人评分列表
     * @param quota 晋升名额
     * @return 晋升结果列表
     */
    fun promoteCandidates(candidates: List<CandidateScore>, quota: Int): List<PromotionResult> {
        val sortedCandidates = candidates.sortedByDescending { it.totalScore }
        val actualQuota = minOf(quota, sortedCandidates.size)
        val results = mutableListOf<PromotionResult>()

        for (i in 0 until actualQuota) {
            val candidate = sortedCandidates[i]
            val result = promoteDisciple(candidate.discipleId)
            results.add(result)
        }

        return results
    }

    /**
     * 生成6维性格属性
     *
     * @return 随机生成的6维性格
     */
    fun generatePersonality6(): Personality6 {
        return Personality6.random()
    }

    /**
     * 生成特定类型的性格
     *
     * @param type 性格类型
     * @return 特定类型的6维性格
     */
    fun generatePersonality6ByType(type: PersonalityType): Personality6 {
        return when (type) {
            PersonalityType.DILIGENT -> Personality6.diligent()
            PersonalityType.AMBITIOUS -> Personality6.ambitious()
            PersonalityType.LOYAL -> Personality6.loyal()
            PersonalityType.RANDOM -> Personality6.random()
        }
    }

    /**
     * 更新弟子职位
     *
     * @param discipleId 弟子实体ID
     * @param newPosition 新职位
     */
    fun updatePosition(discipleId: Entity, newPosition: SectPositionType) {
        log.debug { "开始更新弟子职位: discipleId=${discipleId}, newPosition=$newPosition" }
        world.editor(discipleId) {
            it.addComponent(SectPositionInfo(position = newPosition))
        }
        log.debug { "更新弟子职位完成" }
    }

    /**
     * 获取弟子当前职位
     *
     * @param discipleId 弟子实体ID
     * @return 职位类型，如果未找到返回null
     */
    fun getCurrentPosition(discipleId: Entity): SectPositionType? {
        val query = world.query { DiscipleQueryContext(this) }
        var position: SectPositionType? = null

        query.forEach { ctx ->
            if (ctx.entity == discipleId) {
                position = ctx.position.position
            }
        }

        return position
    }

    /**
     * 检查弟子是否可以晋升
     *
     * @param discipleId 弟子实体ID
     * @return 是否可以晋升
     */
    fun canPromote(discipleId: Entity): Boolean {
        val position = getCurrentPosition(discipleId)
        return position == SectPositionType.DISCIPLE_OUTER
    }

    /**
     * 查询上下文 - 弟子
     */
    class DiscipleQueryContext(world: World) : EntityQueryContext(world) {
        val position: SectPositionInfo by component()
        val cultivation: CultivationProgress by component()
    }
}
