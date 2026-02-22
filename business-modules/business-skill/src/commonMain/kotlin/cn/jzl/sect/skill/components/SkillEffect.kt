package cn.jzl.sect.skill.components

/**
 * 功法效果组件
 * 存储功法的具体效果信息
 *
 * @property type 效果类型
 * @property targetAttribute 目标属性(如"attack", "cultivation"等)
 * @property baseValue 基础效果值
 * @property description 效果描述
 */
data class SkillEffect(
    val type: SkillEffectType = SkillEffectType.ATTRIBUTE_BONUS,
    val targetAttribute: String = "",
    val baseValue: Double = 0.0,
    val description: String = ""
)
