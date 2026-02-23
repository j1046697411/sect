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
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.resource.systems.ProductionRecord
import cn.jzl.sect.resource.systems.MonthlyProductionSummary
import cn.jzl.sect.resource.systems.ResourceProductionSystem

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

    private val productionSystem by lazy {
        ResourceProductionSystem(world)
    }

    /**
     * 每日资源产出
     * @return 产出记录列表
     */
    fun dailyProduction(): List<ProductionRecord> {
        return productionSystem.dailyProduction()
    }

    /**
     * 每月资源产出（30天）
     * @return 月度产出统计
     */
    fun monthlyProduction(): MonthlyProductionSummary {
        return productionSystem.monthlyProduction()
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
}
