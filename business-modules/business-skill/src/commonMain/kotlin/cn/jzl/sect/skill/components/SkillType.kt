package cn.jzl.sect.skill.components

/**
 * 功法类型枚举
 * 定义功法的不同类别
 *
 * @property displayName 类型的显示名称
 */
enum class SkillType(val displayName: String) {
    CULTIVATION("修炼"),    // 修炼功法 - 提升修炼速度、灵气容量
    COMBAT("战斗"),         // 战斗功法 - 提升战斗属性、战斗技能
    MOVEMENT("身法"),       // 身法 - 提升速度、闪避
    ALCHEMY("炼丹"),        // 炼丹功法 - 提升炼丹成功率、品质
    FORGING("炼器"),        // 炼器功法 - 提升炼器成功率、品质
    FORMATION("阵法"),      // 阵法 - 布置阵法、阵法威力
    SPIRITUAL("神识"),      // 神识功法 - 提升神识范围、强度
    SUPPORT("辅助")         // 辅助功法 - 治疗、增益等
}
