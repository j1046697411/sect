package cn.jzl.sect.core.quest

/**
 * 策略组件 - 存储任务选拔和资源分配策略
 */
data class PolicyComponent(
    val selectionCycleYears: Int,       // 选拔周期（年）
    val selectionRatio: Float,          // 选拔比例（百分比，0.0 - 1.0）
    val resourceAllocationRatio: Float  // 资源分配比例（百分比，0.0 - 1.0）
)

/**
 * 验证策略参数是否有效
 */
fun PolicyComponent.isValid(): Boolean {
    return selectionCycleYears > 0 &&
           selectionRatio in 0.0f..1.0f &&
           resourceAllocationRatio in 0.0f..1.0f
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
 */
fun defaultPolicy(): PolicyComponent {
    return PolicyComponent(
        selectionCycleYears = 1,      // 每年选拔一次
        selectionRatio = 0.2f,        // 选拔20%的弟子
        resourceAllocationRatio = 0.3f // 分配30%的资源
    )
}
