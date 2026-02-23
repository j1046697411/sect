package cn.jzl.sect.quest.components

/**
 * 任务类型枚举
 */
enum class QuestType {
    RESOURCE_COLLECTION,    // 资源采集
    FACILITY_CONSTRUCTION,  // 设施建设
    RUIN_EXPLORATION,       // 遗迹探索
    BEAST_HUNT              // 妖兽猎杀
}

/**
 * 任务难度枚举
 */
enum class QuestDifficulty {
    EASY,    // 简单
    NORMAL,  // 普通
    HARD     // 困难
}

/**
 * 任务状态枚举
 */
enum class QuestStatus {
    PENDING_APPROVAL,  // 待审批
    IN_PROGRESS,       // 进行中
    COMPLETED,         // 已完成
    CANCELLED          // 已取消
}

/**
 * 任务组件 - 存储任务的基础信息
 */
data class QuestComponent(
    val questId: Long,              // 任务ID
    val type: QuestType,            // 任务类型
    val difficulty: QuestDifficulty,// 任务难度
    val status: QuestStatus,        // 任务状态
    val createdAt: Long,            // 创建时间戳
    val maxParticipants: Int,       // 名额数量（最大参与人数）
    val description: String = ""    // 任务描述
)

/**
 * 任务类型显示名称
 */
val QuestType.displayName: String
    get() = when (this) {
        QuestType.RESOURCE_COLLECTION -> "资源采集"
        QuestType.FACILITY_CONSTRUCTION -> "设施建设"
        QuestType.RUIN_EXPLORATION -> "遗迹探索"
        QuestType.BEAST_HUNT -> "妖兽猎杀"
    }

/**
 * 任务难度显示名称
 */
val QuestDifficulty.displayName: String
    get() = when (this) {
        QuestDifficulty.EASY -> "简单"
        QuestDifficulty.NORMAL -> "普通"
        QuestDifficulty.HARD -> "困难"
    }

/**
 * 任务状态显示名称
 */
val QuestStatus.displayName: String
    get() = when (this) {
        QuestStatus.PENDING_APPROVAL -> "待审批"
        QuestStatus.IN_PROGRESS -> "进行中"
        QuestStatus.COMPLETED -> "已完成"
        QuestStatus.CANCELLED -> "已取消"
    }
