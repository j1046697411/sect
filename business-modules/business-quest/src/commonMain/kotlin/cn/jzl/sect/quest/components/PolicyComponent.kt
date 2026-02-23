package cn.jzl.sect.quest.components

/**
 * 资源分配配置
 * @param cultivation 修炼分配比例（百分比，0-100）
 * @param facility 设施分配比例（百分比，0-100）
 * @param reserve 储备分配比例（百分比，0-100）
 */
data class ResourceAllocation(
    val cultivation: Int,  // 修炼分配比例
    val facility: Int,     // 设施分配比例
    val reserve: Int       // 储备分配比例
) {
    /**
     * 验证资源分配总和是否为100%
     */
    fun isValid(): Boolean {
        return cultivation + facility + reserve == 100 &&
               cultivation >= 0 && facility >= 0 && reserve >= 0
    }
}

/**
 * 策略组件 - 存储宗门政策配置
 * @param selectionCycleYears 选拔周期（年）
 * @param selectionRatio 选拔比例（百分比，0.0 - 1.0）
 * @param resourceAllocationRatio 资源分配比例（百分比，0.0 - 1.0）- 保留字段，兼容旧代码
 * @param resourceAllocation 详细的资源分配配置
 */
data class PolicyComponent(
    val selectionCycleYears: Int,       // 选拔周期（年）
    val selectionRatio: Float,          // 选拔比例（百分比，0.0 - 1.0）
    val resourceAllocationRatio: Float, // 资源分配比例（百分比，0.0 - 1.0）
    val resourceAllocation: ResourceAllocation  // 详细的资源分配配置
)

/**
 * 策略验证结果
 */
sealed class PolicyValidationResult {
    data object Valid : PolicyValidationResult()
    data class Invalid(val reasons: List<String>) : PolicyValidationResult()
}

/**
 * 验证策略参数是否有效
 */
fun PolicyComponent.isValid(): Boolean {
    return validatePolicy(this) is PolicyValidationResult.Valid
}

/**
 * 验证政策配置
 * @param policy 要验证的政策配置
 * @return 验证结果
 */
fun validatePolicy(policy: PolicyComponent): PolicyValidationResult {
    val errors = mutableListOf<String>()

    // 验证选拔周期：3-10年
    if (policy.selectionCycleYears < 3 || policy.selectionCycleYears > 10) {
        errors.add("选拔周期必须在3-10年之间，当前值：${policy.selectionCycleYears}")
    }

    // 验证选拔比例：3%-10%
    if (policy.selectionRatio < 0.03f || policy.selectionRatio > 0.10f) {
        errors.add("选拔比例必须在3%-10%之间，当前值：${(policy.selectionRatio * 100).toInt()}%")
    }

    // 验证资源分配总和必须等于100%
    if (!policy.resourceAllocation.isValid()) {
        val total = policy.resourceAllocation.cultivation +
                    policy.resourceAllocation.facility +
                    policy.resourceAllocation.reserve
        errors.add("资源分配总和必须等于100%，当前总和：$total%")
    }

    return if (errors.isEmpty()) {
        PolicyValidationResult.Valid
    } else {
        PolicyValidationResult.Invalid(errors)
    }
}

/**
 * 计算实际选拔人数
 */
fun PolicyComponent.calculateSelectionCount(totalDisciples: Int): Int {
    return (totalDisciples * selectionRatio).toInt().coerceAtLeast(1)
}

/**
 * 计算资源分配量
 */
fun PolicyComponent.calculateResourceAllocation(totalResources: Long): Long {
    return (totalResources * resourceAllocationRatio).toLong()
}

/**
 * 默认策略配置
 * - 选拔周期：5年
 * - 选拔比例：5%
 * - 资源分配：修炼40%、设施30%、储备30%
 */
fun defaultPolicy(): PolicyComponent {
    return PolicyComponent(
        selectionCycleYears = 5,      // 选拔周期：5年
        selectionRatio = 0.05f,       // 选拔比例：5%
        resourceAllocationRatio = 1.0f, // 资源分配比例：100%
        resourceAllocation = ResourceAllocation(
            cultivation = 40,         // 修炼：40%
            facility = 30,            // 设施：30%
            reserve = 30              // 储备：30%
        )
    )
}
