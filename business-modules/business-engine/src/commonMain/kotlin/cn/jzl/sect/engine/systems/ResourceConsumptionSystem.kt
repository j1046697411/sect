package cn.jzl.sect.engine.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.disciple.LoyaltyComponent
import cn.jzl.sect.core.facility.FacilityComponent
import cn.jzl.sect.core.sect.Position
import cn.jzl.sect.core.sect.PositionComponent
import cn.jzl.sect.core.sect.SectResourceComponent

/**
 * 资源消耗系统 - 处理宗门的资源消耗（俸禄、设施维护等）
 */
class ResourceConsumptionSystem(private val world: World) {

    /**
     * 每月资源消耗结算
     * @return 消耗结果
     */
    fun monthlyConsumption(): ConsumptionResult {
        // 计算各项消耗
        val salaryCost = calculateSalaryCost()
        val maintenanceCost = calculateMaintenanceCost()
        val totalCost = salaryCost + maintenanceCost

        // 获取当前资源
        var targetEntity: cn.jzl.ecs.entity.Entity? = null
        var currentSpiritStones = 0L
        var currentContributionPoints = 0L
        
        val resourceQuery = world.query { SectResourceQueryContext(this) }
        resourceQuery.forEach { ctx ->
            targetEntity = ctx.entity
            currentSpiritStones = ctx.resource.spiritStones
            currentContributionPoints = ctx.resource.contributionPoints
        }

        // 尝试扣除资源
        val canAfford = currentSpiritStones >= totalCost
        var actualCost = 0L
        var unpaidSalaries = false

        if (canAfford) {
            // 足够支付
            actualCost = totalCost
            if (targetEntity != null) {
                val newAmount = (currentSpiritStones - totalCost).coerceAtLeast(0)
                world.editor(targetEntity!!) {
                    it.addComponent(
                        SectResourceComponent(
                            spiritStones = newAmount,
                            contributionPoints = currentContributionPoints
                        )
                    )
                }
            }
            updateLoyaltyAfterPayment(true)
        } else {
            // 资源不足，优先支付维护费，剩余支付俸禄
            unpaidSalaries = true
            actualCost = currentSpiritStones
            if (targetEntity != null) {
                world.editor(targetEntity!!) {
                    it.addComponent(
                        SectResourceComponent(
                            spiritStones = 0L,
                            contributionPoints = currentContributionPoints
                        )
                    )
                }
            }
            updateLoyaltyAfterPayment(false)
        }

        return ConsumptionResult(
            salaryCost = salaryCost,
            maintenanceCost = maintenanceCost,
            totalCost = totalCost,
            actualPaid = actualCost,
            canAfford = canAfford,
            unpaidSalaries = unpaidSalaries,
            remainingSpiritStones = if (canAfford) currentSpiritStones - totalCost else 0L
        )
    }

    /**
     * 计算俸禄成本
     */
    private fun calculateSalaryCost(): Long {
        var totalSalary = 0L
        val discipleQuery = world.query { DiscipleQueryContext(this) }

        discipleQuery.forEach { ctx ->
            val salary = when (ctx.position.position) {
                Position.LEADER -> 500L      // 掌门月俸
                Position.ELDER -> 300L       // 长老月俸
                Position.DISCIPLE_CORE -> 150L  // 亲传弟子月俸
                Position.DISCIPLE_INNER -> 80L  // 内门弟子月俸
                Position.DISCIPLE_OUTER -> 30L  // 外门弟子月俸
            }
            totalSalary += salary
        }

        return totalSalary
    }

