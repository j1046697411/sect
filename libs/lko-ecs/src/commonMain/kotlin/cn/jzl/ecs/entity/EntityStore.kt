package cn.jzl.ecs.entity

/**
 * 实体存储接口，管理实体的创建和销毁
 *
 * EntityStore 负责实体 ID 的分配和回收，维护活跃实体的集合。
 * 它是 ECS 框架中最底层的实体管理服务。
 *
 * ## 主要功能
 * - 分配新的实体 ID
 * - 回收已销毁的实体 ID
 * - 检查实体是否活跃
 *
 * ## 使用示例
 * ```kotlin
 * // 通常通过 EntityService 使用
 * val entity = world.entityStore.create()
 * val entityWithId = world.entityStore.create(100) // 指定 ID
 *
 * // 检查实体是否存在
 * if (entity in world.entityStore) {
 *     // 实体活跃
 * }
 * ```
 *
 * @see EntityService 更高层的实体管理服务
 */
interface EntityStore {
    /**
     * 存储中实体的数量
     */
    val size: Int

    /**
     * 创建新实体
     *
     * 分配一个新的实体 ID，自动选择合适的 ID
     *
     * @return 新创建的实体
     */
    fun create(): Entity

    /**
     * 使用指定 ID 创建实体
     *
     * 用于从保存状态恢复实体时使用特定 ID
     *
     * @param entityId 指定的实体 ID
     * @return 创建的实体
     */
    fun create(entityId: Int): Entity

    /**
     * 检查实体是否存在于存储中
     *
     * @param entity 要检查的实体
     * @return 如果实体活跃返回 true
     */
    operator fun contains(entity: Entity): Boolean

    /**
     * 销毁实体
     *
     * 回收实体 ID，实体将不再活跃
     *
     * @param entity 要销毁的实体
     */
    fun destroy(entity: Entity)
}
