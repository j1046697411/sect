package cn.jzl.sect.quest.services

import cn.jzl.ecs.entity.Entity
import cn.jzl.sect.core.ai.Personality6
import cn.jzl.sect.core.sect.SectPositionType

/**
 * 性格类型枚举
 */
enum class PersonalityType {
    DILIGENT,   // 勤勉型
    AMBITIOUS,  // 野心型
    LOYAL,      // 忠诚型
    RANDOM      // 随机型
}

/**
 * 晋升结果
 */
data class PromotionResult(
    val success: Boolean,
    val discipleId: Entity,
    val oldPosition: SectPositionType,
    val newPosition: SectPositionType,
    val generatedPersonality: Personality6?,
    val message: String
) {
    fun toDisplayString(): String {
        return buildString {
            if (success) {
                appendLine("✓ 晋升成功")
                appendLine("  弟子ID: $discipleId")
                appendLine("  $oldPosition → $newPosition")
                generatedPersonality?.let {
                    appendLine("  生成性格: ${it.getPrimaryTrait()}")
                }
            } else {
                appendLine("✗ 晋升失败")
                appendLine("  原因: $message")
            }
        }
    }
}
