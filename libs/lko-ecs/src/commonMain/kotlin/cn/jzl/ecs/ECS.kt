package cn.jzl.ecs

import cn.jzl.di.*
import cn.jzl.ecs.addon.Injector
import cn.jzl.ecs.addon.Phase
import cn.jzl.ecs.addon.WorldSetup
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.archetype.ArchetypeService
import cn.jzl.ecs.component.componentAddon
import cn.jzl.ecs.entity.*
import cn.jzl.ecs.family.FamilyService
import cn.jzl.ecs.observer.observeAddon
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.Query
import cn.jzl.ecs.query.QueryService
import cn.jzl.ecs.relation.RelationProvider
import cn.jzl.ecs.relation.RelationService

/**
 * 检查实体是否处于活动状态
 *
 * @param entity 要检查的实体
 * @return 如果实体存在且有效返回 true
 */
fun World.isActive(entity: Entity): Boolean = entityService.isActive(entity)

/**
 * 创建新实体
 *
 * 在世界中创建一个新实体，并通过 [configuration] 配置其组件和关系。
 *
 * ## 使用示例
 * ```kotlin
 * val player = world.entity {
 *     it.addComponent(Name("Player"))
 *     it.addComponent(Health(100, 100))
 *     it.addComponent(Position(0, 0))
 *     it.addTag<ActiveTag>()
 * }
 * ```
 *
 * @param configuration 实体配置闭包，用于添加组件和关系
 * @return 创建的实体实例
 */
@ECSDsl
fun World.entity(
    configuration: EntityCreateContext.(Entity) -> Unit
): Entity = entityService.create(true, configuration)

/**
 * 使用指定 ID 创建实体
 *
 * 用于从保存状态恢复实体时使用特定 ID。
 *
 * @param entityId 指定的实体 ID
 * @param configuration 实体配置闭包
 * @return 创建的实体实例
 */
@ECSDsl
fun World.entity(
    entityId: Int,
    configuration: EntityCreateContext.(Entity) -> Unit
): Entity = entityService.create(entityId, true, configuration)

/**
 * 编辑现有实体
 *
 * 打开实体编辑器，通过 [configuration] 修改实体的组件和关系。
 *
 * ## 使用示例
 * ```kotlin
 * world.editor(player) {
 *     it.addComponent(health.copy(current = 80))
 *     it.removeTag<ActiveTag>()
 * }
 * ```
 *
 * @param entity 要编辑的实体
 * @param configuration 编辑配置闭包
 */
@ECSDsl
fun World.editor(entity: Entity, configuration: EntityUpdateContext.(Entity) -> Unit) {
    entityService.configure(entity, true, configuration)
}

/**
 * 在当前上下文中编辑实体
 *
 * 扩展函数版本，可以在拥有 [WorldOwner] 上下文的地方使用
 *
 * @param configuration 编辑配置闭包
 */
@ECSDsl
context(worldOwner: WorldOwner)
fun Entity.editor(configuration: EntityUpdateContext.(Entity) -> Unit): Unit = with(worldOwner) {
    world.editor(this@editor, configuration)
}

/**
 * 在当前上下文中创建子实体
 *
 * 创建一个新实体作为当前实体的子实体，建立父子关系
 *
 * @param configuration 实体配置闭包
 * @return 创建的子实体
 */
@ECSDsl
context(worldOwner: WorldOwner)
fun Entity.childOf(configuration: EntityCreateContext.(Entity) -> Unit): Entity = with(worldOwner){
    return world.childOf(this@childOf, configuration)
}

/**
 * 创建子实体
 *
 * 创建一个新实体并建立父子关系
 *
 * @param parent 父实体
 * @param configuration 实体配置闭包
 * @return 创建的子实体
 */
@ECSDsl
fun World.childOf(parent: Entity, configuration: EntityCreateContext.(Entity) -> Unit): Entity = entity {
    configuration(it)
    it.parent(parent)
}

/**
 * 在当前上下文中从预制体实例化
 *
 * 基于预制体实体创建新实例，继承预制体的组件
 *
 * @param configuration 实例配置闭包，用于覆盖或添加组件
 * @return 创建的实例实体
 */