    /**
     * 计算设施维护成本
     */
    private fun calculateMaintenanceCost(): Long {
        var totalMaintenance = 0L
        val facilityQuery = world.query { FacilityQueryContext(this) }

        facilityQuery.forEach { ctx ->
            val maintenance = when (ctx.facility.type) {
                cn.jzl.sect.core.facility.FacilityType.CULTIVATION_ROOM -> 50L * ctx.facility.level
                cn.jzl.sect.core.facility.FacilityType.DORMITORY -> 20L * ctx.facility.level
                cn.jzl.sect.core.facility.FacilityType.ALCHEMY_ROOM -> 100L * ctx.facility.level
                cn.jzl.sect.core.facility.FacilityType.FORGE_ROOM -> 80L * ctx.facility.level
                cn.jzl.sect.core.facility.FacilityType.LIBRARY -> 30L * ctx.facility.level
                cn.jzl.sect.core.facility.FacilityType.WAREHOUSE -> 40L * ctx.facility.level
            }
            totalMaintenance += maintenance
        }

        return totalMaintenance
    }

    /**
     * 根据支付情况更新忠诚度
     */
    private fun updateLoyaltyAfterPayment(paid: Boolean) {
        // 先收集所有需要更新的数据
        val updates = mutableListOf<LoyaltyUpdateData>()
        val discipleQuery = world.query { DiscipleWithLoyaltyQueryContext(this) }

        discipleQuery.forEach { ctx ->
            val currentLoyalty = ctx.loyalty
            val newLoyalty = if (paid) {
                // 正常发放，忠诚度恢复
                (currentLoyalty.value + 5).coerceAtMost(100)
            } else {
                // 未发放，忠诚度下降
                (currentLoyalty.value - 15).coerceAtLeast(0)
            }

            val newConsecutiveMonths = if (paid) {
                0
            } else {
                currentLoyalty.consecutiveUnpaidMonths + 1
            }

            updates.add(
                LoyaltyUpdateData(
                    entity = ctx.entity,
                    newLoyalty = newLoyalty,
                    newConsecutiveMonths = newConsecutiveMonths
                )
            )
        }

        // 批量应用更新
        updates.forEach { data ->
            world.editor(data.entity) {
                it.addComponent(
                    LoyaltyComponent(
                        value = data.newLoyalty,
                        consecutiveUnpaidMonths = data.newConsecutiveMonths
                    )
                )
            }
        }
    }

    /**
     * 查询上下文 - 宗门资源
     */
    class SectResourceQueryContext(world: World) : EntityQueryContext(world) {
        val resource: SectResourceComponent by component()
    }

    /**
     * 查询上下文 - 弟子（带职务）
     */
    class DiscipleQueryContext(world: World) : EntityQueryContext(world) {
        val position: PositionComponent by component()
    }

    /**
     * 查询上下文 - 设施
     */
    class FacilityQueryContext(world: World) : EntityQueryContext(world) {
        val facility: FacilityComponent by component()
    }

    /**
     * 查询上下文 - 弟子（带忠诚度）
     */
    class DiscipleWithLoyaltyQueryContext(world: World) : EntityQueryContext(world) {
        val loyalty: LoyaltyComponent by component()
    }

    /**
     * 忠诚度更新数据
     */
    private data class LoyaltyUpdateData(
        val entity: cn.jzl.ecs.entity.Entity,
        val newLoyalty: Int,
        val newConsecutiveMonths: Int
    )

    /**
     * 消耗结果
     */
    data class ConsumptionResult(
        val salaryCost: Long,           // 俸禄成本
        val maintenanceCost: Long,      // 维护成本
        val totalCost: Long,            // 总成本
        val actualPaid: Long,           // 实际支付
        val canAfford: Boolean,         // 是否支付得起
        val unpaidSalaries: Boolean,    // 是否拖欠俸禄
        val remainingSpiritStones: Long // 剩余灵石
    ) {
        fun toDisplayString(): String {
            return buildString {
                appendLine("【月度资源消耗结算】")
                appendLine("  俸禄支出：$salaryCost 灵石")
                appendLine("  维护费用：$maintenanceCost 灵石")
                appendLine("  总支出：$totalCost 灵石")
                appendLine("  实际支付：$actualPaid 灵石")
                if (unpaidSalaries) {
                    appendLine("  ⚠️ 警告：资源不足，已拖欠俸禄！")
                }
                appendLine("  剩余灵石：$remainingSpiritStones")
            }
        }
    }
}
