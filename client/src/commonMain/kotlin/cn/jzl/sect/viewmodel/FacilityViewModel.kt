package cn.jzl.sect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.jzl.ecs.World
import cn.jzl.sect.building.systems.FacilityConstructionSystem
import cn.jzl.sect.building.systems.FacilityProductionSystem
import cn.jzl.sect.building.systems.FacilityUpgradeSystem
import cn.jzl.sect.core.facility.*
import cn.jzl.sect.engine.WorldProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 设施UI数据模型
 */
data class FacilityUiModel(
    val id: Long,
    val name: String,
    val type: FacilityType,
    val level: Int,
    val maxLevel: Int,
    val isActive: Boolean,
    val productionType: ResourceType,
    val productionAmount: Int,
    val maintenanceCost: Int,
    val canUpgrade: Boolean
)

/**
 * 设施列表状态
 */
sealed class FacilityListUiState {
    data object Loading : FacilityListUiState()
    data class Success(val facilities: List<FacilityUiModel>) : FacilityListUiState()
    data class Error(val message: String) : FacilityListUiState()
}

/**
 * 设施操作结果
 */
data class FacilityOperationResult(
    val success: Boolean,
    val message: String
)

/**
 * 设施ViewModel
 */
class FacilityViewModel : ViewModel() {

    private val world: World = WorldProvider.world
        ?: throw IllegalStateException("World not initialized")

    private val constructionSystem = FacilityConstructionSystem(world)
    private val upgradeSystem = FacilityUpgradeSystem(world)
    private val productionSystem = FacilityProductionSystem(world)

    // 设施列表状态
    private val _facilityList = MutableStateFlow<FacilityListUiState>(FacilityListUiState.Loading)
    val facilityList: StateFlow<FacilityListUiState> = _facilityList.asStateFlow()

    // 操作结果
    private val _operationResult = MutableStateFlow<FacilityOperationResult?>(null)
    val operationResult: StateFlow<FacilityOperationResult?> = _operationResult.asStateFlow()

    // 总产出汇总
    private val _totalProduction = MutableStateFlow<Map<ResourceType, Int>>(emptyMap())
    val totalProduction: StateFlow<Map<ResourceType, Int>> = _totalProduction.asStateFlow()

    // 总维护费用
    private val _totalMaintenanceCost = MutableStateFlow(0)
    val totalMaintenanceCost: StateFlow<Int> = _totalMaintenanceCost.asStateFlow()

    init {
        loadFacilities()
        updateProductionSummary()
    }

    /**
     * 加载设施列表
     */
    fun loadFacilities() {
        viewModelScope.launch {
            _facilityList.value = FacilityListUiState.Loading
            try {
                val facilities = queryFacilities()
                _facilityList.value = FacilityListUiState.Success(facilities)
            } catch (e: Exception) {
                _facilityList.value = FacilityListUiState.Error("加载失败: ${e.message}")
            }
        }
    }

    /**
     * 查询设施列表
     */
    private fun queryFacilities(): List<FacilityUiModel> {
        // 这里应该查询World中的设施实体
        // 暂时返回模拟数据
        return listOf(
            FacilityUiModel(
                id = 1,
                name = "初级修炼室",
                type = FacilityType.CULTIVATION_ROOM,
                level = 1,
                maxLevel = 10,
                isActive = true,
                productionType = ResourceType.CULTIVATION_SPEED,
                productionAmount = 10,
                maintenanceCost = 5,
                canUpgrade = true
            ),
            FacilityUiModel(
                id = 2,
                name = "灵石矿脉",
                type = FacilityType.SPIRIT_STONE_MINE,
                level = 2,
                maxLevel = 10,
                isActive = true,
                productionType = ResourceType.SPIRIT_STONE,
                productionAmount = 22, // 20 * 1.1 (等级加成)
                maintenanceCost = 10,
                canUpgrade = true
            ),
            FacilityUiModel(
                id = 3,
                name = "贡献堂",
                type = FacilityType.CONTRIBUTION_HALL,
                level = 1,
                maxLevel = 10,
                isActive = true,
                productionType = ResourceType.CONTRIBUTION_POINT,
                productionAmount = 10,
                maintenanceCost = 6,
                canUpgrade = true
            )
        )
    }

    /**
     * 建造设施
     */
    fun buildFacility(name: String, type: FacilityType) {
        viewModelScope.launch {
            val checkResult = constructionSystem.canBuild(type)
            if (!checkResult.canBuild) {
                _operationResult.value = FacilityOperationResult(false, checkResult.reason)
                return@launch
            }

            val result = constructionSystem.build(name, type)
            _operationResult.value = FacilityOperationResult(result.success, result.message)

            if (result.success) {
                loadFacilities()
                updateProductionSummary()
            }
        }
    }

    /**
     * 升级设施
     */
    fun upgradeFacility(facilityId: Long) {
        viewModelScope.launch {
            // 这里应该根据facilityId获取设施实体
            // 暂时模拟升级成功
            _operationResult.value = FacilityOperationResult(true, "升级成功")
            loadFacilities()
            updateProductionSummary()
        }
    }

    /**
     * 更新产出汇总
     */
    fun updateProductionSummary() {
        viewModelScope.launch {
            val summary = productionSystem.summarizeProductionByResource()
            _totalProduction.value = summary

            val maintenanceCost = productionSystem.calculateTotalMaintenanceCost()
            _totalMaintenanceCost.value = maintenanceCost
        }
    }

    /**
     * 清除操作结果
     */
    fun clearOperationResult() {
        _operationResult.value = null
    }

    /**
     * 获取设施建造成本
     */
    fun getConstructionCost(type: FacilityType): FacilityCost {
        return FacilityCost.getConstructionCost(type)
    }

    /**
     * 获取设施升级成本
     */
    fun getUpgradeCost(type: FacilityType, currentLevel: Int): FacilityCost {
        return FacilityCost.getUpgradeCost(type, currentLevel)
    }
}
