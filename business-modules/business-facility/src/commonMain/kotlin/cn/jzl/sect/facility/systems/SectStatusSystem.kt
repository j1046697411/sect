package cn.jzl.sect.facility.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.config.GameConfig
import cn.jzl.sect.core.disciple.SectLoyalty
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.Sect
import cn.jzl.sect.core.sect.SectTreasury

/**
 * 宗门状态系统 - 检测宗门健康状况和破产风险
 */
class SectStatusSystem(private val world: World) {

    private val config = GameConfig.getInstance()

    /**
     * 检查宗门状态
     * @return 宗门状态评估
     */
    fun checkSectStatus(): SectStatus {
        val sect = getSect() ?: return SectStatus.NO_SECT
        val treasury = getSectTreasury(sect)

        // 检查资源状况
        val monthlyCost = estimateMonthlyCost()
        val canSurviveMonths = if (monthlyCost > 0) {
            treasury.spiritStones / monthlyCost
        } else {
            Long.MAX_VALUE
        }

        // 检查弟子忠诚度
        val rebelliousCount = countRebelliousDisciples()
        val totalDisciples = countTotalDisciples()
        val rebelliousRatio = if (totalDisciples > 0) {
            rebelliousCount.toFloat() / totalDisciples.toFloat()
        } else {
            0f
        }

        return when {
            // 宗门解散：资源耗尽且没有弟子
            treasury.spiritStones <= 0 && totalDisciples == 0 -> SectStatus.DISSOLVED

            // 宗门危急：资源耗尽或超过半数弟子有叛逃风险
            treasury.spiritStones <= 0 || rebelliousRatio > 0.5f -> SectStatus.CRITICAL

            // 宗门警告：资源不足3个月或超过1/4弟子有叛逃风险
            canSurviveMonths < 3 || rebelliousRatio > 0.25f -> SectStatus.WARNING

            // 宗门正常
            else -> SectStatus.NORMAL
        }
    }

    /**
     * 获取宗门财务摘要
     */
    fun getFinancialSummary(): FinancialSummary {
        val sect = getSect() ?: return FinancialSummary.EMPTY
        val treasury = getSectTreasury(sect)
        val monthlyCost = estimateMonthlyCost()
        val canSurviveMonths = if (monthlyCost > 0) {
            treasury.spiritStones / monthlyCost
        } else {
            Long.MAX_VALUE
        }

        return FinancialSummary(
            spiritStones = treasury.spiritStones,
            contributionPoints = treasury.contributionPoints,
            monthlyCost = monthlyCost,
            canSurviveMonths = canSurviveMonths,
            totalDisciples = countTotalDisciples(),
            rebelliousDisciples = countRebelliousDisciples()
        )
    }

    /**
     * 估算月度支出
     */
    private fun estimateMonthlyCost(): Long {
        var total = 0L

        // 俸禄支出
        val positionQuery = world.query { PositionQueryContext(this) }
        positionQuery.forEach { ctx ->
            total += getSalaryByPosition(ctx.position.position)
        }

        return total
    }

    /**
     * 根据职位获取俸禄
     */
    private fun getSalaryByPosition(position: cn.jzl.sect.core.sect.SectPositionType): Long {
        return config.salary.getMonthlySalary(position)
    }

    /**
     * 统计叛逆弟子数量
     */
    private fun countRebelliousDisciples(): Int {
        var count = 0
        val loyaltyQuery = world.query { LoyaltyQueryContext(this) }
        loyaltyQuery.forEach { ctx ->
            if (config.loyalty.mayDefect(ctx.loyalty.value, ctx.loyalty.consecutiveUnpaidMonths)) {
                count++
            }
        }
        return count
    }

    /**
     * 统计总弟子数量
     */
    private fun countTotalDisciples(): Int {
        var count = 0
        val positionQuery = world.query { PositionQueryContext(this) }
        positionQuery.forEach { count++ }
        return count
    }

    /**
     * 获取宗门实体
     */
    private fun getSect(): cn.jzl.ecs.entity.Entity? {
        val query = world.query { SectQueryContext(this) }
        var sectEntity: cn.jzl.ecs.entity.Entity? = null
        query.forEach { sectEntity = it.entity }
        return sectEntity
    }

    /**
     * 获取宗门资源
     */
    private fun getSectTreasury(entity: cn.jzl.ecs.entity.Entity): SectTreasury {
        val query = world.query { SectTreasuryQueryContext(this) }
        var treasury = SectTreasury()
        query.forEach {
            if (it.entity == entity) {
                treasury = it.sectTreasury
            }
        }
        return treasury
    }

    /**
     * 查询上下文 - 宗门
     */
    class SectQueryContext(world: World) : EntityQueryContext(world) {
        val sect: Sect by component()
    }

    /**
     * 查询上下文 - 宗门金库
     */
    class SectTreasuryQueryContext(world: World) : EntityQueryContext(world) {
        val sectTreasury: SectTreasury by component()
    }

    /**
     * 查询上下文 - 职位
     */
    class PositionQueryContext(world: World) : EntityQueryContext(world) {
        val position: SectPositionInfo by component()
    }

    /**
     * 查询上下文 - 忠诚度
     */
    class LoyaltyQueryContext(world: World) : EntityQueryContext(world) {
        val loyalty: SectLoyalty by component()
    }
}

/**
 * 宗门状态枚举
 */
enum class SectStatus(val displayName: String, val description: String) {
    NORMAL("正常", "宗门运转良好"),
    WARNING("警告", "宗门面临一些困难，需要注意"),
    CRITICAL("危急", "宗门处于危险状态，需要立即采取措施"),
    DISSOLVED("已解散", "宗门已经解散"),
    NO_SECT("无宗门", "没有找到宗门实体");

    fun isOperational(): Boolean = this == NORMAL || this == WARNING
}

/**
 * 财务摘要
 */
data class FinancialSummary(
    val spiritStones: Long,
    val contributionPoints: Long,
    val monthlyCost: Long,
    val canSurviveMonths: Long,
    val totalDisciples: Int,
    val rebelliousDisciples: Int
) {
    companion object {
        val EMPTY = FinancialSummary(0, 0, 0, 0, 0, 0)
    }

    fun toDisplayString(): String {
        val survivalText = if (canSurviveMonths == Long.MAX_VALUE) {
            "无限期"
        } else {
            "${canSurviveMonths}个月"
        }

        return """
            宗门财务摘要:
            灵石储备: $spiritStones
            贡献点: $contributionPoints
            月度支出: $monthlyCost
            可维持: $survivalText
            弟子总数: $totalDisciples
            叛逆风险: $rebelliousDisciples
        """.trimIndent()
    }
}
