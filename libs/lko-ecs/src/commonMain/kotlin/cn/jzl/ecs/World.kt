package cn.jzl.ecs

import cn.jzl.di.DI
import cn.jzl.di.DIAware
import cn.jzl.di.instance
import cn.jzl.ecs.archetype.ArchetypeService
import cn.jzl.ecs.component.ComponentProvider
import cn.jzl.ecs.component.ComponentService
import cn.jzl.ecs.component.Components
import cn.jzl.ecs.component.ShadedComponentService
import cn.jzl.ecs.entity.EntityService
import cn.jzl.ecs.entity.EntityStore
import cn.jzl.ecs.family.FamilyService
import cn.jzl.ecs.observer.ObserveService
import cn.jzl.ecs.query.QueryService
import cn.jzl.ecs.relation.RelationProvider
import cn.jzl.ecs.relation.RelationService

/**
 * ECS 世界容器，管理所有实体、组件和系统
 *
 * World 是 ECS 架构的核心，通过依赖注入管理以下服务：
 * - 实体服务（EntityService）：实体生命周期管理
 * - 组件服务（ComponentService）：组件注册和存储
 * - 关系服务（RelationService）：实体间关系管理
 * - 家族服务（FamilyService）：实体分组和过滤
 * - 查询服务（QueryService）：实体查询
 * - 观察服务（ObserveService）：事件监听
 *
 * 使用 [world] 函数创建 World 实例：
 * ```kotlin
 * val world = world {
 *     install(myAddon)
 * }
 * ```
 *
 * @property di 依赖注入容器，提供对所有内部服务的访问
 */
class World(override val di: DI) : DIAware by di {
    @PublishedApi
    internal val componentProvider: ComponentProvider by instance()

    @PublishedApi
    internal val components: Components by instance()

    @PublishedApi
    internal val relations: RelationProvider by instance()

    @PublishedApi
    internal val archetypeService: ArchetypeService by instance()

    @PublishedApi
    internal val entityStore: EntityStore by instance()

    @PublishedApi
    internal val entityService: EntityService by instance()

    @PublishedApi
    internal val relationService: RelationService by instance()

    @PublishedApi
    internal val componentService: ComponentService by instance()

    @PublishedApi
    internal val shadedComponentService: ShadedComponentService by instance()

    @PublishedApi
    internal val familyService: FamilyService by instance()

    @PublishedApi
    internal val pipeline: Pipeline by instance()

    @PublishedApi
    internal val queryService: QueryService by instance()

    @PublishedApi
    internal val observeService: ObserveService by instance()
}
