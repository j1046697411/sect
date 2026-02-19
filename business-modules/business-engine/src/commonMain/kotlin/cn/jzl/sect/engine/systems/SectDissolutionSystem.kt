package cn.jzl.sect.engine.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.disciple.LoyaltyComponent
import cn.jzl.sect.core.sect.SectResourceComponent

/**
 * 宗门解散系统 - 检测宗门是否破产并处理解散逻辑
 */
class SectDissolutionSystem(private val world: World) {

    /**
     * 检查宗门状态
     * @return 宗门状态检查结果
     */
    fun checkSectStatus(): SectStatus {
        // 获取当前资源
        var currentSpiritStones = 0L
        val resourceQuery = world.query { SectResourceQueryContext(this) }
        resourceQuery.forEach { ctx ->
            currentSpiritStones = ctx.resource.spiritStones
        }

        // 统计叛逃风险弟子数量
        var defectRiskCount = 0
        var totalDisciples = 0
        val loyaltyQuery = world.query { LoyaltyQueryContext(this) }
        loyaltyQuery.forEach { ctx ->
            totalDisciples++
            if (ctx.loyalty.mayDefect()) {
                defectRiskCount++
            }
        }

        // 判断宗门状态
        return when {
            currentSpiritStones <= 0 && totalDisciples == 0 -> {
                SectStatus.DISSOLVED("宗门已解散：资源耗尽且弟子全部离开")
            }
            currentSpiritStones <= 0 -> {
                SectStatus.CRITICAL("宗门危急：资源耗尽，弟子可能即将叛逃")
            }
            defectRiskCount >= totalDisciples / 2 -> {
                SectStatus.CRITICAL("宗门危急：超过半数弟子有叛逃风险")
            }
            defectRiskCount > 0 -> {
                SectStatus.WARNING("宗门警告：有 $defectRiskCount 名弟子可能叛逃")
            }
            else -> {
                SectStatus.NORMAL("宗门运转正常")
            }
        }
    }

    /**
     * 获取宗门财务摘要
     */
    fun getFinancialSummary(): FinancialSummary {
        var currentSpiritStones = 0L
        val resourceQuery = world.query { SectResourceQueryContext(this) }
        resourceQuery.forEach { ctx ->
            currentSpiritStones = ctx.resource.spiritStones
        }

        // 计算月度收支
        val consumptionSystem = ResourceConsumptionSystem(world)
        val productionSystem = ResourceProductionSystem(world)

        // 估算月度消耗（不实际执行）
        var monthlyConsumption = 0L
        val discipleQuery = world.query { DiscipleQueryContext(this) }
        discipleQuery.forEach { ctx ->
            val salary = when (ctx.position.position) {
                cn.jzl.sect.core.sect.Position.LEADER -> 500L
                cn.jzl.sect.core.sect.Position.ELDER -> 300L
                cn.jzl.sect.core.sect.Position.DISCIPLE_CORE -> 150L
                cn.jzl.sect.core.sect.Position.DISCIPLE_INNER -> 80L
                cn.jzl.sect.core.sect.Position.DISCIPLE_OUTER -> 30L
            }
            monthlyConsumption += salary
        }

        // 估算月度产出
        var monthlyProduction = 0L
        val productionQuery = world.query { ProductionQueryContext(this) }
        productionQuery.forEach { ctx ->
            if (ctx.production.isActive) {
                monthlyProduction += ctx.production.calculateOutput() * 30 // 30天
            }
        }

        val netIncome = monthlyProduction - monthlyConsumption
        val canSurviveMonths = if (monthlyConsumption > 0) {
            currentSpiritStones / monthlyConsumption
        } else {
            Long.MAX_VALUE
        }

        return FinancialSummary(
            currentBalance = currentSpiritStones,
            monthlyIncome = monthlyProduction,
            monthlyExpense = monthlyConsumption,
            netIncome = netIncome,
            canSurviveMonths = canSurviveMonths
        )
    }

    /**
     * 查询上下文 - 宗门资源
     */
    class SectResourceQueryContext(world: World) : EntityQueryContext(world) {
        val resource: SectResourceComponent by component()
    }

    /**
     * 查询上下文 - 忠诚度
     */
    class LoyaltyQueryContext(world: World) : EntityQueryContext(world) {
        val loyalty: LoyaltyComponent by component()
    }

    /**
     * 查询上下文 - 弟子
     */
    class DiscipleQueryContext(world: World) : EntityQueryContext(world) {
        val position: cn.jzl.sect.core.sect.PositionComponent by component()
    }

    /**
     * 查询上下文 - 生产设施
     */
    class ProductionQueryContext(world: World) : EntityQueryContext(world) {
        val production: cn.jzl.sect.core.resource.ResourceProductionComponent by component()
    }

    /**
     * 宗门状态
     */
    sealed class SectStatus(val message: String) {
        class NORMAL(message: String) : SectStatus(message)
        class WARNING(message: String) : SectStatus(message)
        class CRITICAL(message: String) : SectStatus(message)
        class DISSOLVED(message: String) : SectStatus(message)

        fun isOperational(): Boolean = this !is DISSOLVED
    }

    /**
     * 财务摘要
     */
    data class FinancialSummary(
        val currentBalance: Long,           // 当前余额
        val monthlyIncome: Long,            // 月收入
        val monthlyExpense: Long,           // 月支出
        val netIncome: Long,                // 净收入
        val canSurviveMonths: Long          // 可维持月数
    ) {
        fun toDisplayString(): String {
            return buildString {
                appendLine("【宗门财务摘要】")
                appendLine("  当前余额：$currentBalance 灵石")
                appendLine("  月度收入：$monthlyIncome 灵石")
                appendLine("  月度支出：$monthlyExpense 灵石")
                val netSign = if (netIncome >= 0) "+" else ""
                appendLine("  净收入：$netSign$netIncome 灵石")
                if (canSurviveMonths == Long.MAX_VALUE) {
                    appendLine("  可维持：无限期")
                } else if (canSurviveMonths <= 0) {
                    appendLine("  ⚠️ 资源即将耗尽！")
                } else {
                    appendLine("  可维持：约 $canSurviveMonths 个月")
                }
            }
        }
    }
}
