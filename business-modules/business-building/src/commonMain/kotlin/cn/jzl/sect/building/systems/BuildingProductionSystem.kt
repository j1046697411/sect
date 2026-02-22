package cn.jzl.sect.building.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.building.components.*

/**
 * 建筑产出系统
 */
class BuildingProductionSystem(private val world: World) {

    /**
     * 计算所有建筑的总产出
     * @return 产出明细列表
     */
    fun calculateTotalProduction(): List<ProductionDetail> {
        val productions = mutableListOf<ProductionDetail>()
        val query = world.query { BuildingProductionQueryContext(this) }

        query.forEach { ctx ->
            if (ctx.status.isActive) {
                val actualOutput = ctx.production.calculateActualOutput(ctx.level)
                productions.add(
                    ProductionDetail(
                        buildingName = ctx.info.name,
                        buildingType = ctx.info.buildingType,
                        resourceType = ctx.production.productionType,
                        amount = actualOutput,
                        maintenanceCost = ctx.status.maintenanceCost
                    )
                )
            }
        }

        return productions
    }

    /**
     * 按资源类型汇总产出
     * @return 资源类型到总产出的映射
     */
    fun summarizeProductionByResource(): Map<ResourceType, Int> {
        val summary = mutableMapOf<ResourceType, Int>()
        val productions = calculateTotalProduction()

        productions.forEach { detail ->
            val current = summary.getOrDefault(detail.resourceType, 0)
            summary[detail.resourceType] = current + detail.amount
        }

        return summary
    }

    /**
     * 计算总维护费用
     * @return 总维护费用
     */
    fun calculateTotalMaintenanceCost(): Int {
        var totalCost = 0
        val query = world.query { BuildingStatusQueryContext(this) }

        query.forEach { ctx ->
            if (ctx.status.isActive) {
                totalCost += ctx.status.maintenanceCost
            }
        }

        return totalCost
    }

    /**
     * 应用产出（扣除维护费用并添加产出）
     * @return 应用结果
     */
    fun applyProduction(): ProductionApplyResult {
        val productions = calculateTotalProduction()
        val summary = summarizeProductionByResource()
        val maintenanceCost = calculateTotalMaintenanceCost()

        // 这里应该调用资源系统添加产出和扣除维护费用
        // 暂时只返回计算结果

        return ProductionApplyResult(
            success = true,
            productions = productions,
            summary = summary,
            totalMaintenanceCost = maintenanceCost,
            message = "产出计算完成"
        )
    }

    /**
     * 获取建筑产出详情
     * @param buildingName 建筑名称
     * @return 产出详情
     */
    fun getBuildingProduction(buildingName: String): ProductionDetail? {
        val query = world.query { BuildingProductionQueryContext(this) }
        var result: ProductionDetail? = null

        query.forEach { ctx ->
            if (ctx.info.name == buildingName && ctx.status.isActive) {
                val actualOutput = ctx.production.calculateActualOutput(ctx.level)
                result = ProductionDetail(
                    buildingName = ctx.info.name,
                    buildingType = ctx.info.buildingType,
                    resourceType = ctx.production.productionType,
                    amount = actualOutput,
                    maintenanceCost = ctx.status.maintenanceCost
                )
            }
        }

        return result
    }

    /**
     * 查询上下文 - 建筑产出查询
     */
    class BuildingProductionQueryContext(world: World) : EntityQueryContext(world) {
        val info: BuildingInfo by component()
        val level: BuildingLevel by component()
        val status: BuildingStatus by component()
        val production: BuildingProduction by component()
    }

    /**
     * 查询上下文 - 建筑状态查询
     */
    class BuildingStatusQueryContext(world: World) : EntityQueryContext(world) {
        val status: BuildingStatus by component()
    }
}

/**
 * 产出明细
 */
data class ProductionDetail(
    val buildingName: String,
    val buildingType: BuildingType,
    val resourceType: ResourceType,
    val amount: Int,
    val maintenanceCost: Int
)

/**
 * 产出应用结果
 */
data class ProductionApplyResult(
    val success: Boolean,
    val productions: List<ProductionDetail>,
    val summary: Map<ResourceType, Int>,
    val totalMaintenanceCost: Int,
    val message: String
)
