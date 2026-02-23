package cn.jzl.ecs.query

import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.archetype.Archetype
import cn.jzl.ecs.entity.BatchEntityEditor
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.family.Family
import cn.jzl.ecs.family.FamilyBuilder
import cn.jzl.ecs.family.or
import cn.jzl.ecs.relation.EntityType
import cn.jzl.ecs.relation.Relation

/**
 * 实体查询上下文，定义查询条件和提供组件访问
 *
 * EntityQueryContext 是 ECS 查询系统的核心类，用于：
 * 1. 定义查询条件（通过 [FamilyBuilder] 配置）
 * 2. 提供组件属性委托访问
 * 3. 在查询迭代中访问当前实体
 *
 * ## 使用示例
 * ```kotlin
 * // 基础查询上下文
 * class PositionContext(world: World) : EntityQueryContext(world) {
 *     val position: Position by component()
 * }
 *
 * // 带 Family 过滤的查询上下文
 * class ActivePlayerContext(world: World) : EntityQueryContext(world) {
 *     val health: Health by component()
 *     val position: Position by component()
 *
 *     override fun FamilyBuilder.configure() {
 *         component<ActiveTag>()  // 只查询带 ActiveTag 的实体
 *         component<Health>()
 *     }
 * }
 *
 * // 使用查询
 * world.query { ActivePlayerContext(this) }
 *     .forEach { ctx ->
 *         println("Entity ${ctx.entity.id} at (${ctx.position.x}, ${ctx.position.y})")
 *     }
 * ```
 *
 * ## 可选组件
 * ```kotlin
 * class OptionalContext(world: World) : EntityQueryContext(world) {
 *     val required: RequiredComponent by component()
 *     val optional: OptionalComponent? by component()  // 可空表示可选
 * }
 * ```
 *
 * @param world 查询关联的 ECS 世界
 * @property entity 当前迭代的实体，在 [forEach] 回调中有效
 * @property entityType 当前实体的类型信息
 */
open class EntityQueryContext(override val world: World) : AccessorOperations(), WorldOwner {
    private var archetype: Archetype = world.archetypeService.rootArchetype

    /**
     * 当前迭代的实体
     *
     * 注意：此属性只在查询迭代过程中有效，
     * 在 [forEach] 回调外部访问将返回 [Entity.ENTITY_INVALID]
     */
    val entity: Entity get() = if (entityIndex == -1) Entity.ENTITY_INVALID else archetype.table[entityIndex]

    /**
     * 当前实体的类型信息
     */
    val entityType: EntityType get() = archetype.entityType

    @PublishedApi
    internal var entityIndex: Int = -1

    @PublishedApi
    internal val entityEditor: BatchEntityEditor = BatchEntityEditor(world, Entity.ENTITY_INVALID)

    /**
     * 构建查询的 Family
     *
     * 根据上下文中的访问器和 [configure] 方法构建 Family，
     * 用于预过滤符合条件的实体原型
     *
     * @return 构建的 Family 实例
     */
    internal fun build(): Family = world.familyService.family {
        val families = mutableListOf<FamilyMatching>()
        accessors.asSequence().filterIsInstance<FamilyMatching>().forEach {
            if (!it.isMarkedNullable) {
                it.run { matching() }
                return@forEach
            }
            if (it.optionalGroup == OptionalGroup.One) families.add(it)
        }
        if (families.isNotEmpty()) {
            or { families.forEach { it.run { matching() } } }
        }
        configure()
    }

    /**
     * 配置 Family 构建器
     *
     * 子类可以重写此方法添加额外的 Family 过滤条件
     *
     * @param FamilyBuilder 用于配置查询条件的构建器
     */
    protected open fun FamilyBuilder.configure(): Unit = Unit

    /**
     * 更新缓存以匹配新的原型
     *
     * 在查询遍历不同原型时调用，更新组件访问器的缓存
     *
     * @param archetype 新的原型
     */
    internal fun updateCache(archetype: Archetype) {
        this.archetype = archetype
        accessors.asSequence().filterIsInstance<CachedAccessor>().forEach { it.updateCache(archetype) }
        entityIndex = -1
    }

    /**
     * 在指定实体索引上应用操作
     *
     * 设置当前实体索引，执行操作块，然后自动应用实体编辑器
     *
     * @param entityIndex 实体在原型表中的索引
     * @param block 要执行的操作块
     */
    inline fun apply(entityIndex: Int, block: () -> Unit) {
        this.entityIndex = entityIndex
        try {
            entityEditor.entity = entity
            block()
        } finally {
            entityEditor.apply(world, event = true)
        }
    }

    fun removeRelation(relation: Relation) {
        entityEditor.removeRelation(entity, relation)
    }
}
