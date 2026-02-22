package cn.jzl.sect.core.combat

/**
 * 战斗属性组件 - 存储实体的战斗相关属性
 */
data class CombatAttribute(
    val strength: Int = 20,     // 力量
    val agility: Int = 20,      // 敏捷
    val intelligence: Int = 20, // 智力
    val endurance: Int = 20     // 耐力
)
