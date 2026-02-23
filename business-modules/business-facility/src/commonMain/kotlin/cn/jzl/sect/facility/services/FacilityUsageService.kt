/**
 * 设施使用服务
 *
 * 提供设施使用管理功能：
 * - 设施使用效果查询
 * - 使用成本计算
 * - 使用权限检查
 * - 设施功能描述
 * - 预期收益计算
 *
 * 使用方式：
 * ```kotlin
 * val facilityUsageService by world.di.instance<FacilityUsageService>()
 *
 * // 获取设施使用效果
 * val effect = facilityUsageService.getFacilityEffect(FacilityType.CULTIVATION_ROOM)
 *
 * // 计算使用成本
 * val cost = facilityUsageService.calculateUsageCost(FacilityType.ALCHEMY_ROOM, duration = 2)
 *
 * // 检查是否可以使用
 * val canUse = facilityUsageService.canUseFacility(FacilityType.LIBRARY, contributionPoints = 100)
 * ```
 */
package cn.jzl.sect.facility.services

import cn.jzl.di.instance
import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.log.Logger
import cn.jzl.sect.core.facility.FacilityType

/**
 * 设施使用服务
 *
 * 直接实现设施使用管理功能
 *
 * @property world ECS 世界实例
 */
class FacilityUsageService(override val world: World) : EntityRelationContext {

    private val log: Logger by world.di.instance(argProvider = { "FacilityUsageService" })

    companion object {
        // 基础使用成本
        const val BASE_USAGE_COST = 10
    }

    /**
     * 设施使用效果
     */
    data class FacilityEffect(
        val efficiencyBonus: Double = 0.0,      // 效率加成
        val successRateBonus: Double = 0.0,     // 成功率加成
        val qualityBonus: Double = 0.0,         // 品质加成
        val targetActivity: String = ""         // 目标活动
    )

    /**
     * 获取设施使用效果
     *
     * @param facilityType 设施类型
     * @return 设施效果
     */
    fun getFacilityEffect(facilityType: FacilityType): FacilityEffect {
        log.debug { "开始获取设施使用效果: $facilityType" }
        val effect = when (facilityType) {
            FacilityType.CULTIVATION_ROOM -> FacilityEffect(
                efficiencyBonus = 0.2,  // +20%修炼效率
                targetActivity = "cultivation"
            )
            FacilityType.ALCHEMY_ROOM -> FacilityEffect(
                successRateBonus = 0.15,  // +15%炼丹成功率
                qualityBonus = 0.1,       // +10%品质
                targetActivity = "alchemy"
            )
            FacilityType.FORGE_ROOM -> FacilityEffect(
                successRateBonus = 0.15,  // +15%炼器成功率
                qualityBonus = 0.1,       // +10%品质
                targetActivity = "forging"
            )
            FacilityType.LIBRARY -> FacilityEffect(
                efficiencyBonus = 0.25,   // +25%功法学习速度
                targetActivity = "skill_learning"
            )
            FacilityType.WAREHOUSE -> FacilityEffect(
                efficiencyBonus = 0.1,    // +10%存储效率
                targetActivity = "storage"
            )
            FacilityType.DORMITORY -> FacilityEffect(
                efficiencyBonus = 0.05,   // +5%恢复效率
                targetActivity = "rest"
            )
            FacilityType.SPIRIT_STONE_MINE -> FacilityEffect(
                efficiencyBonus = 0.2,    // +20%灵石产出
                targetActivity = "mining"
            )
            FacilityType.CONTRIBUTION_HALL -> FacilityEffect(
                efficiencyBonus = 0.2,    // +20%贡献点产出
                targetActivity = "contribution"
            )
        }
        log.debug { "设施使用效果获取完成: $facilityType, 效率加成=${effect.efficiencyBonus}" }
        return effect
    }

