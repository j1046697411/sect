package cn.jzl.sect.core.facility

/**
 * 设施类型枚举
 */
enum class FacilityType(
    val displayName: String,
    val description: String
) {
    CULTIVATION_ROOM("修炼室", "提升弟子修炼速度"),
    ALCHEMY_ROOM("炼丹房", "生产丹药资源"),
    FORGE_ROOM("炼器室", "生产装备资源"),
    LIBRARY("藏书阁", "提供功法学习"),
    WAREHOUSE("仓库", "存储资源"),
    DORMITORY("宿舍", "提供弟子居住"),
    SPIRIT_STONE_MINE("灵石矿", "每小时产出灵石"),
    CONTRIBUTION_HALL("贡献堂", "每小时产出贡献点");

    companion object {
        fun fromString(value: String): FacilityType? {
            return entries.find { it.name == value }
        }
    }
}
