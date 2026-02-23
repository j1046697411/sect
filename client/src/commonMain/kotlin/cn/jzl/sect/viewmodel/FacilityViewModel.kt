package cn.jzl.sect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.jzl.ecs.World
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.engine.WorldProvider
import cn.jzl.sect.facility.services.FacilityValueService
import cn.jzl.sect.resource.components.ResourceType
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
    val canUpgrade: Boolean,
    // 扩展字段
    val valueScore: Int = 0,
    val valueLevel: String = "",
    val roi: Double = 0.0,
    val paybackPeriod: Int = 0,
    val recommendation: String = ""
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

    private val valueCalculator = FacilityValueService(world)

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

    // 设施价值报告
    private val _facilityValueReports = MutableStateFlow<Map<Long, FacilityValueReportUiModel>>(emptyMap())
    val facilityValueReports: StateFlow<Map<Long, FacilityValueReportUiModel>> = _facilityValueReports.asStateFlow()

    init {
        loadFacilities()
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
                productionType = ResourceType.SPIRIT_STONE,
                productionAmount = 10,
                maintenanceCost = 5,
                canUpgrade = true,
                valueScore = calculateFacilityValue(100, 5, 1.0, FacilityType.CULTIVATION_ROOM),
                valueLevel = valueCalculator.assessValueLevel(
                    calculateFacilityValue(100, 5, 1.0, FacilityType.CULTIVATION_ROOM)
                ).getDisplayName(),
                roi = calculateROI(100, 10, 5),
                paybackPeriod = calculatePaybackDays(100, 10, 5),
                recommendation = generateRecommendation(calculateROI(100, 10, 5), calculatePaybackDays(100, 10, 5))
            ),
            FacilityUiModel(
                id = 2,
                name = "灵石矿脉",
                type = FacilityType.SPIRIT_STONE_MINE,
                level = 2,
                maxLevel = 10,
                isActive = true,
                productionType = ResourceType.SPIRIT_STONE,
                productionAmount = 22,
                maintenanceCost = 10,
                canUpgrade = true,
                valueScore = calculateFacilityValue(200, 10, 1.1, FacilityType.SPIRIT_STONE_MINE),
                valueLevel = valueCalculator.assessValueLevel(
                    calculateFacilityValue(200, 10, 1.1, FacilityType.SPIRIT_STONE_MINE)
                ).getDisplayName(),
                roi = calculateROI(200, 22, 10),
                paybackPeriod = calculatePaybackDays(200, 22, 10),
                recommendation = generateRecommendation(calculateROI(200, 22, 10), calculatePaybackDays(200, 22, 10))
            ),
            FacilityUiModel(
                id = 3,
                name = "贡献堂",
                type = FacilityType.CONTRIBUTION_HALL,
                level = 1,
                maxLevel = 10,
                isActive = true,
                productionType = ResourceType.HERB,
                productionAmount = 10,
                maintenanceCost = 6,
                canUpgrade = true,
                valueScore = calculateFacilityValue(120, 6, 1.0, FacilityType.CONTRIBUTION_HALL),
                valueLevel = valueCalculator.assessValueLevel(
                    calculateFacilityValue(120, 6, 1.0, FacilityType.CONTRIBUTION_HALL)
                ).getDisplayName(),
                roi = calculateROI(120, 10, 6),
                paybackPeriod = calculatePaybackDays(120, 10, 6),
                recommendation = generateRecommendation(calculateROI(120, 10, 6), calculatePaybackDays(120, 10, 6))
            )
        )
    }

    /**
     * 计算设施价值
     */
    private fun calculateFacilityValue(
        constructionCost: Int,
        maintenanceCost: Int,
        productionEfficiency: Double,
        facilityType: FacilityType
    ): Int {
        return valueCalculator.calculateFacilityValue(
            constructionCost = constructionCost,
            maintenanceCost = maintenanceCost,
            productionEfficiency = productionEfficiency,
            facilityType = facilityType
        )
    }

    /**
     * 计算投资回报率
     */
    private fun calculateROI(constructionCost: Int, dailyRevenue: Int, dailyMaintenance: Int): Double {
        return valueCalculator.calculateROI(constructionCost, dailyRevenue, dailyMaintenance)
    }

    /**
     * 计算回收期（天数）
     */
    private fun calculatePaybackDays(constructionCost: Int, dailyRevenue: Int, dailyMaintenance: Int): Int {
        return valueCalculator.calculatePaybackPeriod(constructionCost, dailyRevenue, dailyMaintenance)
    }

    /**
     * 生成投资建议
     */
    private fun generateRecommendation(roi: Double, paybackPeriod: Int): String {
        return when {
            roi > 0.1 && paybackPeriod < 30 -> "强烈推荐"
            roi > 0.05 && paybackPeriod < 60 -> "推荐"
            roi > 0.02 && paybackPeriod < 100 -> "一般"
            else -> "不推荐"
        }
    }

    /**
     * 生成设施价值报告
     */
    fun generateFacilityValueReport(facility: FacilityUiModel): FacilityValueReportUiModel {
        val valueLevel = valueCalculator.assessValueLevel(facility.valueScore)

        return FacilityValueReportUiModel(
            facilityName = facility.name,
            valueScore = facility.valueScore,
            valueLevel = valueLevel.name,
            valueLevelDisplay = valueLevel.getDisplayName(),
            roi = facility.roi,
            roiDisplay = "${(facility.roi * 100).toInt()}%",
            paybackPeriod = facility.paybackPeriod,
            paybackPeriodDisplay = if (facility.paybackPeriod == Int.MAX_VALUE) "无法回收" else "${facility.paybackPeriod}天",
            recommendation = facility.recommendation,
            strategicValue = valueCalculator.calculateStrategicValue(facility.type)
        )
    }

    /**
     * 清除操作结果
     */
    fun clearOperationResult() {
        _operationResult.value = null
    }
}
