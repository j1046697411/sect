package cn.jzl.ecs.entity

import cn.jzl.core.bits.BitSet
import kotlinx.atomicfu.atomic

/**
 * 实体存储实现类
 *
 * EntityStoreImpl 实现了 [EntityStore] 接口，提供实体的创建、销毁和查询功能。
 * 使用位图跟踪活跃实体，使用栈管理可回收的实体 ID，实现高效的实体生命周期管理。
 *
 * ## 特性
 * - 使用 [BitSet] 跟踪活跃实体，支持快速存在性检查
 * - 使用 [EntityStack] 缓存销毁的实体 ID，实现 ID 复用
 * - 使用原子变量生成新实体 ID，保证线程安全
 * - 支持实体版本管理（通过 Entity 值类）
 *
 * ## 实体 ID 分配策略
 * 1. 优先从回收栈中复用已销毁的实体 ID
 * 2. 如果回收栈为空，则生成新的自增 ID
 *
 * @property activeEntities 活跃实体的位图
 * @property currentId 当前实体 ID 计数器（原子）
 * @property entityStack 实体回收栈
 */
class EntityStoreImpl : EntityStore {
    private val activeEntities = BitSet(1024)
    private val currentId = atomic(0)
    private val entityStack = EntityStack()

    /**
     * 活跃实体的数量
     */
    override val size: Int get() = activeEntities.size

    /**
     * 创建新实体
     *
     * 优先从回收栈获取 ID，如果没有则生成新 ID
     *
     * @return 新创建的实体
     */
    override fun create(): Entity {
        val entity = entityStack.popOrElse { Entity(currentId.getAndIncrement(), 0) }
        activeEntities.set(entity.id)
        return entity
    }

    /**
     * 使用指定 ID 创建实体
     *
     * 用于从保存状态恢复实体时使用特定 ID。
     * 如果指定 ID 已被使用（活跃实体中已存在），则抛出 IllegalArgumentException。
     *
     * @param entityId 指定的实体 ID
     * @return 创建的实体
     * @throws IllegalArgumentException 如果指定 ID 已被使用
     */
    override fun create(entityId: Int): Entity {
        require(entityId !in activeEntities) { "Entity ID $entityId is already in use" }
        val entity = Entity(entityId, 0)
        activeEntities.set(entityId)
        currentId.getAndIncrement()
        return entity
    }

    /**
     * 检查实体是否存在于存储中
     *
     * @param entity 要检查的实体
     * @return 如果实体活跃返回 true
     */
    override fun contains(entity: Entity): Boolean = entity.id in activeEntities

    /**
     * 销毁实体
     *
     * 将实体从活跃集合中移除，并将其 ID 压入回收栈
     *
     * @param entity 要销毁的实体
     */
    override fun destroy(entity: Entity) {
        if (entity !in this) return
        activeEntities.clear(entity.id)
        entityStack.push(entity)
    }
}
