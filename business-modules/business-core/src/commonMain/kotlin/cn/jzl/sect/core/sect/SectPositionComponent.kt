package cn.jzl.sect.core.sect

/**
 * 宗门职位组件 - 存储弟子在宗门中的职位信息
 */
data class SectPositionInfo(
    val position: SectPositionType = SectPositionType.DISCIPLE_OUTER,
    val department: String? = null
)

/**
 * 宗门职位类型
 */
enum class SectPositionType(val sortOrder: Int) {
    LEADER(0),         // 宗主
    ELDER(1),          // 长老
    DISCIPLE_INNER(2), // 内门弟子
    DISCIPLE_OUTER(3)  // 外门弟子
}
