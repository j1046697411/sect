package cn.jzl.sect.engine.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.resource.ResourceProductionComponent
import cn.jzl.sect.core.resource.ResourceType
import cn.jzl.sect.core.sect.SectResourceComponent

/**
 * 资源生产系统 - 处理宗门的资源产出（灵脉、矿脉等）
 */
class ResourceProductionSystem(private val world: World) {

    /**
     * 每日资源产出
     * @return 生产结果列表
     */
    fun dailyProduction(): List<ProductionResult> {
        val results = mutableListOf<ProductionResult>()

        // 先收集所有产出数据
        val productionQuery = world.query { ProductionQueryContext(this) }
        val outputByType = mutableMapOf<ResourceType, Long>()

        productionQuery.forEach { ctx ->
            val production = ctx.production
            if (production.isActive) {
                val output = production.calculateOutput()
                if (output > 0) {
                    outputByType[production.type] = outputByType.getOrDefault(production.type, 0L) + output
                }
            }
        }

        // 生成结果（不立即更新资源，避免并发问题）
        outputByType.forEach { (type, amount) ->
            if (amount > 0) {
                results.add(ProductionResult(type, amount, "灵脉产出"))
            }
        }

        return results
    }

    /**
     * 每月资源产出（30天）
     * @return 生产结果列表
     */
    fun monthlyProduction(): List<ProductionResult> {
        // 先计算30天的总产出
        val productionQuery = world.query { ProductionQueryContext(this) }
        val outputByType = mutableMapOf<ResourceType, Long>()

        productionQuery.forEach { ctx ->
            val production = ctx.production
            if (production.isActive) {
                val dailyOutput = production.calculateOutput()
                val monthlyOutput = dailyOutput * 30 // 30天
                if (monthlyOutput > 0) {
                    outputByType[production.type] = outputByType.getOrDefault(production.type, 0L) + monthlyOutput
                }
            }
        }

        // 一次性更新资源
        val resourceQuery = world.query { SectResourceQueryContext(this) }
        var targetEntity: cn.jzl.ecs.entity.Entity? = null
        var currentSpiritStones = 0L
        var currentContributionPoints = 0L

        resourceQuery.forEach { ctx ->
            targetEntity = ctx.entity
            currentSpiritStones = ctx.resource.spiritStones
            currentContributionPoints = ctx.resource.contributionPoints
        }

        // 更新资源
        if (targetEntity != null) {
            val spiritStoneOutput = outputByType[ResourceType.SPIRIT_STONE] ?: 0L
            val newSpiritStones = currentSpiritStones + spiritStoneOutput

            world.editor(targetEntity!!) {
                it.addComponent(
                    SectResourceComponent(
                        spiritStones = newSpiritStones,
                        contributionPoints = currentContributionPoints
                    )
                )
            }
        }

        // 生成结果
        return outputByType.map { (type, amount) ->
            ProductionResult(type, amount, "月度总产出")
        }
    }

    /**
     * 查询上下文 - 生产设施
     */
    class ProductionQueryContext(world: World) : EntityQueryContext(world) {
        val production: ResourceProductionComponent by component()
    }

    /**
     * 查询上下文 - 宗门资源
     */
    class SectResourceQueryContext(world: World) : EntityQueryContext(world) {
        val resource: SectResourceComponent by component()
    }

    /**
     * 生产结果
     */
    data class ProductionResult(
        val resourceType: ResourceType,
        val amount: Long,
        val source: String
    ) {
        fun toDisplayString(): String {
            val typeName = when (resourceType) {
                ResourceType.SPIRIT_STONE -> "灵石"
                ResourceType.HERB -> "草药"
                ResourceType.ORE -> "矿石"
                ResourceType.FOOD -> "粮食"
            }
            return "$typeName +$amount ($source)"
        }
    }
}
