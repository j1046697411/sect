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
import cn.jzl.sect.building.systems.FacilityProductionSystem
import cn.jzl.sect.building.systems.ProductionApplyResult
import cn.jzl.sect.building.systems.ProductionDetail
import cn.jzl.sect.core.facility.ResourceType

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

    private val productionSystem by lazy {
        FacilityProductionSystem(world)
    }

    /**
     * 计算所有设施的总产出
     * @return 产出明细列表
     */
    fun calculateTotalProduction(): List<ProductionDetail> {
        return productionSystem.calculateTotalProduction()
    }

    /**
     * 按资源类型汇总产出
     * @return 资源类型到总产出的映射
     */
    fun summarizeProductionByResource(): Map<ResourceType, Int> {
        return productionSystem.summarizeProductionByResource()
    }

    /**
     * 计算总维护费用
     * @return 总维护费用
     */
    fun calculateTotalMaintenanceCost(): Int {
        return productionSystem.calculateTotalMaintenanceCost()
    }

    /**
     * 应用产出（扣除维护费用并添加产出）
     * @return 应用结果
     */
    fun applyProduction(): ProductionApplyResult {
        return productionSystem.applyProduction()
    }

    /**
     * 获取设施产出详情
     * @param facilityName 设施名称
     * @return 产出详情
     */
    fun getFacilityProduction(facilityName: String): ProductionDetail? {
        return productionSystem.getFacilityProduction(facilityName)
    }
}
