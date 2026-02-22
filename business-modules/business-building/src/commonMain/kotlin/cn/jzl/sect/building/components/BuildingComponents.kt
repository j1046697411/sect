package cn.jzl.sect.building.components

/**
 * 建筑类型枚举
 */
enum class BuildingType(val displayName: String, val description: String) {
    CULTIVATION_ROOM("修炼室", "提升弟子修炼速度"),
    ALCHEMY_LAB("炼丹房", "生产丹药资源"),
    SPIRIT_STONE_MINE("灵石矿", "每小时产出灵石"),
    CONTRIBUTION_HALL("贡献堂", "每小时产出贡献点");

    companion object {
        fun fromString(value: String): BuildingType? {
            return entries.find { it.name == value }
        }
    }
}

/**
 * 资源类型枚举
 */
enum class ResourceType(val displayName: String) {
    SPIRIT_STONE("灵石"),
    CONTRIBUTION_POINT("贡献点"),
    MEDICINE("丹药"),
    CULTIVATION_SPEED("修炼速度");
}

/**
 * 建筑等级组件
 */
data class BuildingLevel(
    val level: Int = 1,
    val maxLevel: Int = 10
) {
    init {
        require(level > 0) { "建筑等级必须大于0" }
        require(maxLevel > 0) { "最大等级必须大于0" }
        require(level <= maxLevel) { "当前等级不能超过最大等级" }
    }

    /**
     * 是否可以升级
     */
    fun canUpgrade(): Boolean = level < maxLevel

    /**
     * 升级后的等级
     */
    fun upgraded(): BuildingLevel = copy(level = level + 1)
}

/**
 * 建筑状态组件
 */
data class BuildingStatus(
    val isActive: Boolean = true,
    val maintenanceCost: Int = 10
) {
    init {
        require(maintenanceCost >= 0) { "维护费用不能为负数" }
    }
}

/**
 * 建筑产出组件
 */
data class BuildingProduction(
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
     * @param buildingLevel 建筑等级
     * @return 实际产出数量
     */
    fun calculateActualOutput(buildingLevel: BuildingLevel): Int {
        val levelBonus = 1 + (buildingLevel.level - 1) * 0.1f // 每级提升10%
        return (baseAmount * efficiency * levelBonus).toInt()
    }
}

/**
 * 建筑基础信息组件
 */
data class BuildingInfo(
    val name: String,
    val buildingType: BuildingType
) {
    init {
        require(name.isNotBlank()) { "建筑名称不能为空" }
    }
}

/**
 * 建筑建造成本
 */
data class BuildingCost(
    val spiritStones: Int,
    val contributionPoints: Int
) {
    init {
        require(spiritStones >= 0) { "灵石成本不能为负数" }
        require(contributionPoints >= 0) { "贡献点成本不能为负数" }
    }

    companion object {
        /**
         * 获取建筑建造成本
         */
        fun getConstructionCost(type: BuildingType): BuildingCost {
            return when (type) {
                BuildingType.CULTIVATION_ROOM -> BuildingCost(500, 100)
                BuildingType.ALCHEMY_LAB -> BuildingCost(800, 200)
                BuildingType.SPIRIT_STONE_MINE -> BuildingCost(1000, 150)
                BuildingType.CONTRIBUTION_HALL -> BuildingCost(600, 300)
            }
        }

        /**
         * 获取建筑升级成本
         * @param currentLevel 当前等级
         * @return 升级成本（每级成本增加50%）
         */
        fun getUpgradeCost(type: BuildingType, currentLevel: Int): BuildingCost {
            val baseCost = getConstructionCost(type)
            val multiplier = 1 + (currentLevel - 1) * 0.5f
            return BuildingCost(
                spiritStones = (baseCost.spiritStones * multiplier * 0.5f).toInt(),
                contributionPoints = (baseCost.contributionPoints * multiplier * 0.5f).toInt()
            )
        }
    }
}

/**
 * 建筑产出配置
 */
object BuildingProductionConfig {
    /**
     * 获取建筑基础产出
     */
    fun getBaseProduction(type: BuildingType): BuildingProduction {
        return when (type) {
            BuildingType.CULTIVATION_ROOM -> BuildingProduction(
                productionType = ResourceType.CULTIVATION_SPEED,
                baseAmount = 10 // 每小时增加10点修炼速度
            )
            BuildingType.ALCHEMY_LAB -> BuildingProduction(
                productionType = ResourceType.MEDICINE,
                baseAmount = 5 // 每小时产出5个丹药
            )
            BuildingType.SPIRIT_STONE_MINE -> BuildingProduction(
                productionType = ResourceType.SPIRIT_STONE,
                baseAmount = 20 // 每小时产出20灵石
            )
            BuildingType.CONTRIBUTION_HALL -> BuildingProduction(
                productionType = ResourceType.CONTRIBUTION_POINT,
                baseAmount = 10 // 每小时产出10贡献点
            )
        }
    }

    /**
     * 获取建筑基础维护费用
     */
    fun getBaseMaintenanceCost(type: BuildingType): Int {
        return when (type) {
            BuildingType.CULTIVATION_ROOM -> 5
            BuildingType.ALCHEMY_LAB -> 8
            BuildingType.SPIRIT_STONE_MINE -> 10
            BuildingType.CONTRIBUTION_HALL -> 6
        }
    }
}
