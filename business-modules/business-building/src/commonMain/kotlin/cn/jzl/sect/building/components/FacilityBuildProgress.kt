/**
 * 建筑系统组件定义
 *
 * 包含设施建造相关的组件定义
 */
package cn.jzl.sect.building.components

/**
 * 设施建造进度组件
 *
 * 用于追踪设施建造的进度和状态
 *
 * @property totalTicks 建造所需总时间（刻）
 * @property currentTicks 当前已花费时间（刻）
 * @property isComplete 是否建造完成
 * @property builderId 建造者实体ID（可选）
 */
data class FacilityBuildProgress(
    val totalTicks: Int,
    val currentTicks: Int = 0,
    val isComplete: Boolean = false,
    val builderId: Int? = null
) {
    init {
        require(totalTicks > 0) { "建造总时间必须大于0" }
        require(currentTicks >= 0) { "当前时间不能为负数" }
        require(currentTicks <= totalTicks) { "当前时间不能超过总时间" }
    }

    /**
     * 获取建造进度百分比（0-100）
     */
    fun getProgressPercentage(): Int = (currentTicks * 100 / totalTicks).coerceIn(0, 100)

    /**
     * 更新建造进度
     * @param ticks 经过的时间刻数
     * @return 更新后的建造进度组件
     */
    fun updateProgress(ticks: Int): FacilityBuildProgress {
        val newTicks = (currentTicks + ticks).coerceAtMost(totalTicks)
        return copy(
            currentTicks = newTicks,
            isComplete = newTicks >= totalTicks
        )
    }

    /**
     * 标记建造完成
     */
    fun complete(): FacilityBuildProgress = copy(
        currentTicks = totalTicks,
        isComplete = true
    )
}
