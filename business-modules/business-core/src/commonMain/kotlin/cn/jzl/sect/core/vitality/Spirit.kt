package cn.jzl.sect.core.vitality

/**
 * 精神状态组件 - 存储实体的精神值信息
 */
data class Spirit(
    val currentSpirit: Int = 50, // 当前精神值
    val maxSpirit: Int = 50      // 最大精神值
)
