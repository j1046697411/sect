package cn.jzl.sect.core.sect

data class Position(
    val position: SectPosition = SectPosition.DISCIPLE_OUTER,
    val department: String? = null
)

enum class SectPosition(val sortOrder: Int) {
    LEADER(0),         // 宗主
    ELDER(1),          // 长老
    DISCIPLE_INNER(2), // 内门弟子
    DISCIPLE_OUTER(3)  // 外门弟子
}
