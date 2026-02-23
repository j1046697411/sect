package cn.jzl.sect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.jzl.ecs.World
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.engine.WorldProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 战斗视图模型
 * 管理战斗相关的UI状态
 */
class CombatViewModel : ViewModel() {

    private val world: World = WorldProvider.world

    // 弟子战斗属性映射
    private val _discipleCombatStats = MutableStateFlow<Map<Long, CombatStatsUiModel>>(emptyMap())
    val discipleCombatStats: StateFlow<Map<Long, CombatStatsUiModel>> = _discipleCombatStats.asStateFlow()

    // 弟子战斗力映射
    private val _discipleCombatPower = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val discipleCombatPower: StateFlow<Map<Long, Int>> = _discipleCombatPower.asStateFlow()

    // 弟子战斗等级映射
    private val _discipleCombatLevel = MutableStateFlow<Map<Long, String>>(emptyMap())
    val discipleCombatLevel: StateFlow<Map<Long, String>> = _discipleCombatLevel.asStateFlow()

    // 战斗力对比结果
    private val _combatComparison = MutableStateFlow<CombatComparisonUiState>(CombatComparisonUiState.Idle)
    val combatComparison: StateFlow<CombatComparisonUiState> = _combatComparison.asStateFlow()

    // 战斗力排行榜
    private val _combatPowerRanking = MutableStateFlow<List<CombatRankingItemUiModel>>(emptyList())
    val combatPowerRanking: StateFlow<List<CombatRankingItemUiModel>> = _combatPowerRanking.asStateFlow()

    /**
     * 加载弟子战斗属性
     */
    fun loadDiscipleCombatStats(discipleId: Long) {
        viewModelScope.launch {
            try {
                val stats = queryDiscipleCombatStats(discipleId)
                _discipleCombatStats.value = _discipleCombatStats.value + (discipleId to stats)
            } catch (e: Exception) {
                // 加载失败不更新状态
            }
        }
    }

    /**
     * 查询弟子战斗属性
     */
    private fun queryDiscipleCombatStats(discipleId: Long): CombatStatsUiModel {
        // 这里应该从World中查询弟子的CombatStats组件
        // 暂时返回基于弟子ID的模拟数据
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
     * 对比多个弟子的战斗力（简化版）
     */
    fun compareCombatPower(disciples: List<DiscipleCombatInfo>) {
        viewModelScope.launch {
            try {
                if (disciples.isEmpty()) {
                    _combatComparison.value = CombatComparisonUiState.Empty
                    return@launch
                }

                // 简化版：直接返回基础数据
                val comparisons = disciples.map { disciple ->
                    CombatComparisonUiModel(
                        discipleId = disciple.id,
                        discipleName = disciple.name,
                        combatPower = disciple.stats.attack * 10, // 简化计算
                        combatLevel = "普通",
                        stats = disciple.stats,
                        isHighest = false,
                        isLowest = false
                    )
                }

                // 标记最高和最低
                val maxPower = comparisons.maxOfOrNull { it.combatPower } ?: 0
                val minPower = comparisons.minOfOrNull { it.combatPower } ?: 0

                val markedComparisons = comparisons.map { comp ->
                    comp.copy(
                        isHighest = comp.combatPower == maxPower,
                        isLowest = comp.combatPower == minPower
                    )
                }

                _combatComparison.value = CombatComparisonUiState.Ready(markedComparisons)
            } catch (e: Exception) {
                _combatComparison.value = CombatComparisonUiState.Error(e.message ?: "对比失败")
            }
        }
    }

    /**
     * 加载战斗力排行榜（简化版）
     */
    fun loadCombatPowerRanking(disciples: List<DiscipleCombatInfo>) {
        viewModelScope.launch {
            try {
                val rankings = disciples.map { disciple ->
                    CombatRankingItemUiModel(
                        rank = 0, // 稍后排序后设置
                        discipleId = disciple.id,
                        discipleName = disciple.name,
                        combatPower = disciple.stats.attack * 10, // 简化计算
                        combatLevel = "普通",
                        realm = disciple.realm.displayName
                    )
                }
                    .sortedByDescending { it.combatPower }
                    .mapIndexed { index, item -> item.copy(rank = index + 1) }

                _combatPowerRanking.value = rankings
            } catch (e: Exception) {
                // 加载失败不更新状态
            }
        }
    }

    /**
     * 清除对比状态
     */
    fun clearComparison() {
        _combatComparison.value = CombatComparisonUiState.Idle
    }

    /**
     * 弟子战斗信息
     */
    data class DiscipleCombatInfo(
        val id: Long,
        val name: String,
        val realm: Realm,
        val stats: CombatStatsUiModel,
        val skillPower: Double = 0.0,
        val equipmentPower: Double = 0.0
    )

    /**
     * 战斗力对比UI状态
     */
    sealed class CombatComparisonUiState {
        data object Idle : CombatComparisonUiState()
        data object Empty : CombatComparisonUiState()
        data class Ready(val comparisons: List<CombatComparisonUiModel>) : CombatComparisonUiState()
        data class Error(val message: String) : CombatComparisonUiState()
    }
}

/**
 * 排行榜项UI模型
 */
data class CombatRankingItemUiModel(
    val rank: Int,
    val discipleId: Long,
    val discipleName: String,
    val combatPower: Int,
    val combatLevel: String,
    val realm: String
)
