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