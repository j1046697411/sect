package cn.jzl.sect.core.disciple

/**
 * 忠诚度组件 - 表示弟子对宗门的忠诚程度
 */
data class LoyaltyComponent(
    val value: Int = 100,           // 忠诚度值 (0-100)
    val consecutiveUnpaidMonths: Int = 0  // 连续未发放俸禄月数
) {
    /**
     * 获取忠诚度等级
     */
    fun getLevel(): LoyaltyLevel {
        return when {
            value >= 80 -> LoyaltyLevel.DEVOTED      // 忠心耿耿
            value >= 60 -> LoyaltyLevel.LOYAL        // 忠诚
            value >= 40 -> LoyaltyLevel.NEUTRAL      // 中立
            value >= 20 -> LoyaltyLevel.DISCONTENT   // 不满
            else -> LoyaltyLevel.REBELLIOUS          // 叛逆
        }
    }

    /**
     * 是否可能叛逃
     */
    fun mayDefect(): Boolean {
        return value <= 10 || consecutiveUnpaidMonths >= 6
    }
}

/**
 * 忠诚度等级
 */
enum class LoyaltyLevel {
    DEVOTED,      // 忠心耿耿 (80-100)
    LOYAL,        // 忠诚 (60-79)
    NEUTRAL,      // 中立 (40-59)
    DISCONTENT,   // 不满 (20-39)
    REBELLIOUS    // 叛逆 (0-19)
}

/**
 * 忠诚度等级显示名称
 */
val LoyaltyLevel.displayName: String
    get() = when (this) {
        LoyaltyLevel.DEVOTED -> "忠心耿耿"
        LoyaltyLevel.LOYAL -> "忠诚"
        LoyaltyLevel.NEUTRAL -> "中立"
        LoyaltyLevel.DISCONTENT -> "不满"
        LoyaltyLevel.REBELLIOUS -> "叛逆"
    }
