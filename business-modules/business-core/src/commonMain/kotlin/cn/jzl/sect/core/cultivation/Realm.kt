package cn.jzl.sect.core.cultivation

/**
 * 境界枚举
 * 定义修真世界的8个境界层次
 *
 * @property level 境界等级（0-7）
 * @property displayName 境界显示名称
 */
enum class Realm(
    val level: Int,
    val displayName: String
) {
    MORTAL(0, "凡人"),
    QI_REFINING(1, "炼气期"),
    FOUNDATION(2, "筑基期"),
    GOLDEN_CORE(3, "金丹期"),
    NASCENT_SOUL(4, "元婴期"),
    SOUL_TRANSFORMATION(5, "化神期"),
    TRIBULATION(6, "渡劫期"),
    IMMORTAL(7, "成仙")
}
