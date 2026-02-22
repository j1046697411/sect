package cn.jzl.sect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.jzl.ecs.World
import cn.jzl.sect.combat.systems.CombatPowerCalculator
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.cultivation.Talent
import cn.jzl.sect.core.relation.RelationshipType
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.disciples.systems.RelationshipSystem
import cn.jzl.sect.engine.WorldProvider
import cn.jzl.sect.engine.service.WorldQueryService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 弟子视图模型
 * 管理弟子相关的UI状态
 */
class DiscipleViewModel : ViewModel() {

    // 通过WorldProvider获取World实例
    private val world: World = WorldProvider.world
    private val queryService = WorldQueryService(world)
    private val relationshipSystem = RelationshipSystem()
    private val combatPowerCalculator = CombatPowerCalculator()

    // 原始弟子列表（从ECS查询的完整数据）
    private var allDisciples: List<DiscipleUiModel> = emptyList()

    // 弟子列表状态（经过筛选后的数据）
    private val _discipleList = MutableStateFlow<DiscipleListUiState>(DiscipleListUiState.Loading)
    val discipleList: StateFlow<DiscipleListUiState> = _discipleList.asStateFlow()

    // 当前筛选条件
    private val _currentFilter = MutableStateFlow<DiscipleFilter>(DiscipleFilter.All)
    val currentFilter: StateFlow<DiscipleFilter> = _currentFilter.asStateFlow()

    // 选中的弟子
    private val _selectedDisciple = MutableStateFlow<DiscipleUiModel?>(null)
    val selectedDisciple: StateFlow<DiscipleUiModel?> = _selectedDisciple.asStateFlow()

    // 弟子关系映射
    private val _discipleRelationships = MutableStateFlow<Map<Long, List<RelationshipUiModel>>>(emptyMap())
    val discipleRelationships: StateFlow<Map<Long, List<RelationshipUiModel>>> = _discipleRelationships.asStateFlow()

    // 弟子已学功法映射
    private val _discipleLearnedSkills = MutableStateFlow<Map<Long, List<LearnedSkillUiModel>>>(emptyMap())
    val discipleLearnedSkills: StateFlow<Map<Long, List<LearnedSkillUiModel>>> = _discipleLearnedSkills.asStateFlow()

    // 弟子战斗属性映射
    private val _discipleCombatStats = MutableStateFlow<Map<Long, CombatStatsUiModel>>(emptyMap())
    val discipleCombatStats: StateFlow<Map<Long, CombatStatsUiModel>> = _discipleCombatStats.asStateFlow()

    // 弟子资质映射
    private val _discipleTalents = MutableStateFlow<Map<Long, TalentUiModel>>(emptyMap())
    val discipleTalents: StateFlow<Map<Long, TalentUiModel>> = _discipleTalents.asStateFlow()

    // 自动刷新任务
    private var refreshJob: kotlinx.coroutines.Job? = null

    init {
        loadDisciples()
        startAutoRefresh()
    }

    /**
     * 启动自动刷新
     */
    private fun startAutoRefresh() {
        refreshJob = viewModelScope.launch {
            while (isActive) {
                delay(1000) // 每秒刷新一次
                refreshDisciples()
            }
        }
    }

    /**
     * 刷新弟子数据（不显示Loading状态）
     */
    private fun refreshDisciples() {
        viewModelScope.launch {
            try {
                val disciples = queryService.queryAllDisciples()
                allDisciples = disciples.map { dto ->
                    DiscipleUiModel(
                        id = dto.id,
                        name = dto.name,
                        position = dto.position,
                        positionDisplay = getPositionDisplay(dto.position),
                        realm = dto.realm,
                        realmDisplay = getRealmDisplay(dto.realm, dto.layer),
                        layer = dto.layer,
                        age = dto.age,
                        health = dto.health,
                        maxHealth = dto.maxHealth,
                        spirit = dto.spirit,
                        maxSpirit = dto.maxSpirit,
                        cultivation = dto.cultivation,
                        maxCultivation = dto.maxCultivation,
                        currentBehavior = dto.currentBehavior,
                        cultivationProgress = dto.cultivationProgress,
                        // 扩展字段
                        combatStats = getDiscipleCombatStats(dto.id),
                        combatPower = calculateCombatPower(dto.id, dto.realm),
                        combatLevel = getCombatLevel(dto.id),
                        learnedSkills = getDiscipleLearnedSkills(dto.id),
                        relationships = getDiscipleRelationships(dto.id),
                        talent = getDiscipleTalent(dto.id)
                    )
                }
                // 应用当前筛选条件
                applyFilter()
            } catch (e: Exception) {
                // 刷新失败不更新UI，保持旧数据
            }
        }
    }

