package cn.jzl.sect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.jzl.ecs.World
import cn.jzl.sect.cultivation.components.Talent
import cn.jzl.sect.engine.WorldProvider
import cn.jzl.sect.skill.components.Skill
import cn.jzl.sect.skill.components.SkillLearned
import cn.jzl.sect.skill.components.SkillRarity
import cn.jzl.sect.skill.services.SkillInheritanceService
import cn.jzl.sect.skill.services.SkillLearningService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock

/**
 * 功法视图模型
 * 管理功法相关的UI状态
 */
class SkillViewModel : ViewModel() {

    private val world: World = WorldProvider.world
    private val learningSystem = SkillLearningService(world)
    private val inheritanceSystem = SkillInheritanceService(world)

    // 功法列表状态
    private val _skillList = MutableStateFlow<SkillListUiState>(SkillListUiState.Loading)
    val skillList: StateFlow<SkillListUiState> = _skillList.asStateFlow()

    // 弟子已学功法列表
    private val _discipleSkills = MutableStateFlow<Map<Long, List<LearnedSkillUiModel>>>(emptyMap())
    val discipleSkills: StateFlow<Map<Long, List<LearnedSkillUiModel>>> = _discipleSkills.asStateFlow()

    // 学习条件状态
    private val _learningCondition = MutableStateFlow<SkillLearningConditionUiState>(SkillLearningConditionUiState.Idle)
    val learningCondition: StateFlow<SkillLearningConditionUiState> = _learningCondition.asStateFlow()

    // 传承条件状态
    private val _inheritanceCondition = MutableStateFlow<SkillInheritanceConditionUiState>(SkillInheritanceConditionUiState.Idle)
    val inheritanceCondition: StateFlow<SkillInheritanceConditionUiState> = _inheritanceCondition.asStateFlow()

    // 操作结果
    private val _operationResult = MutableStateFlow<SkillOperationResult?>(null)
    val operationResult: StateFlow<SkillOperationResult?> = _operationResult.asStateFlow()

    init {
        loadSkills()
    }

    /**
     * 加载功法列表
     */
    fun loadSkills() {
        viewModelScope.launch {
            _skillList.value = SkillListUiState.Loading
            try {
                val skills = queryAllSkills()
                _skillList.value = SkillListUiState.Success(skills)
            } catch (e: Exception) {
                _skillList.value = SkillListUiState.Error(e.message ?: "加载功法失败")
            }
        }
    }

    /**
     * 查询所有可用功法
     */
    private fun queryAllSkills(): List<SkillUiModel> {
        // 这里应该从World中查询所有功法实体
        // 暂时返回模拟数据
        return listOf(
            SkillUiModel(
                id = 1,
                name = "基础心法",
                description = "修炼入门心法，提升修炼效率",
                type = cn.jzl.sect.skill.components.SkillType.CULTIVATION,
                typeDisplay = "修炼",
                rarity = SkillRarity.COMMON,
                rarityDisplay = "凡品",
                requiredRealm = cn.jzl.sect.core.cultivation.Realm.QI_REFINING,
                requiredRealmDisplay = "炼气期",
                requiredComprehension = 30,
                hasPrerequisites = false,
                prerequisiteSkillIds = emptyList(),
                displayName = "【凡品】基础心法"
            ),
            SkillUiModel(
                id = 2,
                name = "青云剑诀",
                description = "青云宗基础剑法，攻守兼备",
                type = cn.jzl.sect.skill.components.SkillType.COMBAT,
                typeDisplay = "战斗",
                rarity = SkillRarity.UNCOMMON,
                rarityDisplay = "下品",
                requiredRealm = cn.jzl.sect.core.cultivation.Realm.QI_REFINING,
                requiredRealmDisplay = "炼气期",
                requiredComprehension = 40,
                hasPrerequisites = true,
                prerequisiteSkillIds = listOf(1),
                displayName = "【下品】青云剑诀"
            ),
            SkillUiModel(
                id = 3,
                name = "炼丹初解",
                description = "基础炼丹术，可炼制低级丹药",
                type = cn.jzl.sect.skill.components.SkillType.ALCHEMY,
                typeDisplay = "炼丹",
                rarity = SkillRarity.RARE,
                rarityDisplay = "中品",
                requiredRealm = cn.jzl.sect.core.cultivation.Realm.FOUNDATION,
                requiredRealmDisplay = "筑基期",
                requiredComprehension = 50,
                hasPrerequisites = false,
                prerequisiteSkillIds = emptyList(),
                displayName = "【中品】炼丹初解"
            ),
            SkillUiModel(
                id = 4,
                name = "御风术",
                description = "提升移动速度和闪避能力",
                type = cn.jzl.sect.skill.components.SkillType.MOVEMENT,
                typeDisplay = "身法",
                rarity = SkillRarity.UNCOMMON,
                rarityDisplay = "下品",
                requiredRealm = cn.jzl.sect.core.cultivation.Realm.QI_REFINING,
                requiredRealmDisplay = "炼气期",
                requiredComprehension = 35,
                hasPrerequisites = false,
                prerequisiteSkillIds = emptyList(),
                displayName = "【下品】御风术"
            )
        )
    }

