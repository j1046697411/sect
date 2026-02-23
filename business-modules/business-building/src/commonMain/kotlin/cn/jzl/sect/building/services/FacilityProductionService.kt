/**
 * 设施产出服务
 *
 * 提供宗门设施产出管理功能：
 * - 计算所有设施的总产出
 * - 按资源类型汇总产出
 * - 计算总维护费用
 * - 应用产出到资源系统
 */
package cn.jzl.sect.building.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.facility.components.Facility
import cn.jzl.sect.facility.components.FacilityProduction
import cn.jzl.sect.facility.components.FacilityStatus
import cn.jzl.sect.facility.components.ResourceType

/**
 * 设施产出服务
 *
 * 提供宗门设施产出管理功能的核心服务：
 * - 计算所有设施的总产出
 * - 按资源类型汇总产出
 * - 计算总维护费用
 * - 应用产出到资源系统
 *
 * 使用方式：
 * ```kotlin
 * val productionService by world.di.instance<FacilityProductionService>()
 * val productions = productionService.calculateTotalProduction()
 * ```
 *
 * @property world ECS 世界实例
 */
class FacilityProductionService(override val world: World) : EntityRelationContext {

    /**
     * 计算所有设施的总产出
     * @return 产出明细列表
     */
    fun calculateTotalProduction(): List<ProductionDetail> {
        val productions = mutableListOf<ProductionDetail>()
        val query = world.query { FacilityProductionQueryContext(this) }

        query.forEach { ctx ->
            if (ctx.status.isActive) {
                val actualOutput = ctx.production.calculateActualOutput(ctx.facility.level)
                productions.add(
                    ProductionDetail(
                        facilityName = ctx.facility.name,
                        facilityType = ctx.facility.type,
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
            val current = summary[detail.resourceType] ?: 0
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
        val query = world.query { FacilityStatusQueryContext(this) }

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
     * 获取设施产出详情
     * @param facilityName 设施名称
     * @return 产出详情
     */
    fun getFacilityProduction(facilityName: String): ProductionDetail? {
        val query = world.query { FacilityProductionQueryContext(this) }
        var result: ProductionDetail? = null

        query.forEach { ctx ->
            if (ctx.facility.name == facilityName && ctx.status.isActive) {
                val actualOutput = ctx.production.calculateActualOutput(ctx.facility.level)
                result = ProductionDetail(
                    facilityName = ctx.facility.name,
                    facilityType = ctx.facility.type,
                    resourceType = ctx.production.productionType,
                    amount = actualOutput,
                    maintenanceCost = ctx.status.maintenanceCost
                )
            }
        }

        return result
    }

    /**
     * 查询上下文 - 设施产出查询
     */
    class FacilityProductionQueryContext(world: World) : EntityQueryContext(world) {
        val facility: Facility by component()
        val status: FacilityStatus by component()
        val production: FacilityProduction by component()
    }

    /**
     * 查询上下文 - 设施状态查询
     */
    class FacilityStatusQueryContext(world: World) : EntityQueryContext(world) {
        val status: FacilityStatus by component()
    }
}

/**
 * 产出明细
 */
data class ProductionDetail(
    val facilityName: String,
    val facilityType: FacilityType,
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
