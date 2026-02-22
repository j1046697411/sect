package cn.jzl.sect.skill.components

/**
 * 功法效果类型枚举
 * 定义功法可以提供的各种效果类型
 *
 * @property displayName 效果类型的显示名称
 */
enum class SkillEffectType(val displayName: String) {
    ATTRIBUTE_BONUS("属性加成"),      // 增加角色属性(攻击/防御/速度等)
    EFFICIENCY_BONUS("效率加成"),     // 增加各种活动的效率(修炼/炼丹/炼器等)
    PASSIVE_SKILL("被动技能"),        // 被动触发的特殊效果
    ACTIVE_SKILL("主动技能")          // 需要主动使用的技能
}