    /**
     * 加载弟子已学功法
     */
    fun loadDiscipleSkills(discipleId: Long) {
        viewModelScope.launch {
            try {
                val skills = queryDiscipleSkills(discipleId)
                _discipleSkills.value = _discipleSkills.value + (discipleId to skills)
            } catch (e: Exception) {
                // 加载失败不更新状态
            }
        }
    }

    /**
     * 查询弟子已学功法
     */
    private fun queryDiscipleSkills(discipleId: Long): List<LearnedSkillUiModel> {
        // 这里应该从World中查询弟子的SkillLearned组件
        // 暂时返回模拟数据
        return when (discipleId) {
            1L -> listOf(
                LearnedSkillUiModel(
                    skillId = 1,
                    skillName = "基础心法",
                    proficiency = 75,
                    learnedTime = Clock.System.now().toEpochMilliseconds() - 86400000 * 30,
                    canInherit = true
                )
            )
            else -> emptyList()
        }
    }

    /**
     * 检查学习条件
     */
    fun checkLearningCondition(
        skill: SkillUiModel,
        discipleId: Long,
        currentRealm: cn.jzl.sect.core.cultivation.Realm,
        talent: Talent,
        learnedSkillIds: List<Long>
    ) {
        viewModelScope.launch {
            val skillObj = Skill(
                id = skill.id,
                name = skill.name,
                description = skill.description,
                type = skill.type,
                rarity = skill.rarity,
                requiredRealm = skill.requiredRealm,
                requiredComprehension = skill.requiredComprehension,
                prerequisiteSkillIds = skill.prerequisiteSkillIds
            )

            val canLearn = learningSystem.canLearnSkill(skillObj, currentRealm, talent, learnedSkillIds)
            val successRate = learningSystem.calculateLearningSuccessRate(skillObj, talent)
            val learningTime = learningSystem.calculateLearningTime(skillObj, talent)

            // 检查各项条件
            val realmMet = currentRealm.level >= skill.requiredRealm.level
            val comprehensionMet = talent.comprehension >= skill.requiredComprehension
            val prerequisitesMet = skill.prerequisiteSkillIds.all { it in learnedSkillIds }

            val missingPrerequisites = skill.prerequisiteSkillIds
                .filter { it !in learnedSkillIds }
                .map { "前置功法 #$it" }

            _learningCondition.value = SkillLearningConditionUiState.Ready(
                SkillLearningConditionUiModel(
                    skillId = skill.id,
                    skillName = skill.name,
                    canLearn = canLearn,
                    realmMet = realmMet,
                    comprehensionMet = comprehensionMet,
                    prerequisitesMet = prerequisitesMet,
                    missingPrerequisites = missingPrerequisites,
                    successRate = successRate,
                    learningTime = learningTime
                )
            )
        }
    }

