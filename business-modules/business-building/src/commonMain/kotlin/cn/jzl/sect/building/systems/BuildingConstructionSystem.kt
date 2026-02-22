package cn.jzl.sect.building.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.entity
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.addComponent
import cn.jzl.sect.building.components.*
import cn.jzl.sect.core.sect.SectTreasury

/**
 * 建筑建造系统
 */
class BuildingConstructionSystem(private val world: World) {

    /**
     * 检查是否可以建造建筑
     * @param type 建筑类型
     * @return 建造结果
     */
    fun canBuild(type: BuildingType): BuildCheckResult {
        val cost = BuildingCost.getConstructionCost(type)
        val treasury = getSectTreasury()
            ?: return BuildCheckResult(false, "无法获取宗门资源", cost)

        return when {
            treasury.spiritStones < cost.spiritStones ->
                BuildCheckResult(false, "灵石不足（需要${cost.spiritStones}）", cost)
            treasury.contributionPoints < cost.contributionPoints ->
                BuildCheckResult(false, "贡献点不足（需要${cost.contributionPoints}）", cost)
            else -> BuildCheckResult(true, "可以建造", cost)
        }
    }

    /**
     * 建造建筑
     * @param name 建筑名称
     * @param type 建筑类型
     * @return 建造结果
     */
    fun build(name: String, type: BuildingType): BuildResult {
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

        // 创建建筑实体
        val building = createBuildingEntity(name, type)

        return BuildResult(true, "建造成功", building)
    }

    /**
     * 创建建筑实体
     */
    private fun createBuildingEntity(name: String, type: BuildingType): Entity {
        val production = BuildingProductionConfig.getBaseProduction(type)
        val maintenanceCost = BuildingProductionConfig.getBaseMaintenanceCost(type)

        return world.entity {
            it.addComponent(BuildingInfo(name = name, buildingType = type))
            it.addComponent(BuildingLevel(level = 1, maxLevel = 10))
            it.addComponent(BuildingStatus(isActive = true, maintenanceCost = maintenanceCost))
            it.addComponent(BuildingProduction(
                productionType = production.productionType,
                baseAmount = production.baseAmount,
                efficiency = production.efficiency
            ))
        }
    }

    /**
     * 扣除资源
     */
    private fun deductResources(cost: BuildingCost): Boolean {
        // 这里应该调用资源系统扣除资源
        // 暂时返回成功，实际实现需要与ResourceSystem集成
        return true
    }

    /**
     * 获取宗门金库
     */
    private fun getSectTreasury(): SectTreasury? {
        // 这里应该查询宗门金库
        // 暂时返回null，实际实现需要与SectSystem集成
        return null
    }
}

/**
 * 建造检查结果
 */
data class BuildCheckResult(
    val canBuild: Boolean,
    val reason: String,
    val cost: BuildingCost
)

/**
 * 建造结果
 */
data class BuildResult(
    val success: Boolean,
    val message: String,
    val building: Entity?
)
