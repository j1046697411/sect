package cn.jzl.sect.skill.components

/**
 * 功法品级枚举
 * 定义功法的稀有度和品级
 *
 * @property level 品级数值(1-7)
 * @property displayName 品级的显示名称
 */
enum class SkillRarity(val level: Int, val displayName: String) {
    COMMON(1, "凡品"),       // 一阶 - 最常见
    UNCOMMON(2, "灵品"),     // 二阶
    RARE(3, "玄品"),         // 三阶
    EPIC(4, "地品"),         // 四阶
    LEGENDARY(5, "天品"),    // 五阶
    MYTHIC(6, "仙品"),       // 六阶
    DIVINE(7, "神品");       // 七阶 - 最稀有

    /**
     * 获取学习难度
     * 品级越高，学习难度越大
     *
     * @return 学习难度值
     */
    fun getLearningDifficulty(): Int {
        return when (this) {
            COMMON -> 10
            UNCOMMON -> 20
            RARE -> 35
            EPIC -> 55
            LEGENDARY -> 80
            MYTHIC -> 110
            DIVINE -> 150
        }
    }

    /**
     * 获取功法威力倍率
     * 品级越高，威力越大
     *
     * @return 威力倍率
     */
    fun getPowerMultiplier(): Double {
        return when (this) {
            COMMON -> 1.0
            UNCOMMON -> 1.3
            RARE -> 1.7
            EPIC -> 2.2
            LEGENDARY -> 2.8
            MYTHIC -> 3.5
            DIVINE -> 4.5
        }
    }
}
