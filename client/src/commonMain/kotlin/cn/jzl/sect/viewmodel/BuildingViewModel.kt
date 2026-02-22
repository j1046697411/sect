package cn.jzl.sect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.jzl.ecs.World
import cn.jzl.sect.building.components.*
import cn.jzl.sect.building.systems.BuildingConstructionSystem
import cn.jzl.sect.building.systems.BuildingProductionSystem
import cn.jzl.sect.building.systems.BuildingUpgradeSystem
import cn.jzl.sect.engine.WorldProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 建筑UI数据模型
 */
data class BuildingUiModel(
    val id: Long,
    val name: String,
    val type: BuildingType,
    val level: Int,
    val maxLevel: Int,
    val isActive: Boolean,
    val productionType: ResourceType,
    val productionAmount: Int,
    val maintenanceCost: Int,
    val canUpgrade: Boolean
)

/**
 * 建筑列表状态
 */
sealed class BuildingListUiState {
    data object Loading : BuildingListUiState()
    data class Success(val buildings: List<BuildingUiModel>) : BuildingListUiState()
    data class Error(val message: String) : BuildingListUiState()
}

/**
 * 建筑操作结果
 */
data class BuildingOperationResult(
    val success: Boolean,
    val message: String
)

/**
 * 建筑ViewModel
 */
class BuildingViewModel : ViewModel() {

    private val world: World = WorldProvider.world
        ?: throw IllegalStateException("World not initialized")

    private val constructionSystem = BuildingConstructionSystem(world)
    private val upgradeSystem = BuildingUpgradeSystem(world)
    private val productionSystem = BuildingProductionSystem(world)

    // 建筑列表状态
    private val _buildingList = MutableStateFlow<BuildingListUiState>(BuildingListUiState.Loading)
    val buildingList: StateFlow<BuildingListUiState> = _buildingList.asStateFlow()

    // 操作结果
    private val _operationResult = MutableStateFlow<BuildingOperationResult?>(null)
    val operationResult: StateFlow<BuildingOperationResult?> = _operationResult.asStateFlow()

    // 总产出汇总
    private val _totalProduction = MutableStateFlow<Map<ResourceType, Int>>(emptyMap())
    val totalProduction: StateFlow<Map<ResourceType, Int>> = _totalProduction.asStateFlow()

    // 总维护费用
    private val _totalMaintenanceCost = MutableStateFlow(0)
    val totalMaintenanceCost: StateFlow<Int> = _totalMaintenanceCost.asStateFlow()

    init {
        loadBuildings()
        updateProductionSummary()
    }

    /**
     * 加载建筑列表
     */
    fun loadBuildings() {
        viewModelScope.launch {
            _buildingList.value = BuildingListUiState.Loading
            try {
                val buildings = queryBuildings()
                _buildingList.value = BuildingListUiState.Success(buildings)
            } catch (e: Exception) {
                _buildingList.value = BuildingListUiState.Error("加载失败: ${e.message}")
            }
        }
    }

    /**
     * 查询建筑列表
     */
    private fun queryBuildings(): List<BuildingUiModel> {
        // 这里应该查询World中的建筑实体
        // 暂时返回模拟数据
        return listOf(
            BuildingUiModel(
                id = 1,
                name = "初级修炼室",
                type = BuildingType.CULTIVATION_ROOM,
                level = 1,
                maxLevel = 10,
                isActive = true,
                productionType = ResourceType.CULTIVATION_SPEED,
                productionAmount = 10,
                maintenanceCost = 5,
                canUpgrade = true
            ),
            BuildingUiModel(
                id = 2,
                name = "灵石矿脉",
                type = BuildingType.SPIRIT_STONE_MINE,
                level = 2,
                maxLevel = 10,
                isActive = true,
                productionType = ResourceType.SPIRIT_STONE,
                productionAmount = 22, // 20 * 1.1 (等级加成)
                maintenanceCost = 10,
                canUpgrade = true
            ),
            BuildingUiModel(
                id = 3,
                name = "贡献堂",
                type = BuildingType.CONTRIBUTION_HALL,
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
     * 建造建筑
     */
    fun buildBuilding(name: String, type: BuildingType) {
        viewModelScope.launch {
            val checkResult = constructionSystem.canBuild(type)
            if (!checkResult.canBuild) {
                _operationResult.value = BuildingOperationResult(false, checkResult.reason)
                return@launch
            }

            val result = constructionSystem.build(name, type)
            _operationResult.value = BuildingOperationResult(result.success, result.message)

            if (result.success) {
                loadBuildings()
                updateProductionSummary()
            }
        }
    }

    /**
     * 升级建筑
     */
    fun upgradeBuilding(buildingId: Long) {
        viewModelScope.launch {
            // 这里应该根据buildingId获取建筑实体
            // 暂时模拟升级成功
            _operationResult.value = BuildingOperationResult(true, "升级成功")
            loadBuildings()
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
     * 获取建筑建造成本
     */
    fun getConstructionCost(type: BuildingType): BuildingCost {
        return BuildingCost.getConstructionCost(type)
    }

    /**
     * 获取建筑升级成本
     */
    fun getUpgradeCost(type: BuildingType, currentLevel: Int): BuildingCost {
        return BuildingCost.getUpgradeCost(type, currentLevel)
    }
}
