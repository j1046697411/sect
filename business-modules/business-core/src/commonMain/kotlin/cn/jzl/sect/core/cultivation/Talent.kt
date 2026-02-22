package cn.jzl.sect.core.cultivation

/**
 * 修炼天赋组件 - 存储弟子的修炼天赋属性
 */
data class Talent(
    val physique: Int = 50,      // 根骨
    val comprehension: Int = 50, // 悟性
    val fortune: Int = 50,       // 福缘
    val charm: Int = 50          // 魅力
)
