package cn.jzl.sect.core.facility

/**
 * 设施基础信息组件
 */
data class Facility(
    val name: String,
    val type: FacilityType,
    val level: Int = 1,
    val maxLevel: Int = 10,
    val capacity: Int = 0,
    val efficiency: Float = 1.0f
) {
    init {
        require(name.isNotBlank()) { "设施名称不能为空" }
        require(level > 0) { "设施等级必须大于0" }
        require(maxLevel > 0) { "最大等级必须大于0" }
        require(level <= maxLevel) { "当前等级不能超过最大等级" }
    }

    /**
     * 是否可以升级
     */
    fun canUpgrade(): Boolean = level < maxLevel

    /**
     * 升级后的设施
     */
    fun upgraded(): Facility = copy(level = level + 1)
}

/**
 * 设施状态组件
 */
data class FacilityStatus(
    val isActive: Boolean = true,
    val maintenanceCost: Int = 10
) {
    init {
        require(maintenanceCost >= 0) { "维护费用不能为负数" }
    }
}

/**
 * 设施产出组件
 */
data class FacilityProduction(
    val productionType: ResourceType,
    val baseAmount: Int,
    val efficiency: Float = 1.0f
) {
    init {
        require(baseAmount >= 0) { "基础产出不能为负数" }
        require(efficiency > 0) { "效率必须大于0" }
    }

    /**
     * 计算实际产出
     * @param facilityLevel 设施等级
     * @return 实际产出数量
     */
    fun calculateActualOutput(facilityLevel: Int): Int {
        val levelBonus = 1 + (facilityLevel - 1) * 0.1f // 每级提升10%
        return (baseAmount * efficiency * levelBonus).toInt()
    }
}

/**
 * 资源类型枚举
 */
enum class ResourceType(val displayName: String) {
    SPIRIT_STONE("灵石"),
    CONTRIBUTION_POINT("贡献点"),
    MEDICINE("丹药"),
    EQUIPMENT("装备"),
    CULTIVATION_SPEED("修炼速度");
}

/**
 * 设施建造成本
 */
data class FacilityCost(
    val spiritStones: Int,
    val contributionPoints: Int
) {
    init {
        require(spiritStones >= 0) { "灵石成本不能为负数" }
        require(contributionPoints >= 0) { "贡献点成本不能为负数" }
    }

    companion object {
        /**
         * 获取设施建造成本
         */
        fun getConstructionCost(type: FacilityType): FacilityCost {
            return when (type) {
                FacilityType.CULTIVATION_ROOM -> FacilityCost(500, 100)
                FacilityType.ALCHEMY_ROOM -> FacilityCost(800, 200)
                FacilityType.FORGE_ROOM -> FacilityCost(700, 150)
                FacilityType.LIBRARY -> FacilityCost(600, 300)
                FacilityType.WAREHOUSE -> FacilityCost(400, 50)
                FacilityType.DORMITORY -> FacilityCost(300, 80)
                FacilityType.SPIRIT_STONE_MINE -> FacilityCost(1000, 150)
                FacilityType.CONTRIBUTION_HALL -> FacilityCost(600, 300)
            }
        }

        /**
         * 获取设施升级成本
         * @param currentLevel 当前等级
         * @return 升级成本（每级成本增加50%）
         */
        fun getUpgradeCost(type: FacilityType, currentLevel: Int): FacilityCost {
            val baseCost = getConstructionCost(type)
            val multiplier = 1 + (currentLevel - 1) * 0.5f
            return FacilityCost(
                spiritStones = (baseCost.spiritStones * multiplier * 0.5f).toInt(),
                contributionPoints = (baseCost.contributionPoints * multiplier * 0.5f).toInt()
            )
        }
    }
}

/**
 * 设施产出配置
 */
object FacilityProductionConfig {
    /**
     * 获取设施基础产出
     */
    fun getBaseProduction(type: FacilityType): FacilityProduction {
        return when (type) {
            FacilityType.CULTIVATION_ROOM -> FacilityProduction(
                productionType = ResourceType.CULTIVATION_SPEED,
                baseAmount = 10 // 每小时增加10点修炼速度
            )
            FacilityType.ALCHEMY_ROOM -> FacilityProduction(
                productionType = ResourceType.MEDICINE,
                baseAmount = 5 // 每小时产出5个丹药
            )
            FacilityType.FORGE_ROOM -> FacilityProduction(
                productionType = ResourceType.EQUIPMENT,
                baseAmount = 3 // 每小时产出3件装备
            )
            FacilityType.LIBRARY -> FacilityProduction(
                productionType = ResourceType.CULTIVATION_SPEED,
                baseAmount = 5 // 每小时增加5点修炼速度
            )
            FacilityType.WAREHOUSE -> FacilityProduction(
                productionType = ResourceType.SPIRIT_STONE,
                baseAmount = 0 // 仓库无产出，只增加存储容量
            )
            FacilityType.DORMITORY -> FacilityProduction(
                productionType = ResourceType.CONTRIBUTION_POINT,
                baseAmount = 2 // 每小时产出2贡献点
            )
            FacilityType.SPIRIT_STONE_MINE -> FacilityProduction(
                productionType = ResourceType.SPIRIT_STONE,
                baseAmount = 20 // 每小时产出20灵石
            )
            FacilityType.CONTRIBUTION_HALL -> FacilityProduction(
                productionType = ResourceType.CONTRIBUTION_POINT,
                baseAmount = 10 // 每小时产出10贡献点
            )
        }
    }

    /**
     * 获取设施基础维护费用
     */
    fun getBaseMaintenanceCost(type: FacilityType): Int {
        return when (type) {
            FacilityType.CULTIVATION_ROOM -> 5
            FacilityType.ALCHEMY_ROOM -> 8
            FacilityType.FORGE_ROOM -> 7
            FacilityType.LIBRARY -> 6
            FacilityType.WAREHOUSE -> 3
            FacilityType.DORMITORY -> 4
            FacilityType.SPIRIT_STONE_MINE -> 10
            FacilityType.CONTRIBUTION_HALL -> 6
        }
    }
}
