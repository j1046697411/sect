package cn.jzl.ecs.serialization.internal

import cn.jzl.di.instance
import cn.jzl.ecs.World
import cn.jzl.ecs.archetype.ArchetypeService
import cn.jzl.ecs.component.Components
import cn.jzl.ecs.entity.EntityService
import cn.jzl.ecs.relation.RelationService

/**
 * World 服务访问辅助类
 *
 * 通过 DI 获取 World 的内部服务，用于序列化模块
 */
class WorldServices(world: World) {
    val components: Components by world.di.instance()
    val entityService: EntityService by world.di.instance()
    val relationService: RelationService by world.di.instance()
    val archetypeService: ArchetypeService by world.di.instance()
}
