package cn.jzl.ecs.component

import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.relation.kind

/**
 * 共享组件服务，管理在多个实体间共享的组件数据
 *
 * ShadedComponentService 提供一种机制，让多个实体共享同一个组件数据实例。
 * 这与普通组件不同，普通组件每个实体都有独立的数据副本。
 *
 * ## 使用场景
 * - 全局配置数据
 * - 共享资源引用
 * - 只读数据缓存
 *
 * ## 工作原理
 * 共享组件的数据存储在 [components] Map 中，以组件类型（relation.kind）为键。
 * 所有使用该共享组件的实体都引用同一个数据实例。
 *
 * ## 使用示例
 * ```kotlin
 * // 添加共享组件
 * entity.addSharedComponent<GlobalConfig>(config)
 *
 * // 获取共享组件
 * val config = entity.getSharedComponent<GlobalConfig>()
 * ```
 *
 * @param world 关联的 ECS 世界
 * @property components 共享组件数据存储，键为组件类型实体，值为组件数据
 */
class ShadedComponentService(override val world: World) : WorldOwner {
    private val components = mutableMapOf<Entity, Any>()

    /**
     * 获取共享组件数据
     *
     * @param relation 关系对象，包含组件类型信息
     * @return 共享组件数据，如果不存在返回 null
     */
    operator fun get(relation: Relation): Any? = components[relation.kind]

    /**
     * 设置共享组件数据
     *
     * @param relation 关系对象，包含组件类型信息
     * @param component 共享组件数据
     */
    operator fun set(relation: Relation, component: Any) {
        components[relation.kind] = component
    }
}
