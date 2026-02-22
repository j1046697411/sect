package cn.jzl.sect.core.ai

import kotlin.random.Random

/**
 * AI角色6维性格模型
 * 对应MVP需求中的性格属性：野心、勤勉、忠诚、贪婪、和善、冷漠
 * 每个维度范围：[-1, 1]
 *
 * @property ambition 野心 (1) vs 淡泊 (-1) - 影响晋升欲望和风险承担
 * @property diligence 勤勉 (1) vs 懒惰 (-1) - 影响修炼时间和任务完成率
 * @property loyalty 忠诚 (1) vs 背叛 (-1) - 影响服从度
 * @property greed 贪婪 (1) vs 慷慨 (-1) - 影响收益偏好
 * @property kindness 和善 (1) vs 冷漠 (-1) - 影响社交活跃度
 * @property aloofness 冷漠 (1) vs 热情 (-1) - 影响独来独往倾向
 */
data class Personality6(
    val ambition: Double = 0.0,
    val diligence: Double = 0.0,
    val loyalty: Double = 0.0,
    val greed: Double = 0.0,
    val kindness: Double = 0.0,
    val aloofness: Double = 0.0
) {
    init {
        require(ambition in -1.0..1.0) { "野心必须在[-1, 1]范围内" }
        require(diligence in -1.0..1.0) { "勤勉必须在[-1, 1]范围内" }
        require(loyalty in -1.0..1.0) { "忠诚必须在[-1, 1]范围内" }
        require(greed in -1.0..1.0) { "贪婪必须在[-1, 1]范围内" }
        require(kindness in -1.0..1.0) { "和善必须在[-1, 1]范围内" }
        require(aloofness in -1.0..1.0) { "冷漠必须在[-1, 1]范围内" }
    }

    /**
     * 获取主要性格特征描述
     */
    fun getPrimaryTrait(): String {
        val traits = mapOf(
            "野心勃勃" to ambition,
            "勤勉刻苦" to diligence,
            "忠诚可靠" to loyalty,
            "贪婪自私" to greed,
            "和善友善" to kindness,
            "冷漠孤僻" to aloofness
        )
        val maxEntry = traits.maxByOrNull { it.value }
        // 如果最大值小于等于0，说明所有值都很低或相等，返回性格平和
        return if (maxEntry != null && maxEntry.value > 0) maxEntry.key else "性格平和"
    }

    /**
     * 转换为显示字符串
     */
    fun toDisplayString(): String {
        return buildString {
            appendLine("性格特征：${getPrimaryTrait()}")
            appendLine("  野心: ${formatValue(ambition)} (${getTraitDescription(ambition)})")
            appendLine("  勤勉: ${formatValue(diligence)} (${getTraitDescription(diligence)})")
            appendLine("  忠诚: ${formatValue(loyalty)} (${getTraitDescription(loyalty)})")
            appendLine("  贪婪: ${formatValue(greed)} (${getTraitDescription(greed)})")
            appendLine("  和善: ${formatValue(kindness)} (${getTraitDescription(kindness)})")
            appendLine("  冷漠: ${formatValue(aloofness)} (${getTraitDescription(aloofness)})")
        }
    }

    private fun formatValue(value: Double): String {
        return String.format("%+.2f", value)
    }

    private fun getTraitDescription(value: Double): String {
        return when {
            value >= 0.6 -> "极高"
            value >= 0.3 -> "较高"
            value > -0.3 -> "中等"
            value > -0.6 -> "较低"
            else -> "极低"
        }
    }

    companion object {
        /**
         * 随机生成初始值在 [-0.5, 0.5] 之间的性格
         */
        fun random(): Personality6 {
            return Personality6(
                ambition = Random.nextDouble(-0.5, 0.5),
                diligence = Random.nextDouble(-0.5, 0.5),
                loyalty = Random.nextDouble(-0.5, 0.5),
                greed = Random.nextDouble(-0.5, 0.5),
                kindness = Random.nextDouble(-0.5, 0.5),
                aloofness = Random.nextDouble(-0.5, 0.5)
            )
        }

        /**
         * 生成高勤勉性格（适合苦修型弟子）
         */
        fun diligent(): Personality6 {
            return Personality6(
                ambition = Random.nextDouble(0.0, 0.5),
                diligence = Random.nextDouble(0.5, 1.0),
                loyalty = Random.nextDouble(0.0, 0.5),
                greed = Random.nextDouble(-0.5, 0.0),
                kindness = Random.nextDouble(-0.3, 0.3),
                aloofness = Random.nextDouble(0.0, 0.5)
            )
        }

        /**
         * 生成高野心性格（适合进取型弟子）
         */
        fun ambitious(): Personality6 {
            return Personality6(
                ambition = Random.nextDouble(0.5, 1.0),
                diligence = Random.nextDouble(0.0, 0.5),
                loyalty = Random.nextDouble(-0.3, 0.3),
                greed = Random.nextDouble(0.0, 0.5),
                kindness = Random.nextDouble(-0.5, 0.0),
                aloofness = Random.nextDouble(-0.3, 0.3)
            )
        }

        /**
         * 生成高忠诚性格（适合守护型弟子）
         */
        fun loyal(): Personality6 {
            return Personality6(
                ambition = Random.nextDouble(-0.3, 0.3),
                diligence = Random.nextDouble(0.0, 0.5),
                loyalty = Random.nextDouble(0.5, 1.0),
                greed = Random.nextDouble(-0.5, 0.0),
                kindness = Random.nextDouble(0.0, 0.5),
                aloofness = Random.nextDouble(-0.5, 0.0)
            )
        }
    }
}
