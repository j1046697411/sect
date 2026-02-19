package cn.jzl.sect.engine.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.cultivation.CultivationComponent
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.disciple.AttributeComponent
import cn.jzl.sect.core.disciple.LoyaltyComponent
import cn.jzl.sect.core.disciple.LoyaltyLevel
import cn.jzl.sect.core.sect.Position
import cn.jzl.sect.core.sect.PositionComponent
import cn.jzl.sect.core.sect.SectComponent
import cn.jzl.sect.core.sect.SectResourceComponent
import cn.jzl.sect.core.time.TimeComponent

/**
 * 宗门信息系统 - 显示宗门和弟子的状态信息
 */
class SectInfoSystem(private val world: World) {

    /**
     * 获取宗门概览信息
     */
    fun getSectOverview(): SectOverview {
        var sectInfo: SectComponent? = null
        var resourceInfo: SectResourceComponent? = null
        var timeInfo: TimeComponent? = null

        // 查询宗门信息
        val sectQuery = world.query { SectQueryContext(this) }
        sectQuery.forEach { ctx ->
            sectInfo = ctx.sect
            resourceInfo = ctx.resource
        }

        // 查询时间信息
        val timeQuery = world.query { TimeQueryContext(this) }
        timeQuery.forEach { ctx ->
            timeInfo = ctx.time
        }

        // 统计弟子信息
        val discipleStats = countDisciples()

        return SectOverview(
            sectName = sectInfo?.name ?: "未知宗门",
            foundedYear = sectInfo?.foundedYear ?: 1,
            currentTime = timeInfo?.toDisplayString() ?: "未知时间",
            spiritStones = resourceInfo?.spiritStones ?: 0,
            contributionPoints = resourceInfo?.contributionPoints ?: 0,
            discipleCount = discipleStats.total,
            leaderCount = discipleStats.leaders,
            elderCount = discipleStats.elders,
            discipleOuterCount = discipleStats.outer,
            discipleInnerCount = discipleStats.inner,
            discipleCoreCount = discipleStats.core
        )
    }

