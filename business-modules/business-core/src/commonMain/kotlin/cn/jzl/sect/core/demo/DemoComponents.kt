package cn.jzl.sect.core.demo

/**
 * Demo 组件定义 - 展示 ECS 基础用法
 */

/**
 * 位置组件 - 存储实体的二维坐标
 */
data class PositionComponent(
    val x: Float,
    val y: Float
)

/**
 * 速度组件 - 存储实体的移动速度
 */
data class VelocityComponent(
    val vx: Float,
    val vy: Float
)

/**
 * 名称组件 - 存储实体的显示名称
 */
data class NameComponent(
    val name: String
)

/**
 * 活跃标签 - 标记实体是否处于活跃状态
 */
sealed class ActiveTag
