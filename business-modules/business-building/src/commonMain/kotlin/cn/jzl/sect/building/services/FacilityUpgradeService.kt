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
import cn.jzl.sect.building.systems.FacilityUpgradeSystem
import cn.jzl.sect.building.systems.UpgradeCheckResult
import cn.jzl.sect.building.systems.UpgradeResult

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

    private val upgradeSystem by lazy {
        FacilityUpgradeSystem(world)
    }

    /**
     * 检查设施是否可以升级
     * @param facility 设施实体
     * @return 升级检查结果
     */
    fun canUpgrade(facility: Entity): UpgradeCheckResult {
        return upgradeSystem.canUpgrade(facility)
    }

    /**
     * 升级设施
     * @param facility 设施实体
     * @return 升级结果
     */
    fun upgrade(facility: Entity): UpgradeResult {
        return upgradeSystem.upgrade(facility)
    }
}
