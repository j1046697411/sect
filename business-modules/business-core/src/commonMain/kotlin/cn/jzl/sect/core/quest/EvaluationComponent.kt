package cn.jzl.sect.core.quest

/**
 * 评估维度枚举
 */
enum class EvaluationDimension {
    CULTIVATION,    // 修为
    COMBAT,         // 战斗力
    LOYALTY,        // 忠诚度
    EXPERIENCE,     // 经验
    SPECIALTY       // 专长匹配度
}

/**
 * 候选人评分数据类
 */
data class CandidateScore(
    val discipleId: Long,                       // 弟子ID
    val totalScore: Float,                      // 总分
    val dimensionScores: Map<EvaluationDimension, Float> // 各维度得分
)

/**
 * 评估组件 - 存储任务候选人评估信息
 */
data class EvaluationComponent(
    val questId: Long,                          // 任务ID
    val candidates: List<CandidateScore>        // 候选人列表
)

/**
 * 获取指定维度的得分
 */
fun CandidateScore.getDimensionScore(dimension: EvaluationDimension): Float {
    return dimensionScores[dimension] ?: 0.0f
}

/**
 * 获取最高分的候选人
 */
fun EvaluationComponent.getTopCandidate(): CandidateScore? {
    return candidates.maxByOrNull { it.totalScore }
}

/**
 * 按分数排序获取前N个候选人
 */
fun EvaluationComponent.getTopNCandidates(n: Int): List<CandidateScore> {
    return candidates.sortedByDescending { it.totalScore }.take(n)
}

/**
 * 评估维度显示名称
 */
val EvaluationDimension.displayName: String
    get() = when (this) {
        EvaluationDimension.CULTIVATION -> "修为"
        EvaluationDimension.COMBAT -> "战斗力"
        EvaluationDimension.LOYALTY -> "忠诚度"
        EvaluationDimension.EXPERIENCE -> "经验"
        EvaluationDimension.SPECIALTY -> "专长匹配度"
    }
