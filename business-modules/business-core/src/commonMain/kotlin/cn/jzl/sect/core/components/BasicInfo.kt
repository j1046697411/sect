package cn.jzl.sect.core.components

// 基础信息组件 - 普通 data class
data class EntityName(val value: String) {
    override fun toString(): String = value
}

data class Age(val years: Int)

// 修炼组件
data class CultivationProgress(val percentage: Float)

// 境界密封类
sealed class CultivationRealm(
    val level: Int,
    val displayName: String
) {
    object QiRefining1 : CultivationRealm(1, "炼气一层")
    object QiRefining5 : CultivationRealm(5, "炼气五层")
    object QiRefining9 : CultivationRealm(9, "炼气九层")
    object Foundation : CultivationRealm(10, "筑基期")
    
    override fun toString(): String = displayName
}
