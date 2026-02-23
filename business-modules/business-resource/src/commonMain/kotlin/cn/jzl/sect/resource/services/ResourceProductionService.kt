/**
 * 资源生产服务
 *
 * 提供宗门资源产出管理功能：
 * - 每日资源产出计算
 * - 月度资源产出统计
 * - 按资源类型汇总产出
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
import cn.jzl.sect.core.sect.SectTreasury
import cn.jzl.sect.resource.components.ResourceProduction
import cn.jzl.sect.resource.components.ResourceType
import cn.jzl.sect.resource.components.calculateOutput
import cn.jzl.sect.resource.components.displayName

/**
 * 资源生产服务
 *
 * 提供宗门资源产出管理功能的核心服务：
 * - 每日资源产出计算
 * - 月度资源产出统计
 * - 按资源类型汇总产出
 *
 * 使用方式：
 * ```kotlin
 * val productionService by world.di.instance<ResourceProductionService>()
 * val records = productionService.dailyProduction()
 * ```
 *
 * @property world ECS 世界实例
 */
class ResourceProductionService(override val world: World) : EntityRelationContext {

    /**
     * 每日资源产出
     * @return 产出记录列表
     */
    fun dailyProduction(): List<ProductionRecord> {
        val records = mutableListOf<ProductionRecord>()
        val query = world.query { ProductionQueryContext(this) }

        query.forEach { ctx ->
            val production = ctx.production
            if (production.isActive) {
                val output = production.calculateOutput()
                val treasury = ctx.sectTreasury

                // 更新宗门资源
                val newSpiritStones = when (production.type) {
                    ResourceType.SPIRIT_STONE -> treasury.spiritStones + output
                    else -> treasury.spiritStones
                }

                world.editor(ctx.entity) {
                    it.addComponent(
                        SectTreasury(
                            spiritStones = newSpiritStones,
                            contributionPoints = treasury.contributionPoints
                        )
                    )
                }

                records.add(
                    ProductionRecord(
                        entity = ctx.entity,
                        resourceType = production.type,
                        amount = output,
                        efficiency = production.efficiency
                    )
                )
            }
        }

        return records
    }

    /**
     * 每月资源产出（30天）
     * @return 月度产出统计
     */
    fun monthlyProduction(): MonthlyProductionSummary {
        val dailyRecords = mutableListOf<List<ProductionRecord>>()

        repeat(30) {
            dailyRecords.add(dailyProduction())
        }

        // 统计各类资源产出
        val spiritStoneTotal = dailyRecords.flatten()
            .filter { it.resourceType == ResourceType.SPIRIT_STONE }
            .sumOf { it.amount }

        val herbTotal = dailyRecords.flatten()
            .filter { it.resourceType == ResourceType.HERB }
            .sumOf { it.amount }

        val oreTotal = dailyRecords.flatten()
            .filter { it.resourceType == ResourceType.ORE }
            .sumOf { it.amount }

        val foodTotal = dailyRecords.flatten()
            .filter { it.resourceType == ResourceType.FOOD }
            .sumOf { it.amount }

        return MonthlyProductionSummary(
            spiritStones = spiritStoneTotal,
            herbs = herbTotal,
            ores = oreTotal,
            food = foodTotal,
            totalDays = 30
        )
    }

    /**
     * 汇总所有资源产出
     * @return 按资源类型汇总的产出映射
     */
    fun summarizeProductionByResource(): Map<String, Long> {
        val records = dailyProduction()
        return records.groupBy { it.resourceType.name }
            .mapValues { (_, records) -> records.sumOf { it.amount } }
    }

    /**
     * 查询上下文 - 资源生产
     */
    class ProductionQueryContext(world: World) : EntityQueryContext(world) {
        val production: ResourceProduction by component()
        val sectTreasury: SectTreasury by component()
    }
}

/**
 * 产出记录
 */
data class ProductionRecord(
    val entity: Entity,
    val resourceType: ResourceType,
    val amount: Long,
    val efficiency: Float
) {
    fun toDisplayString(): String {
        val efficiencyPercent = (efficiency * 100).toInt()
        return "${resourceType.displayName} +${amount} (效率: ${efficiencyPercent}%)"
    }
}

/**
 * 月度产出统计
 */
data class MonthlyProductionSummary(
    val spiritStones: Long,
    val herbs: Long,
    val ores: Long,
    val food: Long,
    val totalDays: Int
) {
    fun toDisplayString(): String {
        return """
            月度资源产出统计 (${totalDays}天):
            灵石: +$spiritStones
            草药: +$herbs
            矿石: +$ores
            粮食: +$food
        """.trimIndent()
    }
}
