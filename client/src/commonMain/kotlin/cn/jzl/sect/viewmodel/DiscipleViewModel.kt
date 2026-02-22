package cn.jzl.sect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.jzl.ecs.World
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.engine.SectWorld
import cn.jzl.sect.engine.service.WorldQueryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 弟子视图模型
 * 管理弟子相关的UI状态
 */
class DiscipleViewModel : ViewModel() {

    // ECS世界实例
    private val world: World = SectWorld.create("青云宗")
    private val queryService = WorldQueryService(world)

    // 弟子列表状态
    private val _discipleList = MutableStateFlow<DiscipleListUiState>(DiscipleListUiState.Loading)
    val discipleList: StateFlow<DiscipleListUiState> = _discipleList.asStateFlow()

    // 当前筛选条件
    private val _currentFilter = MutableStateFlow<DiscipleFilter>(DiscipleFilter.All)
    val currentFilter: StateFlow<DiscipleFilter> = _currentFilter.asStateFlow()

    init {
        loadDisciples()
    }

    /**
     * 加载弟子列表
     */
    fun loadDisciples() {
        viewModelScope.launch {
            _discipleList.value = DiscipleListUiState.Loading
            try {
                val disciples = queryService.queryAllDisciples()
                val uiDisciples = disciples.map { dto ->
                    DiscipleUiModel(
                        id = dto.id,
                        position = dto.position,
                        positionDisplay = getPositionDisplay(dto.position),
                        realm = dto.realm,
                        realmDisplay = getRealmDisplay(dto.realm, dto.layer),
                        layer = dto.layer,
                        age = dto.age,
                        health = dto.health,
                        maxHealth = dto.maxHealth,
                        spirit = dto.spirit,
                        maxSpirit = dto.maxSpirit
                    )
                }
                _discipleList.value = DiscipleListUiState.Success(uiDisciples)
            } catch (e: Exception) {
                _discipleList.value = DiscipleListUiState.Error(e.message ?: "未知错误")
            }
        }
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
     */
    private fun applyFilter() {
        viewModelScope.launch {
            val currentState = _discipleList.value
            if (currentState is DiscipleListUiState.Success) {
                val allDisciples = currentState.data
                val filtered = when (val filter = _currentFilter.value) {
                    is DiscipleFilter.All -> allDisciples
                    is DiscipleFilter.ByPosition -> allDisciples.filter { it.position == filter.position }
                    is DiscipleFilter.ByRealm -> allDisciples.filter { it.realm == filter.realm }
                }
                _discipleList.value = DiscipleListUiState.Success(filtered)
            }
        }
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
        val realmName = when (realm) {
            Realm.MORTAL -> "凡人"
            Realm.QI_REFINING -> "炼气"
            Realm.FOUNDATION -> "筑基"
        }
        return "$realmName${layer}层"
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
}

/**
 * 弟子UI模型
 */
data class DiscipleUiModel(
    val id: Long,
    val position: SectPositionType,
    val positionDisplay: String,
    val realm: Realm,
    val realmDisplay: String,
    val layer: Int,
    val age: Int,
    val health: Int,
    val maxHealth: Int,
    val spirit: Int,
    val maxSpirit: Int
)
