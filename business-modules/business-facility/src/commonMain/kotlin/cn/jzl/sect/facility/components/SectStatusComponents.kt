package cn.jzl.sect.facility.components

/**
 * 宗门状态枚举
 */
enum class SectStatus(val displayName: String, val description: String) {
    NORMAL("正常", "宗门运转良好"),
    WARNING("警告", "宗门面临一些困难，需要注意"),
    CRITICAL("危急", "宗门处于危险状态，需要立即采取措施"),
    DISSOLVED("已解散", "宗门已经解散"),
    NO_SECT("无宗门", "没有找到宗门实体");

    fun isOperational(): Boolean = this == NORMAL || this == WARNING
}

/**
 * 财务摘要
 */
data class FinancialSummary(
    val spiritStones: Long,
    val contributionPoints: Long,
    val monthlyCost: Long,
    val canSurviveMonths: Long,
    val totalDisciples: Int,
    val rebelliousDisciples: Int
) {
    companion object {
        val EMPTY = FinancialSummary(0, 0, 0, 0, 0, 0)
    }

    fun toDisplayString(): String {
        val survivalText = if (canSurviveMonths == Long.MAX_VALUE) {
            "无限期"
        } else {
            "${canSurviveMonths}个月"
        }

        return """
            宗门财务摘要:
            灵石储备: $spiritStones
            贡献点: $contributionPoints
            月度支出: $monthlyCost
            可维持: $survivalText
            弟子总数: $totalDisciples
            叛逆风险: $rebelliousDisciples
        """.trimIndent()
    }
}
