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

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.core.ai.Personality6
import cn.jzl.sect.core.quest.CandidateScore
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.quest.systems.PersonalityType
import cn.jzl.sect.quest.systems.PromotionResult
import cn.jzl.sect.quest.systems.PromotionSystem

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

    private val promotionSystem by lazy {
        PromotionSystem(world)
    }

    /**
     * 晋升弟子
     *
     * @param discipleId 要晋升的弟子实体ID
     * @return 晋升结果
     */
    fun promoteDisciple(discipleId: Entity): PromotionResult {
        return promotionSystem.promoteDisciple(discipleId)
    }

    /**
     * 批量晋升候选人
     *
     * @param candidates 候选人评分列表
     * @param quota 晋升名额
     * @return 晋升结果列表
     */
    fun promoteCandidates(candidates: List<CandidateScore>, quota: Int): List<PromotionResult> {
        return promotionSystem.promoteCandidates(candidates, quota)
    }

    /**
     * 生成6维性格属性
     *
     * @return 随机生成的6维性格
     */
    fun generatePersonality6(): Personality6 {
        return promotionSystem.generatePersonality6()
    }

    /**
     * 生成特定类型的性格
     *
     * @param type 性格类型
     * @return 特定类型的6维性格
     */
    fun generatePersonality6ByType(type: PersonalityType): Personality6 {
        return promotionSystem.generatePersonality6ByType(type)
    }

    /**
     * 更新弟子职位
     *
     * @param discipleId 弟子实体ID
     * @param newPosition 新职位
     */
    fun updatePosition(discipleId: Entity, newPosition: SectPositionType) {
        promotionSystem.updatePosition(discipleId, newPosition)
    }

    /**
     * 获取弟子当前职位
     *
     * @param discipleId 弟子实体ID
     * @return 职位类型，如果未找到返回null
     */
    fun getCurrentPosition(discipleId: Entity): SectPositionType? {
        return promotionSystem.getCurrentPosition(discipleId)
    }

    /**
     * 检查弟子是否可以晋升
     *
     * @param discipleId 弟子实体ID
     * @return 是否可以晋升
     */
    fun canPromote(discipleId: Entity): Boolean {
        return promotionSystem.canPromote(discipleId)
    }
}
