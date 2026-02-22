package cn.jzl.sect.viewmodel

import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.core.relation.RelationshipType
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.facility.systems.FacilityValueCalculator
import cn.jzl.sect.skill.components.SkillRarity
import cn.jzl.sect.skill.components.SkillType

/**
 * 功法UI模型
 */
data class SkillUiModel(
    val id: Long,
    val name: String,
    val description: String,
    val type: SkillType,
    val typeDisplay: String,
    val rarity: SkillRarity,
    val rarityDisplay: String,
    val requiredRealm: Realm,
    val requiredRealmDisplay: String,
    val requiredComprehension: Int,
    val hasPrerequisites: Boolean,
    val prerequisiteSkillIds: List<Long>,
    val displayName: String
)

/**
 * 已学习功法UI模型
 */
data class LearnedSkillUiModel(
    val skillId: Long,
    val skillName: String,
    val proficiency: Int,
    val maxProficiency: Int = 100,
    val learnedTime: Long,
    val canInherit: Boolean
)

/**
 * 战斗属性UI模型
 */
data class CombatStatsUiModel(
    val attack: Int,
    val defense: Int,
    val speed: Int,
    val critRate: Int,
    val dodgeRate: Int
) {
    /**
     * 计算有效攻击力
     */
    fun calculateEffectiveAttack(): Int = attack

    /**
     * 计算伤害减免百分比
     */
    fun calculateDamageReduction(): Double {
        return defense.toDouble() / (defense + 100)
    }
}

/**
 * 战斗力对比结果UI模型
 */
data class CombatComparisonUiModel(
    val discipleId: Long,
    val discipleName: String,
    val combatPower: Int,
    val combatLevel: String,
    val stats: CombatStatsUiModel,
    val isHighest: Boolean = false,
    val isLowest: Boolean = false
)

/**
 * 关系UI模型
 */
data class RelationshipUiModel(
    val targetId: Long,
    val targetName: String,
    val type: RelationshipType,
    val typeDisplay: String,
    val level: Int,
    val effectBonus: Int,
    val isMutual: Boolean
)

/**
 * 资质UI模型
 */
data class TalentUiModel(
    // 资质属性
    val physique: Int,
    val comprehension: Int,
    val fortune: Int,
    val mental: Int,
    // 战斗属性
    val strength: Int,
    val agility: Int,
    val intelligence: Int,
    val endurance: Int,
    // 生活属性
    val charm: Int,
    val alchemyTalent: Int,
    val forgingTalent: Int
)

/**
 * 设施价值报告UI模型
 */
data class FacilityValueReportUiModel(
    val facilityName: String,
    val valueScore: Int,
    val valueLevel: String,
    val valueLevelDisplay: String,
    val roi: Double,
    val roiDisplay: String,
    val paybackPeriod: Int,
    val paybackPeriodDisplay: String,
    val recommendation: String,
    val strategicValue: Int
)

/**
 * 设施使用结果UI模型
 */
data class FacilityUsageResultUiModel(
    val success: Boolean,
    val message: String,
    val consumedResources: Map<String, Int>,
    val gainedBenefits: Map<String, Int>
)

/**
 * 功法学习条件UI模型
 */
data class SkillLearningConditionUiModel(
    val skillId: Long,
    val skillName: String,
    val canLearn: Boolean,
    val realmMet: Boolean,
    val comprehensionMet: Boolean,
    val prerequisitesMet: Boolean,
    val missingPrerequisites: List<String>,
    val successRate: Double,
    val learningTime: Int
)

/**
 * 功法传承条件UI模型
 */
data class SkillInheritanceConditionUiModel(
    val skillId: Long,
    val skillName: String,
    val canInherit: Boolean,
    val proficiencyMet: Boolean,
    val realmGapMet: Boolean,
    val rarityMet: Boolean,
    val requiredProficiency: Int,
    val masterRealm: String,
    val apprenticeRealm: String
)

/**
 * 扩展函数：获取关系类型显示名称
 */
fun RelationshipType.getDisplayName(): String {
    return when (this) {
        RelationshipType.MASTER_APPRENTICE -> "师徒"
        RelationshipType.FELLOW_DISCIPLE -> "同门"
        RelationshipType.COMPETITOR -> "竞争"
        RelationshipType.PARTNER -> "合作"
        RelationshipType.FRIENDLY -> "友好"
        RelationshipType.HOSTILE -> "敌对"
    }
}

/**
 * 扩展函数：获取功法类型显示名称
 */
fun SkillType.getDisplayName(): String {
    return when (this) {
        SkillType.CULTIVATION -> "修炼"
        SkillType.COMBAT -> "战斗"
        SkillType.MOVEMENT -> "身法"
        SkillType.ALCHEMY -> "炼丹"
        SkillType.FORGING -> "炼器"
        SkillType.FORMATION -> "阵法"
        SkillType.SPIRITUAL -> "神识"
        SkillType.SUPPORT -> "辅助"
    }
}

/**
 * 扩展函数：获取功法品级显示名称
 */
fun SkillRarity.getDisplayName(): String {
    return when (this) {
        SkillRarity.COMMON -> "凡品"
        SkillRarity.UNCOMMON -> "下品"
        SkillRarity.RARE -> "中品"
        SkillRarity.EPIC -> "上品"
        SkillRarity.LEGENDARY -> "极品"
        SkillRarity.MYTHIC -> "仙品"
        SkillRarity.DIVINE -> "神品"
    }
}

/**
 * 扩展函数：获取境界显示名称
 */
fun Realm.getDisplayName(): String {
    return this.displayName
}

/**
 * 扩展函数：获取设施价值等级显示名称
 */
fun FacilityValueCalculator.FacilityValueLevel.getDisplayName(): String {
    return when (this) {
        FacilityValueCalculator.FacilityValueLevel.BASIC -> "基础"
        FacilityValueCalculator.FacilityValueLevel.STANDARD -> "标准"
        FacilityValueCalculator.FacilityValueLevel.ADVANCED -> "高级"
        FacilityValueCalculator.FacilityValueLevel.PREMIUM -> "顶级"
        FacilityValueCalculator.FacilityValueLevel.LEGENDARY -> "传奇"
    }
}
