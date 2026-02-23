package cn.jzl.ecs.entity

import cn.jzl.core.bits.fromLowHigh
import cn.jzl.core.bits.high
import cn.jzl.core.bits.low
import cn.jzl.core.list.LongFastList
import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.archetype.Archetype
import cn.jzl.core.log.ConsoleLogger
import cn.jzl.core.log.LogLevel
import cn.jzl.core.log.Logger

/**
 * 实体服务，管理实体的生命周期
 *
 * EntityService 提供实体的创建、配置、销毁等核心操作。
 * 所有实体操作都通过此服务进行，确保实体状态的一致性和正确性。
 *
 * ## 主要功能
 * - 创建新实体
 * - 配置（编辑）现有实体
 * - 检查实体活动状态
 * - 在指定实体上执行操作
 *
 * 通常通过 [World.entity]、[World.editor] 等扩展函数使用，
 * 而非直接使用此类。
 *
 * @param world 关联的 ECS 世界
 */
class EntityService(override val world: World) : WorldOwner {

    private val log: Logger by lazy { ConsoleLogger(LogLevel.DEBUG, "EntityService") }
    private val entityRecords = BucketedLongArray()
    private val entityEditorPool = BatchEntityEditorPool(world)

    /**
     * 检查实体是否处于活动状态
     *
     * @param entity 要检查的实体
     * @return 如果实体存在于世界中且有效返回 true
     */
    fun isActive(entity: Entity): Boolean = entity in world.entityStore

    /**
     * 更新实体记录
     *
     * 内部方法，在实体原型或索引变化时更新记录
     *
     * @param entity 目标实体
     * @param archetype 实体所在原型
     * @param entityIndex 实体在原型表中的索引
     */
    @PublishedApi
    internal fun updateEntityRecord(entity: Entity, archetype: Archetype, entityIndex: Int) {
        entityRecords[entity.id] = Long.fromLowHigh(archetype.id, entityIndex)
    }

    /**
     * 在指定实体上执行操作
     *
     * 提供对实体所在原型和索引的访问，用于底层组件操作
     *
     * @param R 操作返回类型
     * @param entity 目标实体
     * @param block 在原型上下文中执行的操作
     * @return 操作结果
     * @throws IllegalArgumentException 如果实体不存在于世界中
     */
    fun <R> runOn(entity: Entity, block: Archetype.(Int) -> R): R {
        require(entity in world.entityStore) { "Entity $entity is not in world" }
        val record = entityRecords[entity.id]
        val archetype = world.archetypeService[record.low]
        return archetype.block(record.high)
    }

    /**
     * 创建新实体
     *
     * 创建一个新实体并应用配置
     *
     * @param event 是否触发实体创建事件
     * @param configuration 实体配置闭包
     * @return 创建的实体
     */
    fun create(event: Boolean = true, configuration: EntityCreateContext.(Entity) -> Unit = {}): Entity {
        log.debug { "开始创建实体" }
        val entity = postCreate(world.entityStore.create(), event, configuration)
        log.debug { "实体创建完成，ID: ${entity.id}" }
        return entity
    }

    /**
     * 使用指定 ID 创建实体
     *
     * 用于从保存状态恢复实体
     *
     * @param entityId 指定的实体 ID
     * @param event 是否触发实体创建事件
     * @param configuration 实体配置闭包
     * @return 创建的实体
     */
    fun create(entityId: Int, event: Boolean, configuration: EntityCreateContext.(Entity) -> Unit = {}): Entity {
        return postCreate(world.entityStore.create(entityId), event, configuration)
    }

    /**
     * 配置（编辑）实体
     *
     * 打开实体编辑器，应用配置修改
     *
     * @param entity 要编辑的实体
     * @param event 是否触发组件变更事件
     * @param configuration 编辑配置闭包
     */
    fun configure(entity: Entity, event: Boolean, configuration: EntityUpdateContext.(Entity) -> Unit = {}) {
        val editor = entityEditorPool.obtain(entity)
        val context = EntityUpdateContext(editor)
        context.configuration(entity)
        editor.apply(world, event)
        entityEditorPool.release(editor)
    }

    /**
     * 实体创建后处理
     *
     * 将实体插入根原型，应用配置，触发事件
     */
    private fun postCreate(
        entity: Entity,
        event: Boolean,
        configuration: EntityCreateContext.(Entity) -> Unit
    ): Entity {
        val rootArchetype = world.archetypeService.rootArchetype
        val entityIndex = rootArchetype.table.insert(entity) {}
        updateEntityRecord(entity, rootArchetype, entityIndex)
        val editor = entityEditorPool.obtain(entity)
        val context = EntityCreateContext(editor)
        context.configuration(entity)
        editor.apply(world, event)
        entityEditorPool.release(editor)
        return entity
    }
}
