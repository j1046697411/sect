package cn.jzl.sect.quest.services

import cn.jzl.ecs.entity.Entity

/**
 * 团队组建结果数据类
 */
data class TeamFormationResult(
    val success: Boolean,
    val elder: Entity?,
    val innerDisciples: List<Entity>,
    val outerDisciples: List<Entity>
) {
    val totalCount: Int
        get() = (if (elder != null) 1 else 0) + innerDisciples.size + outerDisciples.size
}
