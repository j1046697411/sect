/**
 * 资源消耗服务
 *
 * 提供宗门资源消耗管理功能：
 * - 月度俸禄发放
 * - 设施维护费用结算
 * - 忠诚度影响计算
 */
package cn.jzl.sect.resource.services

import cn.jzl.ecs.World
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.config.GameConfig
import cn.jzl.sect.core.disciple.SectLoyalty
import cn.jzl.sect.core.facility.Facility
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.core.sect.SectTreasury

/**
 * 资源消耗服务
 *
 * 提供宗门资源消耗管理功能的核心服务：
 * - 月度俸禄发放
 * - 设施维护费用结算
 * - 忠诚度影响计算
 *
 * 使用方式：
 * ```kotlin
 * val consumptionService by world.di.instance<ResourceConsumptionService>()
 * val result = consumptionService.monthlyConsumption()
 * ```
 *
 * @property world ECS 世界实例
 */
class ResourceConsumptionService(override val world: World) : EntityRelationContext {

    private val config = GameConfig.getInstance()

    /**
     * 月度资源消耗结算
     * @return 消耗结算结果
     */
    fun monthlyConsumption(): ConsumptionResult {
        val sect = getSectEntity() ?: return ConsumptionResult(false, 0, 0, 0, emptyList())
        val sectTreasury = getSectTreasury(sect)

        // 计算各项支出
        val salaryCost = calculateSalaryCost()
        val maintenanceCost = calculateMaintenanceCost()
        val totalCost = salaryCost + maintenanceCost

        var remainingSpiritStones = sectTreasury.spiritStones
        val paymentRecords = mutableListOf<PaymentRecord>()

        // 优先支付维护费
        val actualMaintenanceCost = if (remainingSpiritStones >= maintenanceCost) {
            remainingSpiritStones -= maintenanceCost
            maintenanceCost
        } else {
            val partial = remainingSpiritStones
            remainingSpiritStones = 0
            partial
        }

        // 支付俸禄
        val salaryQuery = world.query { SalaryQueryContext(this) }
        salaryQuery.forEach { ctx ->
            val expectedSalary = getSalaryByPosition(ctx.position.position)
            val actualSalary = if (remainingSpiritStones >= expectedSalary) {
                remainingSpiritStones -= expectedSalary
                expectedSalary
            } else {
                val partial = remainingSpiritStones
                remainingSpiritStones = 0
                partial
            }

            val paid = actualSalary == expectedSalary
            updateLoyaltyAfterPayment(ctx.entity, ctx.loyalty, paid)

            paymentRecords.add(
                PaymentRecord(
                    entity = ctx.entity,
                    position = ctx.position.position,
                    expectedAmount = expectedSalary,
                    actualAmount = actualSalary,
                    paid = paid
                )
            )
        }

        // 更新宗门资源
        world.editor(sect) {
            it.addComponent(
                SectTreasury(
                    spiritStones = remainingSpiritStones,
                    contributionPoints = sectTreasury.contributionPoints
                )
            )
        }

        val totalPaid = paymentRecords.sumOf { it.actualAmount }
        val allPaid = paymentRecords.all { it.paid }

        return ConsumptionResult(
            success = allPaid,
            totalCost = totalCost,
            salaryPaid = totalPaid,
            maintenancePaid = actualMaintenanceCost,
            paymentRecords = paymentRecords
        )
    }

    /**
     * 计算俸禄总支出
     */
    private fun calculateSalaryCost(): Long {
        var total = 0L
        val query = world.query { SalaryQueryContext(this) }

        query.forEach { ctx ->
            total += getSalaryByPosition(ctx.position.position)
        }

        return total
    }

    /**
     * 计算设施维护总支出
     */
    private fun calculateMaintenanceCost(): Long {
        var total = 0L
        val query = world.query { FacilityQueryContext(this) }

        query.forEach { ctx ->
            total += calculateFacilityMaintenance(ctx.facility)
        }

        return total
    }

