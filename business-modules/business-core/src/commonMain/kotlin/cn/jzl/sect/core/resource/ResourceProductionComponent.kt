package cn.jzl.sect.core.resource

/**
 * 资源生产组件 - 用于灵脉、矿脉等资源产出设施
 */
data class ResourceProductionComponent(
    val type: ResourceType = ResourceType.SPIRIT_STONE,
    val baseOutput: Long = 100L,        // 基础产出量（每天）
    val efficiency: Float = 1.0f,        // 效率系数
    val isActive: Boolean = true         // 是否激活
) {
    /**
     * 计算实际产出
     */
    fun calculateOutput(): Long {
        if (!isActive) return 0L
        return (baseOutput * efficiency).toLong()
    }
}

/**
 * 资源类型
 */
enum class ResourceType {
    SPIRIT_STONE,    // 灵石
    HERB,            // 草药
    ORE,             // 矿石
    FOOD             // 粮食
}

/**
 * 资源类型显示名称
 */
val ResourceType.displayName: String
    get() = when (this) {
        ResourceType.SPIRIT_STONE -> "灵石"
        ResourceType.HERB -> "草药"
        ResourceType.ORE -> "矿石"
        ResourceType.FOOD -> "粮食"
    }