    /**
     * 学习功法
     */
    fun learnSkill(skill: SkillUiModel, discipleId: Long) {
        viewModelScope.launch {
            try {
                val skillObj = Skill(
                    id = skill.id,
                    name = skill.name,
                    description = skill.description,
                    type = skill.type,
                    rarity = skill.rarity,
                    requiredRealm = skill.requiredRealm,
                    requiredComprehension = skill.requiredComprehension,
                    prerequisiteSkillIds = skill.prerequisiteSkillIds
                )

                val learnedSkill = learningSystem.learnSkill(skillObj)

                // 更新弟子已学功法列表
                val currentSkills = _discipleSkills.value[discipleId] ?: emptyList()
                val newLearnedSkill = LearnedSkillUiModel(
                    skillId = skill.id,
                    skillName = skill.name,
                    proficiency = learnedSkill.proficiency,
                    learnedTime = learnedSkill.learnedTime,
                    canInherit = false
                )

                _discipleSkills.value = _discipleSkills.value + (discipleId to (currentSkills + newLearnedSkill))
                _operationResult.value = SkillOperationResult(
                    success = true,
                    message = "成功学习功法【${skill.name}】"
                )
            } catch (e: Exception) {
                _operationResult.value = SkillOperationResult(
                    success = false,
                    message = "学习失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 检查传承条件
     */
    fun checkInheritanceCondition(
        skillId: Long,
        skillName: String,
        masterProficiency: Int,
        masterRealm: cn.jzl.sect.core.cultivation.Realm,
        apprenticeRealm: cn.jzl.sect.core.cultivation.Realm,
        skillRarity: SkillRarity
    ) {
        viewModelScope.launch {
            // 使用SkillInheritanceSystem的canInherit方法检查
            val skill = Skill(
                id = skillId,
                name = skillName,
                description = "",
                type = cn.jzl.sect.skill.components.SkillType.CULTIVATION,
                rarity = skillRarity,
                requiredRealm = masterRealm,
                requiredComprehension = 0
            )
            val learned = SkillLearned(
                skillId = skillId,
                proficiency = masterProficiency,
                learnedTime = Clock.System.now().toEpochMilliseconds()
            )
            val canInherit = inheritanceSystem.canInherit(
                skill = skill,
                learned = learned,
                masterRealm = masterRealm,
                apprenticeRealm = apprenticeRealm
            )

            val requiredProficiency = SkillInheritanceService.MIN_PROFICIENCY_FOR_INHERITANCE
            val minRealmGap = SkillInheritanceService.MAX_REALM_GAP

            _inheritanceCondition.value = SkillInheritanceConditionUiState.Ready(
                SkillInheritanceConditionUiModel(
                    skillId = skillId,
                    skillName = skillName,
                    canInherit = canInherit,
                    proficiencyMet = masterProficiency >= requiredProficiency,
                    realmGapMet = (masterRealm.level - apprenticeRealm.level) <= minRealmGap,
                    rarityMet = true, // 品级限制已包含在其他条件中
                    requiredProficiency = requiredProficiency,
                    masterRealm = masterRealm.displayName,
                    apprenticeRealm = apprenticeRealm.displayName
                )
            )
        }
    }

    /**
     * 传承功法
     */
    fun inheritSkill(
        skillId: Long,
        skillName: String,
        masterId: Long,
        apprenticeId: Long
    ) {
        viewModelScope.launch {
            try {
                // 这里应该调用inheritanceSystem.inheritSkill
                // 暂时模拟成功
                val currentSkills = _discipleSkills.value[apprenticeId] ?: emptyList()
                val newLearnedSkill = LearnedSkillUiModel(
                    skillId = skillId,
                    skillName = skillName,
                    proficiency = 0,
                    learnedTime = Clock.System.now().toEpochMilliseconds(),
                    canInherit = false
                )

                _discipleSkills.value = _discipleSkills.value + (apprenticeId to (currentSkills + newLearnedSkill))
                _operationResult.value = SkillOperationResult(
                    success = true,
                    message = "成功将【$skillName】传承给弟子"
                )
            } catch (e: Exception) {
                _operationResult.value = SkillOperationResult(
                    success = false,
                    message = "传承失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 清除学习条件状态
     */
    fun clearLearningCondition() {
        _learningCondition.value = SkillLearningConditionUiState.Idle
    }

    /**
     * 清除传承条件状态
     */
    fun clearInheritanceCondition() {
        _inheritanceCondition.value = SkillInheritanceConditionUiState.Idle
    }

    /**
     * 清除操作结果
     */
    fun clearOperationResult() {
        _operationResult.value = null
    }

    /**
     * 功法列表UI状态
     */
    sealed class SkillListUiState {
        data object Loading : SkillListUiState()
        data class Success(val skills: List<SkillUiModel>) : SkillListUiState()
        data class Error(val message: String) : SkillListUiState()
    }

    /**
     * 学习条件UI状态
     */
    sealed class SkillLearningConditionUiState {
        data object Idle : SkillLearningConditionUiState()
        data class Ready(val condition: SkillLearningConditionUiModel) : SkillLearningConditionUiState()
    }

    /**
     * 传承条件UI状态
     */
    sealed class SkillInheritanceConditionUiState {
        data object Idle : SkillInheritanceConditionUiState()
        data class Ready(val condition: SkillInheritanceConditionUiModel) : SkillInheritanceConditionUiState()
    }

    /**
     * 操作结果
     */
    data class SkillOperationResult(
        val success: Boolean,
        val message: String
    )
}
