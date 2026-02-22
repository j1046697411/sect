package cn.jzl.sect.skill.components

import cn.jzl.sect.core.cultivation.Realm

/**
 * 功法组件
 * 存储功法的基础信息
 *
 * @property id 功法唯一标识
 * @property name 功法名称
 * @property description 功法描述
 * @property type 功法类型
 * @property rarity 功法品级
 * @property requiredRealm 学习所需境界
 * @property requiredComprehension 学习所需悟性
 * @property prerequisiteSkillIds 前置功法ID列表
 */
data class Skill(
    val id: Long = 0L,
    val name: String = "",
    val description: String = "",
    val type: SkillType = SkillType.CULTIVATION,
    val rarity: SkillRarity = SkillRarity.COMMON,
    val requiredRealm: Realm = Realm.MORTAL,
    val requiredComprehension: Int = 0,
    val prerequisiteSkillIds: List<Long> = emptyList()
) {

    /**
     * 获取学习难度
     *
     * @return 学习难度值
     */
    fun getLearningDifficulty(): Int {
        return rarity.getLearningDifficulty()
    }

    /**
     * 检查是否有前置功法
     *
     * @return 如果有前置功法返回true
     */
    fun hasPrerequisites(): Boolean {
        return prerequisiteSkillIds.isNotEmpty()
    }

    /**
     * 获取功法显示名称
     * 包含品级前缀
     *
     * @return 完整的功法名称
     */
    fun getDisplayName(): String {
        return "【${rarity.displayName}】$name"
    }

    /**
     * 获取功法威力倍率
     *
     * @return 威力倍率
     */
    fun getPowerMultiplier(): Double {
        return rarity.getPowerMultiplier()
    }
}
