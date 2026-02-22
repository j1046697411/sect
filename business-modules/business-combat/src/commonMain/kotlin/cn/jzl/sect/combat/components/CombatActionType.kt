package cn.jzl.sect.combat.components

/**
 * 战斗行动类型枚举
 * 定义战斗中可以采取的行动
 *
 * @property displayName 行动类型的显示名称
 */
enum class CombatActionType(val displayName: String) {
    ATTACK("攻击"),       // 普通攻击
    DEFEND("防御"),       // 防御姿态
    SKILL("技能"),        // 使用技能
    ITEM("道具"),         // 使用道具
    ESCAPE("逃跑")        // 尝试逃跑
}
