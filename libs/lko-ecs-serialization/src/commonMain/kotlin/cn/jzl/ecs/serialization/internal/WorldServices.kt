package cn.jzl.ecs.serialization.internal

import cn.jzl.di.DIContext
import cn.jzl.di.instance
import cn.jzl.ecs.World
import cn.jzl.ecs.archetype.ArchetypeService
import cn.jzl.ecs.component.Components
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.EntityService
import cn.jzl.ecs.relation.RelationService

/**
 * World 服务访问辅助类
 *
 * 通过 DI 获取 World 的内部服务，用于序列化模块
 */
class WorldServices(private val world: World) {
    private val directDI = world.di.on(DIContext)
    val components: Components = directDI.instance()
    val entityService: EntityService = directDI.instance()
    val relationService: RelationService = directDI.instance()
    val archetypeService: ArchetypeService = directDI.instance()

    /**
     * 创建实体关系上下文
     */
    fun createRelationContext(): EntityRelationContext {
        return object : EntityRelationContext {
            override val world: World = this@WorldServices.world
        }
    }
}