    /**
     * 加载弟子列表（带Loading状态）
     */
    fun loadDisciples() {
        viewModelScope.launch {
            _discipleList.value = DiscipleListUiState.Loading
            try {
                val disciples = queryService.queryAllDisciples()
                allDisciples = disciples.map { dto ->
                    DiscipleUiModel(
                        id = dto.id,
                        name = dto.name,
                        position = dto.position,
                        positionDisplay = getPositionDisplay(dto.position),
                        realm = dto.realm,
                        realmDisplay = getRealmDisplay(dto.realm, dto.layer),
                        layer = dto.layer,
                        age = dto.age,
                        health = dto.health,
                        maxHealth = dto.maxHealth,
                        spirit = dto.spirit,
                        maxSpirit = dto.maxSpirit,
                        cultivation = dto.cultivation,
                        maxCultivation = dto.maxCultivation,
                        currentBehavior = dto.currentBehavior,
                        cultivationProgress = dto.cultivationProgress,
                        // 扩展字段
                        combatStats = getDiscipleCombatStats(dto.id),
                        combatPower = calculateCombatPower(dto.id, dto.realm),
                        combatLevel = getCombatLevel(dto.id),
                        learnedSkills = getDiscipleLearnedSkills(dto.id),
                        relationships = getDiscipleRelationships(dto.id),
                        talent = getDiscipleTalent(dto.id)
                    )
                }
                // 应用当前筛选条件
                applyFilter()
            } catch (e: Exception) {
                _discipleList.value = DiscipleListUiState.Error(e.message ?: "未知错误")
            }
        }
    }

    /**
     * 获取弟子战斗属性
     */
    private fun getDiscipleCombatStats(discipleId: Long): CombatStatsUiModel {
        return when (discipleId % 4) {
            0L -> CombatStatsUiModel(
                attack = 25,
                defense = 20,
                speed = 15,
                critRate = 8,
                dodgeRate = 5
            )
            1L -> CombatStatsUiModel(
                attack = 30,
                defense = 15,
                speed = 20,
                critRate = 10,
                dodgeRate = 8
            )
            2L -> CombatStatsUiModel(
                attack = 20,
                defense = 30,
                speed = 10,
                critRate = 5,
                dodgeRate = 3
            )
            else -> CombatStatsUiModel(
                attack = 22,
                defense = 22,
                speed = 18,
                critRate = 7,
                dodgeRate = 6
            )
        }
    }

    /**
     * 计算弟子战斗力
     */
    private fun calculateCombatPower(discipleId: Long, realm: Realm): Int {
        val stats = getDiscipleCombatStats(discipleId)
        val combatStats = cn.jzl.sect.combat.components.CombatStats(
            attack = stats.attack,
            defense = stats.defense,
            speed = stats.speed,
            critRate = stats.critRate,
            dodgeRate = stats.dodgeRate
        )
        return combatPowerCalculator.calculateCombatPower(realm, combatStats)
    }

    /**
     * 获取战斗等级
     */
    private fun getCombatLevel(discipleId: Long): String {
        val power = _discipleCombatPower.value[discipleId] ?: 0
        return combatPowerCalculator.assessCombatLevel(power).displayName
    }

    /**
     * 获取弟子已学功法
     */
    private fun getDiscipleLearnedSkills(discipleId: Long): List<LearnedSkillUiModel> {
        // 这里应该从World中查询弟子的SkillLearned组件
        return when (discipleId) {
            1L -> listOf(
                LearnedSkillUiModel(
                    skillId = 1,
                    skillName = "基础心法",
                    proficiency = 75,
                    learnedTime = System.currentTimeMillis() - 86400000 * 30,
                    canInherit = true
                )
            )
            else -> emptyList()
        }
    }

    /**
     * 获取弟子关系列表
     */
    private fun getDiscipleRelationships(discipleId: Long): List<RelationshipUiModel> {
        // 这里应该从relationshipSystem中查询
        return relationshipSystem.getRelationships(discipleId).map { rel ->
            RelationshipUiModel(
                targetId = rel.targetId,
                targetName = "弟子${rel.targetId}", // 这里应该查询实际名称
                type = rel.type,
                typeDisplay = rel.type.getDisplayName(),
                level = rel.level,
                effectBonus = rel.getEffectBonus(),
                isMutual = false
            )
        }
    }

