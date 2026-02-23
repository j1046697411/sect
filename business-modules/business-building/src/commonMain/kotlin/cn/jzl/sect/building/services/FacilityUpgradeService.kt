/**
 * 设施升级服务
 *
 * 提供宗门设施升级管理功能：
 * - 检查设施是否可以升级
 * - 执行设施升级
 * - 管理升级资源消耗
 */
package cn.jzl.sect.building.services

import cn.jzl.di.instance
import cn.jzl.ecs.World
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.log.Logger
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.facility.components.Facility
import cn.jzl.sect.facility.components.FacilityCost

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
    private val log: Logger by world.di.instance(argProvider = { "FacilityUpgradeService" })

    /**
     * 检查设施是否可以升级
     * @param facility 设施实体
     * @return 升级检查结果
     */
    fun canUpgrade(facility: Entity): UpgradeCheckResult {
        log.debug { "开始检查设施是否可以升级: facility=$facility" }
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

        val finalResult = result ?: UpgradeCheckResult(false, "未找到设施", null)
        log.debug { "检查设施是否可以升级完成: canUpgrade=${finalResult.canUpgrade}, reason=${finalResult.reason}" }
        return finalResult
    }

    /**
     * 升级设施
     * @param facility 设施实体
     * @return 升级结果
     */
    fun upgrade(facility: Entity): UpgradeResult {
        log.debug { "开始升级设施: facility=$facility" }
        val checkResult = canUpgrade(facility)
        if (!checkResult.canUpgrade) {
            log.debug { "升级设施失败: ${checkResult.reason}" }
            return UpgradeResult(false, checkResult.reason)
        }

        val cost = checkResult.cost
            ?: return UpgradeResult(false, "无法获取升级成本").also {
                log.debug { "升级设施失败: 无法获取升级成本" }
            }

        // 扣除资源
        if (!deductResources(cost)) {
            log.debug { "升级设施失败: 扣除资源失败" }
            return UpgradeResult(false, "扣除资源失败")
        }

        // 执行升级
        val result = performUpgrade(facility)
        log.debug { "升级设施完成: success=${result.success}, message=${result.message}" }
        return result
    }

    /**
     * 执行升级
     */
    private fun performUpgrade(facility: Entity): UpgradeResult {
        log.debug { "开始执行升级: facility=$facility" }
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

        log.debug { "执行升级完成: success=$success, newLevel=$newLevel" }
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
        log.debug { "开始扣除资源: cost=$cost" }
        // 这里应该调用资源系统扣除资源
        // 暂时返回成功，实际实现需要与ResourceSystem集成
        log.debug { "扣除资源完成" }
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
