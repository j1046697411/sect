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
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.building.systems.BuildCheckResult
import cn.jzl.sect.building.systems.BuildResult
import cn.jzl.sect.building.systems.FacilityConstructionSystem
import cn.jzl.sect.core.facility.FacilityType

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

    private val constructionSystem by lazy {
        FacilityConstructionSystem(world)
    }

    /**
     * 检查是否可以建造设施
     * @param type 设施类型
     * @return 建造检查结果
     */
    fun canBuild(type: FacilityType): BuildCheckResult {
        return constructionSystem.canBuild(type)
    }

    /**
     * 建造设施
     * @param name 设施名称
     * @param type 设施类型
     * @return 建造结果
     */
    fun build(name: String, type: FacilityType): BuildResult {
        return constructionSystem.build(name, type)
    }
}
