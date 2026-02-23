package cn.jzl.ecs.component

import androidx.collection.mutableIntObjectMapOf
import androidx.collection.mutableScatterMapOf
import cn.jzl.core.bits.BitSet
import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.id
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.relation.kind
import cn.jzl.ecs.relation.target
import cn.jzl.core.log.ConsoleLogger
import cn.jzl.core.log.LogLevel
import cn.jzl.core.log.Logger
import kotlin.reflect.KClassifier

/**
 * 组件服务，管理组件类型和存储工厂
 *
 * ComponentService 是 ECS 框架中组件管理的核心服务，负责：
 * - 组件类型（ComponentId）的注册和分配
 * - 组件存储工厂的注册和管理
 * - 判断组件是否持有数据
 * - 判断组件是否为单例关系
 * - 判断组件是否为共享组件
 *
 * ## 组件分类
 * - **标签（Tag）**: 不持有数据的组件，仅用于标记
 * - **数据组件**: 持有实际数据的组件
 * - **共享组件**: 在多个实体间共享的组件数据
 * - **单例关系**: 一个实体只能有一个该类型的关系
 *
 * @param world 关联的 ECS 世界
 * @property entityTags 标记为标签的实体位图
 * @property singleRelationBits 标记为单例关系的位图
 */
class ComponentService(override val world: World) : WorldOwner, ComponentProvider, ComponentStoreFactory<Any> {
    private val log: Logger by lazy { ConsoleLogger(LogLevel.DEBUG, "ComponentService") }
    private val componentIdEntities = mutableScatterMapOf<KClassifier, ComponentId>()
    private val componentStoreFactories = mutableIntObjectMapOf<ComponentStoreFactory<*>>()

    @PublishedApi
    internal val entityTags = BitSet()

    @PublishedApi
    internal val singleRelationBits = BitSet()

    /**
     * 检查关系是否持有数据
     *
     * @param relation 关系对象
     * @return 如果关系持有数据返回 true
     */
    fun holdsData(relation: Relation): Boolean = relation.kind.id !in entityTags

    /**
     * 检查关系是否为单例关系
     *
     * @param relation 关系对象
     * @return 如果是单例关系返回 true
     */
    fun isSingleRelation(relation: Relation): Boolean = relation.kind.id in singleRelationBits

    /**
     * 检查关系是否为共享组件
     *
     * @param relation 关系对象
     * @return 如果是共享组件返回 true
     */
    fun isShadedComponent(relation: Relation): Boolean = components.sharedOf == relation.target

    /**
     * 创建组件存储
     *
     * @param relation 关系对象
     * @return 组件存储实例
     */
    override fun create(relation: Relation): ComponentStore<Any> {
        val factory = componentStoreFactories.getOrPut(relation.kind.data) { ComponentStoreFactory.Companion }
        @Suppress("UNCHECKED_CAST")
        return factory.create(relation) as ComponentStore<Any>
    }

    /**
     * 获取或注册组件类型的实体 ID
     *
     * @param classifier 组件类型的 KClassifier
     * @return 组件类型对应的实体 ID
     */
    override fun getOrRegisterEntityForClass(classifier: KClassifier): ComponentId {
        return componentIdEntities.getOrPut(classifier) { world.entityService.create(false) }
    }

    /**
     * 配置组件的存储类型
     *
     * @param C 组件类型
     * @param componentId 组件 ID
     * @param factory 组件存储工厂
     */
    fun <C : Component> configureStoreType(componentId: ComponentId, factory: ComponentStoreFactory<C>) {
        componentStoreFactories[componentId.data] = factory
    }
}