    /**
     * 计算使用成本
     *
     * @param facilityType 设施类型
     * @param duration 使用时长(小时)
     * @return 使用成本(贡献点)
     */
    fun calculateUsageCost(facilityType: FacilityType, duration: Int): Int {
        log.debug { "开始计算使用成本: $facilityType, 时长=$duration" }
        val baseCost = when (facilityType) {
            FacilityType.CULTIVATION_ROOM -> BASE_USAGE_COST
            FacilityType.ALCHEMY_ROOM -> BASE_USAGE_COST * 2
            FacilityType.FORGE_ROOM -> BASE_USAGE_COST * 2
            FacilityType.LIBRARY -> BASE_USAGE_COST
            FacilityType.WAREHOUSE -> 0  // 免费
            FacilityType.DORMITORY -> 0  // 免费
            FacilityType.SPIRIT_STONE_MINE -> BASE_USAGE_COST
            FacilityType.CONTRIBUTION_HALL -> BASE_USAGE_COST / 2
        }
        val cost = baseCost * duration
        log.debug { "使用成本计算完成: $facilityType, 成本=$cost" }
        return cost
    }

    /**
     * 检查是否可以使用设施
     *
     * @param facilityType 设施类型
     * @param contributionPoints 拥有的贡献点
     * @param duration 使用时长
     * @return 是否可以使用
     */
    fun canUseFacility(
        facilityType: FacilityType,
        contributionPoints: Int,
        duration: Int = 1
    ): Boolean {
        log.debug { "开始检查设施使用权限: $facilityType, 贡献点=$contributionPoints" }
        val cost = calculateUsageCost(facilityType, duration)
        val canUse = contributionPoints >= cost
        log.debug { "设施使用权限检查完成: $facilityType, 结果=$canUse" }
        return canUse
    }

    /**
     * 获取设施功能描述
     *
     * @param facilityType 设施类型
     * @return 功能描述
     */
    fun getFacilityFunctionDescription(facilityType: FacilityType): String {
        return when (facilityType) {
            FacilityType.CULTIVATION_ROOM -> "提供安静的修炼环境，增加20%修炼效率"
            FacilityType.ALCHEMY_ROOM -> "配备炼丹炉和药材，增加15%炼丹成功率和10%品质"
            FacilityType.FORGE_ROOM -> "配备炼器设备和材料，增加15%炼器成功率和10%品质"
            FacilityType.LIBRARY -> "收藏各类功法典籍，增加25%功法学习速度"
            FacilityType.WAREHOUSE -> "存储资源的仓库，增加10%存储效率"
            FacilityType.DORMITORY -> "弟子居住的地方，增加5%恢复效率"
            FacilityType.SPIRIT_STONE_MINE -> "产出灵石的矿脉，增加20%灵石产出"
            FacilityType.CONTRIBUTION_HALL -> "管理贡献点的地方，增加20%贡献点产出"
        }
    }

    /**
     * 获取设施使用提示
     *
     * @param facilityType 设施类型
     * @return 使用提示
     */
    fun getUsageTip(facilityType: FacilityType): String {
        return when (facilityType) {
            FacilityType.CULTIVATION_ROOM -> "建议长时间修炼以获得最大收益"
            FacilityType.ALCHEMY_ROOM -> "准备好足够的药材再开始炼丹"
            FacilityType.FORGE_ROOM -> "准备好足够的材料再开始炼器"
            FacilityType.LIBRARY -> "悟性越高，学习效果越好"
            FacilityType.WAREHOUSE -> "定期整理仓库以优化存储空间"
            FacilityType.DORMITORY -> "良好的休息有助于恢复状态"
            FacilityType.SPIRIT_STONE_MINE -> "定期收获灵石以获得持续收益"
            FacilityType.CONTRIBUTION_HALL -> "完成任务可以获得更多贡献点"
        }
    }

    /**
     * 计算设施使用收益
     *
     * @param facilityType 设施类型
     * @param userLevel 使用者等级
     * @param duration 使用时长
     * @return 预期收益
     */
    fun calculateExpectedBenefit(
        facilityType: FacilityType,
        userLevel: Int,
        duration: Int
    ): Int {
        val baseBenefit = when (facilityType) {
            FacilityType.CULTIVATION_ROOM -> userLevel * 10
            FacilityType.ALCHEMY_ROOM -> userLevel * 15
            FacilityType.FORGE_ROOM -> userLevel * 15
            FacilityType.LIBRARY -> userLevel * 8
            FacilityType.WAREHOUSE -> userLevel * 5
            FacilityType.DORMITORY -> userLevel * 3
            FacilityType.SPIRIT_STONE_MINE -> userLevel * 12
            FacilityType.CONTRIBUTION_HALL -> userLevel * 10
        }
        return baseBenefit * duration
    }
}
