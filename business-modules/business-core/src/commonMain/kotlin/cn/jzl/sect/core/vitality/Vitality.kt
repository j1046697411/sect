package cn.jzl.sect.core.vitality

/**
 * 生命状态组件 - 存储实体的生命值信息
 */
data class Vitality(
    val currentHealth: Int = 100, // 当前生命值
    val maxHealth: Int = 100      // 最大生命值
)
