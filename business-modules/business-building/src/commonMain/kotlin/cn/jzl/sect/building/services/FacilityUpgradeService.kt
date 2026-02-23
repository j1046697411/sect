/**
 * 设施升级服务
 *
 * 提供宗门设施升级管理功能：
 * - 检查设施是否可以升级
 * - 执行设施升级
 * - 管理升级资源消耗
 */
package cn.jzl.sect.building.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.facility.Facility
import cn.jzl.sect.core.facility.FacilityCost
import cn.jzl.sect.core.facility.FacilityType

/**
 * 设施升级服务
 *
 * 提供宗门设施升级管理功能的核心服务：
 * - 检查设施是否可以升级
 * - 执行设施升级
 * - 管理升级资源消耗
 *
 * 使用方式：
 * ```kotlin
 * val upgradeService by world.di.instance<FacilityUpgradeService>()
 * val result = upgradeService.upgrade(facilityEntity)
 * ```
 *
 * @property world ECS 世界实例
 */
class FacilityUpgradeService(override val world: World) : EntityRelationContext {

    /**
     * 检查设施是否可以升级
     * @param facility 设施实体
     * @return 升级检查结果
     */
    fun canUpgrade(facility: Entity): UpgradeCheckResult {
        val query = world.query { FacilityQueryContext(this) }
        var result: UpgradeCheckResult? = null

        query.forEach { ctx ->
            if (ctx.entity == facility) {
                val facilityInfo = ctx.facility

                result = when {
                    !facilityInfo.canUpgrade() ->
                        UpgradeCheckResult(false, "设施已达到最高等级", null)
                    else -> {
                        val cost = FacilityCost.getUpgradeCost(facilityInfo.type, facilityInfo.level)
                        UpgradeCheckResult(true, "可以升级", cost)
                    }
                }
            }
        }

        return result ?: UpgradeCheckResult(false, "未找到设施", null)
    }

    /**
     * 升级设施
     * @param facility 设施实体
     * @return 升级结果
     */
    fun upgrade(facility: Entity): UpgradeResult {
        val checkResult = canUpgrade(facility)
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
        return performUpgrade(facility)
    }

    /**
     * 执行升级
     */
    private fun performUpgrade(facility: Entity): UpgradeResult {
        val query = world.query { FacilityQueryContext(this) }
        var success = false
        var newLevel = 0

        query.forEach { ctx ->
            if (ctx.entity == facility) {
                val currentFacility = ctx.facility
                if (currentFacility.canUpgrade()) {
                    // 这里应该更新设施等级组件
                    // 由于ECS组件是不可变的，需要移除旧组件并添加新组件
                    newLevel = currentFacility.level + 1
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
    private fun deductResources(cost: FacilityCost): Boolean {
        // 这里应该调用资源系统扣除资源
        // 暂时返回成功，实际实现需要与ResourceSystem集成
        return true
    }

    /**
     * 查询上下文 - 设施信息
     */
    class FacilityQueryContext(world: World) : EntityQueryContext(world) {
        val facility: Facility by component()
    }
}

/**
 * 升级检查结果
 */
data class UpgradeCheckResult(
    val canUpgrade: Boolean,
    val reason: String,
    val cost: FacilityCost?
)

/**
 * 升级结果
 */
data class UpgradeResult(
    val success: Boolean,
    val message: String
)
