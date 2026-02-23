/**
 * 修炼服务
 *
 * 提供弟子修炼管理功能：
 * - 修为增长计算
 * - 境界突破处理
 * - 突破事件通知
 */
package cn.jzl.sect.cultivation.services

import cn.jzl.ecs.World
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.config.GameConfig
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.cultivation.components.CultivationProgress
import cn.jzl.sect.cultivation.components.Talent
import kotlin.random.Random

/**
 * 修炼服务
 *
 * 提供弟子修炼管理功能的核心服务：
 * - 修为增长计算
 * - 境界突破处理
 * - 突破事件通知
 *
 * 使用方式：
 * ```kotlin
 * val cultivationService by world.di.instance<CultivationService>()
 * val breakthroughs = cultivationService.update(hours)
 * ```
 *
 * @property world ECS 世界实例
 */
class CultivationService(override val world: World) : EntityRelationContext {

    private val config = GameConfig

    /**
     * 更新修炼状态
     * @param hours 经过的游戏小时数
     * @return 突破事件列表
     */
    fun update(hours: Int): List<BreakthroughEvent> {
        val breakthroughs = mutableListOf<BreakthroughEvent>()
        val query = world.query { CultivatorQueryContext(this) }

        // 收集所有需要更新的数据
        val updates = mutableListOf<CultivatorUpdateData>()

        query.forEach { ctx ->
            val cult = ctx.cultivation
            val talent = ctx.talent
            val pos = ctx.position

            // 计算修为增长（内门弟子有20%加成）
            val gain = calculateCultivationGain(talent, hours, pos.position)
            var newCultivation = cult.cultivation + gain
            var newRealm = cult.realm
            var newLayer = cult.layer
            var newMaxCultivation = cult.maxCultivation
            var breakthrough = false

            // 检查是否达到突破条件
            while (newCultivation >= cult.maxCultivation) {
                newCultivation -= cult.maxCultivation
                val result = tryBreakthrough(cult.realm, cult.layer, talent)
                if (result.success) {
                    newRealm = result.newRealm
                    newLayer = result.newLayer
                    newMaxCultivation = result.newMaxCultivation
                    breakthrough = true
                } else {
                    // 突破失败，修为保留在瓶颈
                    newCultivation = cult.maxCultivation - 1
                    break
                }
            }

            if (breakthrough) {
                breakthroughs.add(
                    BreakthroughEvent(
                        entity = ctx.entity,
                        oldRealm = cult.realm,
                        oldLayer = cult.layer,
                        newRealm = newRealm,
                        newLayer = newLayer,
                        position = pos.position
                    )
                )
            }

            updates.add(
                CultivatorUpdateData(
                    entity = ctx.entity,
                    oldCultivation = cult,
                    newCultivation = newCultivation,
                    newRealm = newRealm,
                    newLayer = newLayer,
                    newMaxCultivation = newMaxCultivation
                )
            )
        }

        // 应用所有更新
        updates.forEach { data ->
            world.editor(data.entity) {
                it.addComponent(
                    CultivationProgress(
                        realm = data.newRealm,
                        layer = data.newLayer,
                        cultivation = data.newCultivation,
                        maxCultivation = data.newMaxCultivation
                    )
                )
            }
        }

        return breakthroughs
    }

    /**
     * 计算修为增长
     * @param talent 弟子天赋
     * @param hours 修炼时间（小时）
     * @param positionType 弟子职位类型，内门弟子有20%加成
     * @return 修为增长值
     */
    private fun calculateCultivationGain(
        talent: Talent,
        hours: Int,
        positionType: SectPositionType = SectPositionType.DISCIPLE_OUTER
    ): Long {
        // 基础增长 = 悟性 * 根骨 / 100 * 时间系数
        val baseGain = (talent.comprehension * talent.physique) / 100.0
        val timeMultiplier = hours * config.cultivation.baseCultivationGainPerHour

        // 内门弟子修炼效率加成 ×1.2
        val positionMultiplier = if (positionType == SectPositionType.DISCIPLE_INNER) 1.2 else 1.0

        return (baseGain * timeMultiplier * positionMultiplier).toLong().coerceAtLeast(1)
    }