    /**
     * 根据职位获取俸禄
     */
    private fun getSalaryByPosition(position: SectPositionType): Long {
        return config.salary.getMonthlySalary(position)
    }

    /**
     * 计算设施维护费
     */
    private fun calculateFacilityMaintenance(facility: Facility): Long {
        return config.facility.calculateMaintenanceCost(facility.level, facility.efficiency)
    }

    /**
     * 更新忠诚度（根据支付情况）
     */
    private fun updateLoyaltyAfterPayment(entity: Entity, loyalty: SectLoyalty, paid: Boolean) {
        val newLoyalty = if (paid) {
            // 正常发放，忠诚度微增
            (loyalty.value + config.loyalty.loyaltyIncreaseOnPayment).coerceAtMost(100)
        } else {
            // 拖欠俸禄，忠诚度下降
            (loyalty.value - config.loyalty.loyaltyDecreaseOnUnpaid).coerceAtLeast(0)
        }

        val newConsecutiveMonths = if (paid) {
            0
        } else {
            loyalty.consecutiveUnpaidMonths + 1
        }

        world.editor(entity) {
            it.addComponent(
                SectLoyalty(
                    value = newLoyalty,
                    consecutiveUnpaidMonths = newConsecutiveMonths
                )
            )
        }
    }

    /**
     * 获取宗门实体
     */
    private fun getSectEntity(): Entity? {
        val query = world.query { SectQueryContext(this) }
        var sectEntity: Entity? = null
        query.forEach { sectEntity = it.entity }
        return sectEntity
    }

    /**
     * 获取宗门资源
     */
    private fun getSectTreasury(entity: Entity): SectTreasury {
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
     * 计算总维护费用
     * @return 维护费用总额
     */
    fun calculateTotalMaintenanceCost(): Long {
        // 通过执行一次消耗结算来获取维护费用
        val result = monthlyConsumption()
        return result.maintenancePaid
    }

    /**
     * 计算总俸禄支出
     * @return 俸禄支出总额
     */
    fun calculateTotalSalaryCost(): Long {
        // 通过执行一次消耗结算来获取俸禄支出
        val result = monthlyConsumption()
        return result.salaryPaid
    }

    /**
     * 查询上下文 - 俸禄
     */
    class SalaryQueryContext(world: World) : EntityQueryContext(world) {
        val position: SectPositionInfo by component()
        val loyalty: SectLoyalty by component()
    }

    /**
     * 查询上下文 - 设施
     */
    class FacilityQueryContext(world: World) : EntityQueryContext(world) {
        val facility: Facility by component()
    }

    /**
     * 查询上下文 - 宗门
     */
    class SectQueryContext(world: World) : EntityQueryContext(world) {
        val sectTreasury: SectTreasury by component()
    }

    /**
     * 查询上下文 - 宗门金库
     */
    class SectTreasuryQueryContext(world: World) : EntityQueryContext(world) {
        val sectTreasury: SectTreasury by component()
    }
}

/**
 * 消耗结算结果
 */
data class ConsumptionResult(
    val success: Boolean,
    val totalCost: Long,
    val salaryPaid: Long,
    val maintenancePaid: Long,
    val paymentRecords: List<PaymentRecord>
) {
    fun toDisplayString(): String {
        val status = if (success) "✓ 正常" else "✗ 资金不足"
        return """
            月度资源消耗结算 $status
            总支出: $totalCost 灵石
            俸禄支出: $salaryPaid 灵石
            维护支出: $maintenancePaid 灵石
            支付记录: ${paymentRecords.size} 条
        """.trimIndent()
    }
}

/**
 * 支付记录
 */
data class PaymentRecord(
    val entity: Entity,
    val position: SectPositionType,
    val expectedAmount: Long,
    val actualAmount: Long,
    val paid: Boolean
) {
    fun toDisplayString(): String {
        val status = if (paid) "✓" else "✗"
        return "$status ${position.displayName}: ${actualAmount}/${expectedAmount} 灵石"
    }
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
