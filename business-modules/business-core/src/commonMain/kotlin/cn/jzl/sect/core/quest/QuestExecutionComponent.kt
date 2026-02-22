package cn.jzl.sect.core.quest

/**
 * 任务执行组件 - 存储任务的执行信息
 */
data class QuestExecutionComponent(
    val questId: Long,              // 任务ID
    val elderId: Long,              // 长老ID（负责人）
    val innerDiscipleIds: List<Long>, // 内门弟子ID列表
    val outerDiscipleIds: List<Long>, // 外门弟子ID列表
    val progress: Float,            // 执行进度（0.0 - 100.0）
    val startTime: Long,            // 开始时间戳
    val estimatedEndTime: Long      // 预计完成时间戳
)

/**
 * 执行结果数据类 - 存储任务执行结果
 */
data class ExecutionResult(
    val completionRate: Float,      // 完成度（0.0 - 1.0）
    val efficiency: Float,          // 效率（0.0 - 1.0）
    val quality: Float,             // 质量（0.0 - 1.0）
    val survivalRate: Float,        // 存活率（0.0 - 1.0）
    val casualties: Int             // 伤亡人数
)

/**
 * 计算执行结果总分
 */
fun ExecutionResult.calculateTotalScore(): Float {
    return (completionRate * 0.3f + efficiency * 0.25f + quality * 0.25f + survivalRate * 0.2f)
}

/**
 * 获取执行结果评级
 */
fun ExecutionResult.getRating(): String {
    val score = calculateTotalScore()
    return when {
        score >= 0.9f -> "S"
        score >= 0.8f -> "A"
        score >= 0.7f -> "B"
        score >= 0.6f -> "C"
        else -> "D"
    }
}