    /**
     * 尝试突破境界
     */
    private fun tryBreakthrough(
        currentRealm: Realm,
        currentLayer: Int,
        talent: Talent
    ): BreakthroughResult {
        // 计算突破成功率
        val baseSuccessRate = config.cultivation.getBaseBreakthroughSuccessRate(currentRealm)
        val fortuneBonus = talent.fortune * config.cultivation.fortuneEffectOnBreakthrough
        val finalSuccessRate = (baseSuccessRate + fortuneBonus).coerceIn(0.1, 0.95)

        val success = Random.nextDouble() < finalSuccessRate

        return if (success) {
            val (newRealm, newLayer) = getNextRealmLayer(currentRealm, currentLayer)
            BreakthroughResult(
                success = true,
                newRealm = newRealm,
                newLayer = newLayer,
                newMaxCultivation = getMaxCultivation(newRealm, newLayer)
            )
        } else {
            BreakthroughResult(
                success = false,
                newRealm = currentRealm,
                newLayer = currentLayer,
                newMaxCultivation = getMaxCultivation(currentRealm, currentLayer)
            )
        }
    }

    /**
     * 获取下一境界和层数
     */
    private fun getNextRealmLayer(realm: Realm, layer: Int): Pair<Realm, Int> {
        return when {
            layer < config.cultivation.maxLayerPerRealm -> realm to (layer + 1)
            realm == Realm.MORTAL -> Realm.QI_REFINING to 1
            realm == Realm.QI_REFINING -> Realm.FOUNDATION to 1
            else -> realm to config.cultivation.maxLayerPerRealm // 已达最高
        }
    }

    /**
     * 获取境界最大修为值
     */
    private fun getMaxCultivation(realm: Realm, layer: Int): Long {
        return config.cultivation.getMaxCultivation(realm, layer)
    }

    /**
     * 查询上下文 - 修炼者
     */
    class CultivatorQueryContext(world: World) : EntityQueryContext(world) {
        val cultivation: CultivationProgress by component()
        val talent: Talent by component()
        val position: SectPositionInfo by component()
    }

    /**
     * 更新数据
     */
    private data class CultivatorUpdateData(
        val entity: cn.jzl.ecs.entity.Entity,
        val oldCultivation: CultivationProgress,
        val newCultivation: Long,
        val newRealm: Realm,
        val newLayer: Int,
        val newMaxCultivation: Long
    )

    /**
     * 突破结果
     */
    private data class BreakthroughResult(
        val success: Boolean,
        val newRealm: Realm,
        val newLayer: Int,
        val newMaxCultivation: Long
    )

    /**
     * 突破事件
     */
    data class BreakthroughEvent(
        val entity: cn.jzl.ecs.entity.Entity,
        val oldRealm: Realm,
        val oldLayer: Int,
        val newRealm: Realm,
        val newLayer: Int,
        val position: SectPositionType
    ) {
        fun toDisplayString(): String {
            return "${position.displayName} 突破成功！${oldRealm.displayName}${oldLayer}层 → ${newRealm.displayName}${newLayer}层"
        }
    }
}

/**
 * 境界显示名称扩展
 */
private val Realm.displayName: String
    get() = when (this) {
        Realm.MORTAL -> "凡人"
        Realm.QI_REFINING -> "炼气期"
        Realm.FOUNDATION -> "筑基期"
        Realm.GOLDEN_CORE -> "金丹期"
        Realm.NASCENT_SOUL -> "元婴期"
        Realm.SOUL_TRANSFORMATION -> "化神期"
        Realm.TRIBULATION -> "渡劫期"
        Realm.IMMORTAL -> "成仙"
    }

/**
 * 职务显示名称扩展
 */
private val SectPositionType.displayName: String
    get() = when (this) {
        SectPositionType.DISCIPLE_OUTER -> "外门弟子"
        SectPositionType.DISCIPLE_INNER -> "内门弟子"
        SectPositionType.ELDER -> "长老"
        SectPositionType.LEADER -> "掌门"
    }
