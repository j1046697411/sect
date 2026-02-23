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
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.resource.systems.ConsumptionResult
import cn.jzl.sect.resource.systems.ResourceConsumptionSystem

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

    private val consumptionSystem by lazy {
        ResourceConsumptionSystem(world)
    }

    /**
     * 月度资源消耗结算
     * @return 消耗结算结果
     */
    fun monthlyConsumption(): ConsumptionResult {
        return consumptionSystem.monthlyConsumption()
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
}