@ECSDsl
context(worldOwner: WorldOwner)
fun Entity.instanceOf(configuration: EntityCreateContext.(Entity) -> Unit): Entity {
    return worldOwner.world.instanceOf(this, configuration)
}

/**
 * 从预制体实例化实体
 *
 * 基于预制体创建新实例
 *
 * @param prefab 预制体实体
 * @param configuration 实例配置闭包
 * @return 创建的实例实体
 */
@ECSDsl
fun World.instanceOf(prefab: Entity, configuration: EntityCreateContext.(Entity) -> Unit): Entity = entity {
    configuration(it)
    it.addRelation(components.instanceOf, prefab)
}

/**
 * 销毁实体
 *
 * 从世界中移除实体及其所有组件
 *
 * @param entity 要销毁的实体
 */
fun World.destroy(entity: Entity) {
    TODO("Not yet implemented")
}

/**
 * 在当前上下文中销毁实体
 */
context(worldOwner: WorldOwner)
fun Entity.destroy(): Unit = with(worldOwner) {
    world.destroy(this@destroy)
}

/**
 * 执行实体查询
 *
 * 创建并执行一个实体查询，通过 [factory] 定义查询上下文
 *
 * ## 使用示例
 * ```kotlin
 * world.query { HealthContext(this) }
 *     .filter { it.health.current > 0 }
 *     .forEach { ctx ->
 *         // 处理每个匹配的实体
 *     }
 * ```
 *
 * @param E 查询上下文类型
 * @param factory 查询上下文工厂函数
 * @return 查询对象，支持链式操作
 */
@ECSDsl
fun <E : EntityQueryContext> World.query(
    factory: World.() -> E
): Query<E> {
    return queryService.query(factory)
}

/**
 * 原型系统插件
 *
 * 提供实体原型管理服务
 */
val archetypeAddon = createAddon<Unit>("archetypeAddon") {
    injects {
        this bind singleton { new(::ArchetypeService) }
    }
}

/**
 * 实体系统插件
 *
 * 提供实体生命周期管理服务
 */
val entityAddon = createAddon<Unit>("entityAddon") {
    injects {
        this bind singleton { new(::EntityStoreImpl) }
        this bind singleton { new(::EntityService) }
    }
}

/**
 * 关系系统插件
 *
 * 提供实体间关系管理服务
 */
val relationAddon = createAddon<Unit>("relationAddon") {
    injects {
        this bind singleton { new(::RelationService) }
        this bind singleton { new(::RelationProvider) }
    }
}

/**
 * 家族系统插件
 *
 * 提供实体分组和过滤服务
 */
val familyAddon = createAddon("familyAddon") {
    injects { this bind singleton { new(::FamilyService) } }
}

/**
 * 核心代码插件
 *
 * 整合所有核心系统的组合插件
 */
val codeAddon = createAddon<Unit>("codeAddon") {
    install(archetypeAddon)
    install(componentAddon)
    install(relationAddon)
    install(familyAddon)
    install(entityAddon)
    install(observeAddon)
    injects {
        this bind singleton { World(di) }
        this bind singleton { new(::PipelineImpl) }
        this bind singleton { new(::QueryService) }
    }
}

/**
 * 创建 ECS 世界
 *
 * 创建并初始化一个新的 ECS 世界实例
 *
 * ## 使用示例
 * ```kotlin
 * val world = world {
 *     install(myAddon)
 * }
 * ```
 *
 * @param configuration 世界配置闭包，用于安装插件
 * @return 创建的 World 实例
 */
@ECSDsl
fun world(configuration: WorldSetup.() -> Unit): World {
    val mainBuilder = DIMainBuilder("world")
    val injector = Injector { mainBuilder.it() }
    val phaseTasks = mutableListOf<Pair<Phase, WorldOwner.() -> Unit>>()
    val worldSetup = WorldSetup(injector) { _, phase, phaseTask ->
        phaseTasks.add(phase to phaseTask)
    }
    worldSetup.configuration()
    worldSetup.install(codeAddon)
    val di = DI(mainBuilder)
    val world by di.instance<World>()
    val pipeline = world.pipeline
    for ((phase, task) in phaseTasks) {
        pipeline.runOnOrAfter(phase, task)
    }
    pipeline.runStartupTasks()
    return world
}
