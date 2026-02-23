/**
 * 弟子信息服务
 *
 * 提供弟子信息查询和管理功能：
 * - 获取所有弟子列表
 * - 按职位筛选弟子
 * - 获取弟子统计信息
 */
package cn.jzl.sect.disciples.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.cultivation.CultivationProgress
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.cultivation.Talent
import cn.jzl.sect.core.disciple.Age
import cn.jzl.sect.core.disciple.SectLoyalty
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.core.vitality.Spirit
import cn.jzl.sect.core.vitality.Vitality

/**
 * 弟子信息服务
 *
 * 提供弟子信息查询和管理功能的核心服务：
 * - 获取所有弟子列表
 * - 按职位筛选弟子
 * - 获取弟子统计信息
 *
 * 使用方式：
 * ```kotlin
 * val discipleInfoService by world.di.instance<DiscipleInfoService>()
 * val disciples = discipleInfoService.getAllDisciples()
 * val statistics = discipleInfoService.getDiscipleStatistics()
 * ```
 *
 * @property world ECS 世界实例
 */
class DiscipleInfoService(override val world: World) : EntityRelationContext {

    /**
     * 获取所有弟子列表
     *
     * @return 按职位排序的弟子信息列表
     */
    fun getAllDisciples(): List<DiscipleInfo> {
        val disciples = mutableListOf<DiscipleInfo>()
        val query = world.query { DiscipleQueryContext(this) }

        query.forEach { ctx ->
            disciples.add(
                DiscipleInfo(
                    entity = ctx.entity,
                    position = ctx.position.position,
                    realm = ctx.cultivation.realm,
                    layer = ctx.cultivation.layer,
                    cultivation = ctx.cultivation.cultivation,
                    maxCultivation = ctx.cultivation.maxCultivation,
                    progress = calculateProgress(ctx.cultivation),
                    health = ctx.vitality.currentHealth,
                    maxHealth = ctx.vitality.maxHealth,
                    spirit = ctx.spirit.currentSpirit,
                    maxSpirit = ctx.spirit.maxSpirit,
                    age = ctx.age.age,
                    physique = ctx.talent.physique,
                    comprehension = ctx.talent.comprehension,
                    fortune = ctx.talent.fortune,
                    charm = ctx.talent.charm,
                    loyalty = ctx.loyalty.value,
                    loyaltyLevel = ctx.loyalty.getLevel()
                )
            )
        }

        // 按职位等级排序
        return disciples.sortedBy { it.position.sortOrder }
    }

    /**
     * 按职位筛选弟子
     *
     * @param position 职位类型
     * @return 符合条件的弟子列表
     */
    fun getDisciplesByPosition(position: SectPositionType): List<DiscipleInfo> {
        return getAllDisciples().filter { it.position == position }
    }

    /**
     * 获取弟子统计信息
     *
     * @return 弟子统计数据
     */
    fun getDiscipleStatistics(): DiscipleStatistics {
        val allDisciples = getAllDisciples()

        return DiscipleStatistics(
            totalCount = allDisciples.size,
            leaderCount = allDisciples.count { it.position == SectPositionType.LEADER },
            elderCount = allDisciples.count { it.position == SectPositionType.ELDER },
            innerCount = allDisciples.count { it.position == SectPositionType.DISCIPLE_INNER },
            outerCount = allDisciples.count { it.position == SectPositionType.DISCIPLE_OUTER },
            mortalCount = allDisciples.count { it.realm == Realm.MORTAL },
            qiRefiningCount = allDisciples.count { it.realm == Realm.QI_REFINING },
            foundationCount = allDisciples.count { it.realm == Realm.FOUNDATION },
            averageLoyalty = if (allDisciples.isNotEmpty()) {
                allDisciples.map { it.loyalty }.average().toInt()
            } else 0,
            rebelliousCount = allDisciples.count { it.loyaltyLevel == LoyaltyLevel.REBELLIOUS }
        )
    }

    /**
     * 计算修炼进度百分比
     */
    private fun calculateProgress(cultivation: CultivationProgress): Float {
        return (cultivation.cultivation.toFloat() / cultivation.maxCultivation.toFloat())
            .coerceIn(0f, 1f)
    }

    /**
     * 查询上下文 - 弟子
     */
    private class DiscipleQueryContext(world: World) : EntityQueryContext(world) {
        val position: SectPositionInfo by component()
        val cultivation: CultivationProgress by component()
        val talent: Talent by component()
        val vitality: Vitality by component()
        val spirit: Spirit by component()
        val age: Age by component()
        val loyalty: SectLoyalty by component()
    }
}

/**
 * 弟子信息数据类
 */
data class DiscipleInfo(
    val entity: cn.jzl.ecs.entity.Entity,
    val position: SectPositionType,
    val realm: Realm,
    val layer: Int,
    val cultivation: Long,
    val maxCultivation: Long,
    val progress: Float,
    val health: Int,
    val maxHealth: Int,
    val spirit: Int,
    val maxSpirit: Int,
    val age: Int,
    val physique: Int,
    val comprehension: Int,
    val fortune: Int,
    val charm: Int,
    val loyalty: Int,
    val loyaltyLevel: LoyaltyLevel
) {
    fun toDisplayString(): String {
        val progressBar = buildProgressBar(progress, 20)
        return "${position.displayName} | ${realm.displayName}${layer}层 | " +
               "修为: ${cultivation}/${maxCultivation} ${progressBar} | " +
               "气血: ${health}/${maxHealth} | 灵力: ${spirit}/${maxSpirit} | " +
               "年龄: ${age} | 忠诚: ${loyalty}(${loyaltyLevel.displayName})"
    }
}

/**
 * 弟子统计数据类
 */
data class DiscipleStatistics(
    val totalCount: Int,
    val leaderCount: Int,
    val elderCount: Int,
    val innerCount: Int,
    val outerCount: Int,
    val mortalCount: Int,
    val qiRefiningCount: Int,
    val foundationCount: Int,
    val averageLoyalty: Int,
    val rebelliousCount: Int
) {
    fun toDisplayString(): String {
        return """
            弟子统计:
            总人数: $totalCount
            掌门: $leaderCount | 长老: $elderCount
            内门: $innerCount | 外门: $outerCount
            凡人: $mortalCount | 炼气: $qiRefiningCount | 筑基: $foundationCount
            平均忠诚: $averageLoyalty | 叛逆风险: $rebelliousCount
        """.trimIndent()
    }
}

/**
 * 构建进度条
 */
private fun buildProgressBar(progress: Float, length: Int): String {
    val filled = (progress * length).toInt()
    val empty = length - filled
    return "[" + "█".repeat(filled) + "░".repeat(empty) + "]"
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

/**
 * 忠诚度等级
 */
enum class LoyaltyLevel(val displayName: String, val minValue: Int, val maxValue: Int) {
    DEVOTED("忠心耿耿", 80, 100),
    LOYAL("忠诚", 60, 79),
    NEUTRAL("中立", 40, 59),
    DISCONTENT("不满", 20, 39),
    REBELLIOUS("叛逆", 0, 19);

    companion object {
        fun fromValue(value: Int): LoyaltyLevel {
            return values().find { value in it.minValue..it.maxValue } ?: NEUTRAL
        }
    }
}

/**
 * 获取忠诚度等级扩展
 */
private fun SectLoyalty.getLevel(): LoyaltyLevel {
    return LoyaltyLevel.fromValue(this.value)
}
