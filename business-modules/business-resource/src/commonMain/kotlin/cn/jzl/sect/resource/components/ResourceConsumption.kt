/**
 * 资源消耗组件
 *
 * 用于标记和管理资源消耗相关实体
 */
package cn.jzl.sect.resource.components

/**
 * 资源消耗组件 - 用于标记需要消耗资源的实体
 *
 * @property consumptionType 消耗类型
 * @property baseAmount 基础消耗量
 * @property isActive 是否激活
 */
data class ResourceConsumption(
    val consumptionType: ConsumptionType = ConsumptionType.SALARY,
    val baseAmount: Long = 0L,
    val isActive: Boolean = true
)

/**
 * 消耗类型
 */
enum class ConsumptionType {
    SALARY,      // 俸禄
    MAINTENANCE  // 维护费
}

/**
 * 消耗类型显示名称
 */
val ConsumptionType.displayName: String
    get() = when (this) {
        ConsumptionType.SALARY -> "俸禄"
        ConsumptionType.MAINTENANCE -> "维护费"
    }
