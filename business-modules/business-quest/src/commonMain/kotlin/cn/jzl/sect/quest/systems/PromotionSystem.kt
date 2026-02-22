package cn.jzl.sect.quest.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.ai.Personality6
import cn.jzl.sect.core.cultivation.CultivationProgress
import cn.jzl.sect.core.quest.CandidateScore
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.SectPositionType

/**
 * 晋升系统 - 处理外门弟子晋升为内门弟子
 */
class PromotionSystem(private val world: World) {

    /**
     * 晋升弟子
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

            PromotionResult(
                success = true,
                discipleId = discipleId,
                oldPosition = SectPositionType.DISCIPLE_OUTER,
                newPosition = SectPositionType.DISCIPLE_INNER,
                generatedPersonality = personality,
                message = "晋升成功"
            )
        } catch (e: Exception) {
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
     * 根据弟子的修炼天赋倾向生成相应性格
     */
    fun generatePersonality6(): Personality6 {
        return Personality6.random()
    }

    /**
     * 生成特定类型的性格
     * @param type 性格类型
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
     * @param discipleId 弟子实体ID
     * @param newPosition 新职位
     */
    fun updatePosition(discipleId: Entity, newPosition: SectPositionType) {
        world.editor(discipleId) {
            it.addComponent(SectPositionInfo(position = newPosition))
        }
    }

    /**
     * 获取弟子当前职位
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

/**
 * 性格类型枚举
 */
enum class PersonalityType {
    DILIGENT,   // 勤勉型
    AMBITIOUS,  // 野心型
    LOYAL,      // 忠诚型
    RANDOM      // 随机型
}

/**
 * 晋升结果
 */
data class PromotionResult(
    val success: Boolean,
    val discipleId: Entity,
    val oldPosition: SectPositionType,
    val newPosition: SectPositionType,
    val generatedPersonality: Personality6?,
    val message: String
) {
    fun toDisplayString(): String {
        return buildString {
            if (success) {
                appendLine("✓ 晋升成功")
                appendLine("  弟子ID: $discipleId")
                appendLine("  $oldPosition → $newPosition")
                generatedPersonality?.let {
                    appendLine("  生成性格: ${it.getPrimaryTrait()}")
                }
            } else {
                appendLine("✗ 晋升失败")
                appendLine("  原因: $message")
            }
        }
    }
}
