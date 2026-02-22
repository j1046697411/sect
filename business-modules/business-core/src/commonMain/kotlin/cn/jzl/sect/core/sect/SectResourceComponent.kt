package cn.jzl.sect.core.sect

/**
 * 宗门金库组件 - 存储宗门的财政资源
 */
data class SectTreasury(
    val spiritStones: Long = 1000L,
    val contributionPoints: Long = 0L
)
