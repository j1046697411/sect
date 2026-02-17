package cn.jzl.sect.core.ai

import kotlin.math.abs
import kotlin.random.Random

/**
 * AI 弟子 8 维度性格模型
 * 每个维度范围理论上为 [-1, 1]
 */
data class Personality8(
    val diligence: Double = 0.0,   // 勤奋 (1) vs 懒惰 (-1)
    val cautious: Double = 0.0,    // 谨慎 (1) vs 冒险 (-1)
    val greed: Double = 0.0,       // 贪婪 (1) vs 慷慨 (-1)
    val loyalty: Double = 0.0,     // 忠诚 (1) vs 背叛 (-1)
    val ambition: Double = 0.0,    // 野心 (1) vs 淡泊 (-1)
    val sociability: Double = 0.0, // 合群 (1) vs 孤僻 (-1)
    val morality: Double = 0.0,    // 道德 (1) vs 邪恶 (-1)
    val patience: Double = 0.0     // 耐心 (1) vs 急躁 (-1)
) {
    /**
     * 计算归一化后的性格权重（用于决策打分）
     * 如果所有维度的绝对值总和 > 0，则每个维度的权重 = 维度值 / 总和
     */
    fun normalized(): Personality8 {
        val totalAbsSum = abs(diligence) + abs(cautious) + abs(greed) + abs(loyalty) +
                abs(ambition) + abs(sociability) + abs(morality) + abs(patience)

        if (totalAbsSum <= 0.0) {
            return this
        }

        return Personality8(
            diligence = diligence / totalAbsSum,
            cautious = cautious / totalAbsSum,
            greed = greed / totalAbsSum,
            loyalty = loyalty / totalAbsSum,
            ambition = ambition / totalAbsSum,
            sociability = sociability / totalAbsSum,
            morality = morality / totalAbsSum,
            patience = patience / totalAbsSum
        )
    }

    companion object {
        /**
         * 守财奴 (Miser): greed = 0.8, morality = -0.4, 其余 0.0
         */
        val Miser = Personality8(greed = 0.8, morality = -0.4)

        /**
         * 苦行僧 (Ascetic): diligence = 0.9, greed = -0.8, sociability = -0.5, 其余 0.0
         */
        val Ascetic = Personality8(diligence = 0.9, greed = -0.8, sociability = -0.5)

        /**
         * 随机生成初始值在 [-0.5, 0.5] 之间的性格
         */
        fun random(): Personality8 {
            return Personality8(
                diligence = Random.nextDouble(-0.5, 0.5),
                cautious = Random.nextDouble(-0.5, 0.5),
                greed = Random.nextDouble(-0.5, 0.5),
                loyalty = Random.nextDouble(-0.5, 0.5),
                ambition = Random.nextDouble(-0.5, 0.5),
                sociability = Random.nextDouble(-0.5, 0.5),
                morality = Random.nextDouble(-0.5, 0.5),
                patience = Random.nextDouble(-0.5, 0.5)
            )
        }
    }
}
