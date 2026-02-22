package cn.jzl.sect.building.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.building.components.*

/**
 * 建筑升级系统
 */
class BuildingUpgradeSystem(private val world: World) {

    /**
     * 检查建筑是否可以升级
     * @param building 建筑实体
     * @return 升级检查结果
     */
    fun canUpgrade(building: Entity): UpgradeCheckResult {
        val query = world.query { BuildingQueryContext(this) }
        var result: UpgradeCheckResult? = null

        query.forEach { ctx ->
            if (ctx.entity == building) {
                val level = ctx.level
                val info = ctx.info

                result = when {
                    !level.canUpgrade() ->
                        UpgradeCheckResult(false, "建筑已达到最高等级", null)
                    else -> {
                        val cost = BuildingCost.getUpgradeCost(info.buildingType, level.level)
                        UpgradeCheckResult(true, "可以升级", cost)
                    }
                }
            }
        }

        return result ?: UpgradeCheckResult(false, "未找到建筑", null)
    }

    /**
     * 升级建筑
     * @param building 建筑实体
     * @return 升级结果
     */
    fun upgrade(building: Entity): UpgradeResult {
        val checkResult = canUpgrade(building)
        if (!checkResult.canUpgrade) {
            return UpgradeResult(false, checkResult.reason)
        }

        val cost = checkResult.cost
            ?: return UpgradeResult(false, "无法获取升级成本")

        // 扣除资源
        if (!deductResources(cost)) {
            return UpgradeResult(false, "扣除资源失败")
        }

        // 执行升级
        return performUpgrade(building)
    }

    /**
     * 执行升级
     */
    private fun performUpgrade(building: Entity): UpgradeResult {
        val query = world.query { BuildingQueryContext(this) }
        var success = false
        var newLevel = 0

        query.forEach { ctx ->
            if (ctx.entity == building) {
                val currentLevel = ctx.level
                if (currentLevel.canUpgrade()) {
                    // 这里应该更新建筑等级组件
                    // 由于ECS组件是不可变的，需要移除旧组件并添加新组件
                    newLevel = currentLevel.level + 1
                    success = true
                }
            }
        }

        return if (success) {
            UpgradeResult(true, "升级成功，当前等级：$newLevel")
        } else {
            UpgradeResult(false, "升级失败")
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
     * 查询上下文 - 建筑信息
     */
    class BuildingQueryContext(world: World) : EntityQueryContext(world) {
        val info: BuildingInfo by component()
        val level: BuildingLevel by component()
    }
}

/**
 * 升级检查结果
 */
data class UpgradeCheckResult(
    val canUpgrade: Boolean,
    val reason: String,
    val cost: BuildingCost?
)

/**
 * 升级结果
 */
data class UpgradeResult(
    val success: Boolean,
    val message: String
)