    /**
     * 获取弟子资质
     */
    private fun getDiscipleTalent(discipleId: Long): TalentUiModel {
        // 这里应该从World中查询弟子的Talent组件
        return TalentUiModel(
            physique = 50 + (discipleId % 20).toInt(),
            comprehension = 50 + (discipleId % 30).toInt(),
            fortune = 50 + (discipleId % 25).toInt(),
            mental = 50 + (discipleId % 15).toInt(),
            strength = 20 + (discipleId % 15).toInt(),
            agility = 20 + (discipleId % 20).toInt(),
            intelligence = 20 + (discipleId % 25).toInt(),
            endurance = 20 + (discipleId % 10).toInt(),
            charm = 50 + (discipleId % 20).toInt(),
            alchemyTalent = (discipleId % 10).toInt(),
            forgingTalent = (discipleId % 8).toInt()
        )
    }

    /**
     * 建立师徒关系
     */
    fun establishMasterApprenticeRelationship(masterId: Long, apprenticeId: Long) {
        viewModelScope.launch {
            try {
                relationshipSystem.establishRelationship(
                    sourceId = masterId,
                    targetId = apprenticeId,
                    type = RelationshipType.MASTER_APPRENTICE
                )
                // 刷新关系列表
                refreshRelationships(masterId)
                refreshRelationships(apprenticeId)
            } catch (e: Exception) {
                // 建立失败
            }
        }
    }

    /**
     * 刷新弟子关系
     */
    private fun refreshRelationships(discipleId: Long) {
        val relationships = getDiscipleRelationships(discipleId)
        _discipleRelationships.value = _discipleRelationships.value + (discipleId to relationships)
    }

    /**
     * 按职务筛选
     */
    fun filterByPosition(position: SectPositionType?) {
        _currentFilter.value = if (position == null) {
            DiscipleFilter.All
        } else {
            DiscipleFilter.ByPosition(position)
        }
        applyFilter()
    }

    /**
     * 按境界筛选
     */
    fun filterByRealm(realm: Realm?) {
        _currentFilter.value = if (realm == null) {
            DiscipleFilter.All
        } else {
            DiscipleFilter.ByRealm(realm)
        }
        applyFilter()
    }

    /**
     * 应用筛选
     * 从allDisciples中筛选数据，而不是从当前状态
     */
    private fun applyFilter() {
        val filtered = when (val filter = _currentFilter.value) {
            is DiscipleFilter.All -> allDisciples
            is DiscipleFilter.ByPosition -> allDisciples.filter { it.position == filter.position }
            is DiscipleFilter.ByRealm -> allDisciples.filter { it.realm == filter.realm }
        }
        _discipleList.value = DiscipleListUiState.Success(filtered)
    }

    /**
     * 选中弟子
     */
    fun selectDisciple(disciple: DiscipleUiModel) {
        _selectedDisciple.value = disciple
    }

    /**
     * 清除选中
     */
    fun clearSelection() {
        _selectedDisciple.value = null
    }

    /**
     * 获取职务显示文本
     */
    private fun getPositionDisplay(position: SectPositionType): String {
        return when (position) {
            SectPositionType.LEADER -> "掌门"
            SectPositionType.ELDER -> "长老"
            SectPositionType.DISCIPLE_INNER -> "内门"
            SectPositionType.DISCIPLE_OUTER -> "外门"
        }
    }

    /**
     * 获取境界显示文本
     */
    private fun getRealmDisplay(realm: Realm, layer: Int): String {
        return "${realm.displayName}${layer}层"
    }

    /**
     * 弟子列表UI状态
     */
    sealed class DiscipleListUiState {
        data object Loading : DiscipleListUiState()
        data class Success(val data: List<DiscipleUiModel>) : DiscipleListUiState()
        data class Error(val message: String) : DiscipleListUiState()
    }

    /**
     * 弟子筛选条件
     */
    sealed class DiscipleFilter {
        data object All : DiscipleFilter()
        data class ByPosition(val position: SectPositionType) : DiscipleFilter()
        data class ByRealm(val realm: Realm) : DiscipleFilter()
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}

/**
 * 弟子UI模型
 */
data class DiscipleUiModel(
    val id: Long,
    val name: String,                // 弟子姓名
    val position: SectPositionType,
    val positionDisplay: String,
    val realm: Realm,
    val realmDisplay: String,
    val layer: Int,
    val age: Int,
    val health: Int,
    val maxHealth: Int,
    val spirit: Int,
    val maxSpirit: Int,
    val cultivation: Long,           // 当前修为
    val maxCultivation: Long,        // 最大修为
    val currentBehavior: String,     // 当前行为状态
    val cultivationProgress: Float,  // 修为进度 (0.0 - 1.0)
    // 扩展字段
    val combatStats: CombatStatsUiModel? = null,
    val combatPower: Int = 0,
    val combatLevel: String = "",
    val learnedSkills: List<LearnedSkillUiModel> = emptyList(),
    val relationships: List<RelationshipUiModel> = emptyList(),
    val talent: TalentUiModel? = null
)
