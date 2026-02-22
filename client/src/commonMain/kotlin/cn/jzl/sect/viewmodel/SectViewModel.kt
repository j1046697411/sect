package cn.jzl.sect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.jzl.ecs.World
import cn.jzl.sect.engine.WorldProvider
import cn.jzl.sect.engine.service.WorldQueryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 宗门视图模型
 * 管理宗门相关的UI状态
 */
class SectViewModel : ViewModel() {

    // 通过WorldProvider获取World实例
    private val world: World = WorldProvider.world
    private val queryService = WorldQueryService(world)

    // 宗门信息状态
    private val _sectInfo = MutableStateFlow<SectInfoUiState>(SectInfoUiState.Loading)
    val sectInfo: StateFlow<SectInfoUiState> = _sectInfo.asStateFlow()

    // 弟子统计状态
    private val _discipleStats = MutableStateFlow<DiscipleStatsUiState>(DiscipleStatsUiState.Loading)
    val discipleStats: StateFlow<DiscipleStatsUiState> = _discipleStats.asStateFlow()

    init {
        loadSectInfo()
        loadDiscipleStats()
    }

    /**
     * 加载宗门信息
     */
    fun loadSectInfo() {
        viewModelScope.launch {
            _sectInfo.value = SectInfoUiState.Loading
            try {
                val info = queryService.querySectInfo()
                if (info != null) {
                    _sectInfo.value = SectInfoUiState.Success(
                        SectInfo(
                            name = info.name,
                            spiritStones = info.spiritStones,
                            contributionPoints = info.contributionPoints,
                            currentYear = info.currentYear,
                            currentMonth = info.currentMonth,
                            currentDay = info.currentDay
                        )
                    )
                } else {
                    _sectInfo.value = SectInfoUiState.Error("无法加载宗门信息")
                }
            } catch (e: Exception) {
                _sectInfo.value = SectInfoUiState.Error(e.message ?: "未知错误")
            }
        }
    }

    /**
     * 加载弟子统计
     */
    fun loadDiscipleStats() {
        viewModelScope.launch {
            _discipleStats.value = DiscipleStatsUiState.Loading
            try {
                val stats = queryService.queryDiscipleStatistics()
                _discipleStats.value = DiscipleStatsUiState.Success(
                    DiscipleStats(
                        totalCount = stats.totalCount,
                        innerCount = stats.innerCount,
                        outerCount = stats.outerCount,
                        elderCount = stats.elderCount,
                        qiRefiningCount = stats.qiRefiningCount,
                        foundationCount = stats.foundationCount
                    )
                )
            } catch (e: Exception) {
                _discipleStats.value = DiscipleStatsUiState.Error(e.message ?: "未知错误")
            }
        }
    }

    /**
     * 宗门信息UI状态
     */
    sealed class SectInfoUiState {
        data object Loading : SectInfoUiState()
        data class Success(val data: SectInfo) : SectInfoUiState()
        data class Error(val message: String) : SectInfoUiState()
    }

    /**
     * 弟子统计UI状态
     */
    sealed class DiscipleStatsUiState {
        data object Loading : DiscipleStatsUiState()
        data class Success(val data: DiscipleStats) : DiscipleStatsUiState()
        data class Error(val message: String) : DiscipleStatsUiState()
    }
}

/**
 * 宗门信息数据类
 */
data class SectInfo(
    val name: String,
    val spiritStones: Long,
    val contributionPoints: Long,
    val currentYear: Int,
    val currentMonth: Int,
    val currentDay: Int
)

/**
 * 弟子统计数据类
 */
data class DiscipleStats(
    val totalCount: Int,
    val innerCount: Int,
    val outerCount: Int,
    val elderCount: Int,
    val qiRefiningCount: Int,
    val foundationCount: Int
)
