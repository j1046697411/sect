package cn.jzl.ecs.serialization.entity

import cn.jzl.ecs.component.Component
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityCreateContext
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.entity.hasComponent
import cn.jzl.ecs.relation.kind
import cn.jzl.ecs.serialization.core.SerializationContext
import cn.jzl.ecs.serialization.internal.WorldServices
import kotlin.reflect.KClass

/**
 * 在实体创建上下文中设置持久化组件
 *
 * @param serializationContext 序列化上下文
 * @param component 组件实例
 * @param kClass 组件类型
 */
context(context: EntityCreateContext)
inline fun <reified T : Component> Entity.setPersisting(
    serializationContext: SerializationContext,
    component: T,
    kClass: KClass<out T> = T::class,
): T {
    addComponent(component)
    Persistable().updateHash(component)
    return component
}

/**
 * 在实体创建上下文中批量设置持久化组件
 *
 * @param serializationContext 序列化上下文
 * @param components 组件集合
 * @param override 是否覆盖现有组件
 */
context(context: EntityCreateContext)
fun Entity.setAllPersisting(
    serializationContext: SerializationContext,
    components: Collection<Component>,
    override: Boolean = true,
) {
    val services = WorldServices(serializationContext.world)
    components.forEach { component ->
        val componentId = services.components.id<Component>()
        if (override || !hasComponent(componentId)) {
            setPersisting(serializationContext, component, component::class)
        }
    }
}

/**
 * 在实体创建上下文中获取或设置持久化组件
 *
 * @param serializationContext 序列化上下文
 * @param kClass 组件类型
 * @param default 默认组件工厂
 */
context(context: EntityCreateContext)
inline fun <reified T : Component> Entity.getOrSetPersisting(
    serializationContext: SerializationContext,
    kClass: KClass<out T> = T::class,
    default: () -> T,
): T {
    return get(serializationContext, kClass) ?: default().also { setPersisting(serializationContext, it, kClass) }
}

/**
 * 获取所有持久化组件
 *
 * @param context 序列化上下文
 * @return 持久化组件集合
 */
context(ctx: EntityRelationContext)
fun Entity.getAllPersisting(context: SerializationContext): Set<Component> {
    val persistingComponents = mutableSetOf<Component>()
    val services = WorldServices(context.world)
    val persistableComponentId = services.components.id<Persistable>()

    services.entityService.runOn(this) { entityIndex ->
        archetypeType.forEach { relation ->
            if (relation.kind == persistableComponentId) {
                val component = services.relationService.getRelation(this@getAllPersisting, relation)
                if (component != null && component is Component) {
                    persistingComponents.add(component)
                }
            }
        }
    }

    return persistingComponents
}

/**
 * 获取所有非持久化组件
 *
 * @param context 序列化上下文
 * @return 非持久化组件集合
 */
context(ctx: EntityRelationContext)
fun Entity.getAllNotPersisting(context: SerializationContext): Set<Component> {
    val allComponents = getAll(context)
    val persistingComponents = getAllPersisting(context)
    return allComponents - persistingComponents
}

/**
 * 标记实体为已持久化
 *
 * @param context 序列化上下文
 */
fun Entity.markAsPersisted(context: SerializationContext) {
    val persistingComponents = with(WorldServices(context.world).createRelationContext()) {
        getAllPersisting(context)
    }
    persistingComponents.forEach { component ->
        Persistable().updateHash(component)
    }
}

/**
 * 获取实体的所有组件
 *
 * @param context 序列化上下文
 * @return 所有组件集合
 */
context(ctx: EntityRelationContext)
fun Entity.getAll(context: SerializationContext): Set<Component> {
    val components = mutableSetOf<Component>()
    val services = WorldServices(context.world)

    services.entityService.runOn(this) { entityIndex ->
        archetypeType.forEach { relation ->
            val component = services.relationService.getRelation(this@getAll, relation)
            if (component != null && component is Component) {
                components.add(component)
            }
        }
    }
    return components
}

/**
 * 获取单个组件
 *
 * @param context 序列化上下文
 * @param kClass 组件类型
 * @return 组件实例或 null
 */
context(ctx: EntityRelationContext)
fun <T : Component> Entity.get(context: SerializationContext, kClass: KClass<out T>): T? {
    val services = WorldServices(context.world)
    var result: T? = null

    services.entityService.runOn(this) { entityIndex ->
        archetypeType.forEach { relation ->
            val component = services.relationService.getRelation(this@get, relation)
            if (component != null && kClass.isInstance(component)) {
                result = component as T
                return@forEach
            }
        }
    }
    return result
}

/**
 * 内联版本的获取组件（用于测试）
 */
context(ctx: EntityRelationContext)
inline fun <reified T : Component> Entity.get(context: SerializationContext): T? {
    return get(context, T::class)
}
