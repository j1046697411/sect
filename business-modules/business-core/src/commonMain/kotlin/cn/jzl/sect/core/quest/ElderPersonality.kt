package cn.jzl.sect.core.quest

import cn.jzl.sect.core.ai.Personality8

/**
 * 长老性格类型枚举
 */
enum class ElderPersonalityType {
    IMPARTIAL,  // 公正：各维度权重均衡
    BIASED,     // 偏私：偏爱特定属性（根骨高+10%、勤勉高+10%）
    STRICT,     // 严苛：评分标准×1.2
    LENIENT     // 宽松：评分标准×0.8
}

/**
 * 长老偏好属性枚举
 */
enum class ElderPreference {
    NONE,           // 无偏好
    HIGH_PHYSIQUE,  // 偏爱高根骨
    HIGH_DILIGENCE  // 偏爱高勤勉
}

/**
 * 长老性格数据类 - 存储长老的性格特征
 */
data class ElderPersonality(
    val type: ElderPersonalityType,      // 性格类型
    val preference: ElderPreference,     // 偏好属性
    val baseWeights: EvaluationWeights   // 基础评估权重
) {
    companion object {
        /**
         * 默认评估权重
         * 完成度40%、效率25%、质量20%、存活率15%
         */
        val DEFAULT_WEIGHTS = EvaluationWeights(
            completionRate = 0.40f,
            efficiency = 0.25f,
            quality = 0.20f,
            survivalRate = 0.15f
        )

        /**
         * 创建公正型长老性格
         */
        fun impartial(): ElderPersonality = ElderPersonality(
            type = ElderPersonalityType.IMPARTIAL,
            preference = ElderPreference.NONE,
            baseWeights = DEFAULT_WEIGHTS
        )

        /**
         * 创建偏私型长老性格
         * @param preference 偏好属性
         */
        fun biased(preference: ElderPreference): ElderPersonality = ElderPersonality(
            type = ElderPersonalityType.BIASED,
            preference = preference,
            baseWeights = DEFAULT_WEIGHTS
        )

        /**
         * 创建严苛型长老性格
         */
        fun strict(): ElderPersonality = ElderPersonality(
            type = ElderPersonalityType.STRICT,
            preference = ElderPreference.NONE,
            baseWeights = DEFAULT_WEIGHTS
        )

        /**
         * 创建宽松型长老性格
         */
        fun lenient(): ElderPersonality = ElderPersonality(
            type = ElderPersonalityType.LENIENT,
            preference = ElderPreference.NONE,
            baseWeights = DEFAULT_WEIGHTS
        )
    }
}

/**
 * 评估权重数据类
 */
data class EvaluationWeights(
    val completionRate: Float,  // 完成度权重
    val efficiency: Float,      // 效率权重
    val quality: Float,         // 质量权重
    val survivalRate: Float     // 存活率权重
) {
    companion object {
        /**
         * 验证权重之和是否等于1.0
         */
        fun isValid(weights: EvaluationWeights, epsilon: Float = 0.001f): Boolean {
            val sum = weights.completionRate + weights.efficiency + weights.quality + weights.survivalRate
            return kotlin.math.abs(sum - 1.0f) < epsilon
        }
    }

    /**
     * 应用严苛系数（×1.2）
     * 注意：返回的权重之和不为1.0，用于评分标准的调整
     */
    fun applyStrictFactor(): EvaluationWeights = EvaluationWeights(
        completionRate = completionRate * 1.2f,
        efficiency = efficiency * 1.2f,
        quality = quality * 1.2f,
        survivalRate = survivalRate * 1.2f
    )

    /**
     * 应用宽松系数（×0.8）
     * 注意：返回的权重之和不为1.0，用于评分标准的调整
     */
    fun applyLenientFactor(): EvaluationWeights = EvaluationWeights(
        completionRate = completionRate * 0.8f,
        efficiency = efficiency * 0.8f,
        quality = quality * 0.8f,
        survivalRate = survivalRate * 0.8f
    )
}

/**
 * 长老性格类型显示名称
 */
val ElderPersonalityType.displayName: String
    get() = when (this) {
        ElderPersonalityType.IMPARTIAL -> "公正"
        ElderPersonalityType.BIASED -> "偏私"
        ElderPersonalityType.STRICT -> "严苛"
        ElderPersonalityType.LENIENT -> "宽松"
    }

/**
 * 长老偏好显示名称
 */
val ElderPreference.displayName: String
    get() = when (this) {
        ElderPreference.NONE -> "无偏好"
        ElderPreference.HIGH_PHYSIQUE -> "偏爱高根骨"
        ElderPreference.HIGH_DILIGENCE -> "偏爱高勤勉"
    }