    /**
     * 获取所有弟子列表
     */
    fun getDiscipleList(): List<DiscipleInfo> {
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
                    age = ctx.attribute.age,
                    progress = calculateProgress(ctx.cultivation),
                    loyalty = ctx.loyalty.value,
                    loyaltyLevel = ctx.loyalty.getLevel()
                )
            )
        }

        return disciples.sortedByDescending { it.position.ordinal }
    }

    /**
     * 统计弟子数量
     */
    private fun countDisciples(): DiscipleStats {
        var total = 0
        var leaders = 0
        var elders = 0
        var outer = 0
        var inner = 0
        var core = 0

        val query = world.query { DiscipleQueryContext(this) }
        query.forEach { ctx ->
            total++
            when (ctx.position.position) {
                Position.LEADER -> leaders++
                Position.ELDER -> elders++
                Position.DISCIPLE_OUTER -> outer++
                Position.DISCIPLE_INNER -> inner++
                Position.DISCIPLE_CORE -> core++
            }
        }

        return DiscipleStats(total, leaders, elders, outer, inner, core)
    }

    /**
     * 计算修炼进度百分比
     */
    private fun calculateProgress(cult: CultivationComponent): Float {
        return if (cult.maxCultivation > 0) {
            (cult.cultivation.toFloat() / cult.maxCultivation.toFloat()) * 100
        } else {
            0f
        }
    }

    /**
     * 查询上下文 - 宗门
     */
    class SectQueryContext(world: World) : EntityQueryContext(world) {
        val sect: SectComponent by component()
        val resource: SectResourceComponent by component()
    }

    /**
     * 查询上下文 - 时间
     */
    class TimeQueryContext(world: World) : EntityQueryContext(world) {
        val time: TimeComponent by component()
    }

    /**
     * 查询上下文 - 弟子
     */
    class DiscipleQueryContext(world: World) : EntityQueryContext(world) {
        val cultivation: CultivationComponent by component()
        val attribute: AttributeComponent by component()
        val position: PositionComponent by component()
        val loyalty: LoyaltyComponent by component()
    }

    /**
     * 宗门概览
     */
    data class SectOverview(
        val sectName: String,
        val foundedYear: Int,
        val currentTime: String,
        val spiritStones: Long,
        val contributionPoints: Long,
        val discipleCount: Int,
        val leaderCount: Int,
        val elderCount: Int,
        val discipleOuterCount: Int,
        val discipleInnerCount: Int,
        val discipleCoreCount: Int
    ) {
        fun toDisplayString(): String {
            return buildString {
                appendLine("╔════════════════════════════════════════════════╗")
                appendLine("║           $sectName           ║")
                appendLine("╠════════════════════════════════════════════════╣")
                appendLine("║  创立时间：第 ${foundedYear} 年                        ║")
                appendLine("║  当前时间：$currentTime                    ║")
                appendLine("╠════════════════════════════════════════════════╣")
                appendLine("║  【资源状况】                                    ║")
                appendLine("║  灵石：${spiritStones.toString().padEnd(10)} 贡献点：${contributionPoints.toString().padEnd(10)} ║")
                appendLine("╠════════════════════════════════════════════════╣")
                appendLine("║  【弟子统计】  总计：${discipleCount.toString().padEnd(3)} 人                     ║")
                appendLine("║  掌门：${leaderCount}人  长老：${elderCount}人  亲传：${discipleCoreCount}人            ║")
                appendLine("║  内门：${discipleInnerCount}人  外门：${discipleOuterCount}人                      ║")
                appendLine("╚════════════════════════════════════════════════╝")
            }
        }
    }

    /**
     * 弟子信息
     */
    data class DiscipleInfo(
        val entity: cn.jzl.ecs.entity.Entity,
        val position: Position,
        val realm: Realm,
        val layer: Int,
        val cultivation: Long,
        val maxCultivation: Long,
        val age: Int,
        val progress: Float,
        val loyalty: Int,
        val loyaltyLevel: LoyaltyLevel
    ) {
        fun toDisplayString(): String {
            val progressBar = buildProgressBar(progress)
            return String.format(
                "%-6s | %-6s%2d层 | %5d/%5d | %3.0f%% | %s | 年龄:%3d | 忠诚:%3d | %s",
                position.displayName,
                realm.displayName,
                layer,
                cultivation,
                maxCultivation,
                progress,
                progressBar,
                age,
                loyalty,
                loyaltyLevel.displayName
            )
        }

        private fun buildProgressBar(progress: Float): String {
            val filled = (progress / 10).toInt().coerceIn(0, 10)
            val empty = 10 - filled
            return "[" + "█".repeat(filled) + "░".repeat(empty) + "]"
        }
    }

    /**
     * 弟子统计
     */
    private data class DiscipleStats(
        val total: Int,
        val leaders: Int,
        val elders: Int,
        val outer: Int,
        val inner: Int,
        val core: Int
    )
}

/**
 * 境界显示名称扩展
 */
private val Realm.displayName: String
    get() = when (this) {
        Realm.MORTAL -> "凡人"
        Realm.QI_REFINING -> "炼气"
        Realm.FOUNDATION -> "筑基"
    }

/**
 * 职务显示名称扩展
 */
private val Position.displayName: String
    get() = when (this) {
        Position.DISCIPLE_OUTER -> "外门"
        Position.DISCIPLE_INNER -> "内门"
        Position.DISCIPLE_CORE -> "亲传"
        Position.ELDER -> "长老"
        Position.LEADER -> "掌门"
    }

/**
 * 忠诚度等级显示名称扩展
 */
private val LoyaltyLevel.displayName: String
    get() = when (this) {
        LoyaltyLevel.DEVOTED -> "忠心耿耿"
        LoyaltyLevel.LOYAL -> "忠诚"
        LoyaltyLevel.NEUTRAL -> "中立"
        LoyaltyLevel.DISCONTENT -> "不满"
        LoyaltyLevel.REBELLIOUS -> "叛逆"
    }
