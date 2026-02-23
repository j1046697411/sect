/**
 * 设施建造服务
 *
 * 提供宗门设施建造管理功能：
 * - 检查是否可以建造设施
 * - 建造新设施
 * - 管理建造资源消耗
 */
package cn.jzl.sect.building.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.facility.components.Facility
import cn.jzl.sect.facility.components.FacilityCost
import cn.jzl.sect.facility.components.FacilityProduction
import cn.jzl.sect.facility.components.FacilityProductionConfig
import cn.jzl.sect.facility.components.FacilityStatus

/**
 * 设施建造服务
 *
 * 提供宗门设施建造管理功能的核心服务：
 * - 检查是否可以建造设施
 * - 建造新设施
 * - 管理建造资源消耗
 *
 * 使用方式：
 * ```kotlin
 * val constructionService by world.di.instance<FacilityConstructionService>()
 * val result = constructionService.build("灵脉", FacilityType.SPIRIT_VEIN)
 * ```
 *
 * @property world ECS 世界实例
 */
class FacilityConstructionService(override val world: World) : EntityRelationContext {

    /**
     * 检查是否可以建造设施
     * @param type 设施类型
     * @return 建造结果
     */
    fun canBuild(type: FacilityType): BuildCheckResult {
        val cost = FacilityCost.getConstructionCost(type)
        // 这里应该检查宗门资源是否充足
        // 暂时返回可以建造
        return BuildCheckResult(true, "可以建造", cost)
    }

    /**
     * 建造设施
     * @param name 设施名称
     * @param type 设施类型
     * @return 建造结果
     */
    fun build(name: String, type: FacilityType): BuildResult {
        // 检查是否可以建造
        val checkResult = canBuild(type)
        if (!checkResult.canBuild) {
            return BuildResult(false, checkResult.reason, null)
        }

        // 扣除资源
        val cost = checkResult.cost
        if (!deductResources(cost)) {
            return BuildResult(false, "扣除资源失败", null)
        }

        // 创建设施实体
        val facility = createFacilityEntity(name, type)

        return BuildResult(true, "建造成功", facility)
    }

    /**
     * 创建设施实体
     */
    private fun createFacilityEntity(name: String, type: FacilityType): Entity {
        val production = FacilityProductionConfig.getBaseProduction(type)
        val maintenanceCost = FacilityProductionConfig.getBaseMaintenanceCost(type)

        return world.entity {
            it.addComponent(Facility(name = name, type = type))
            it.addComponent(FacilityStatus(isActive = true, maintenanceCost = maintenanceCost))
            it.addComponent(FacilityProduction(
                productionType = production.productionType,
                baseAmount = production.baseAmount,
                efficiency = production.efficiency
            ))
        }
    }

    /**
     * 扣除资源
     */
    private fun deductResources(cost: FacilityCost): Boolean {
        // 这里应该调用资源系统扣除资源
        // 暂时返回成功，实际实现需要与ResourceSystem集成
        return true
    }
}

/**
 * 建造检查结果
 */
data class BuildCheckResult(
    val canBuild: Boolean,
    val reason: String,
    val cost: FacilityCost
)

/**
 * 建造结果
 */
data class BuildResult(
    val success: Boolean,
    val message: String,
    val facility: Entity?
)
