/**
 * 弟子系统 Addon
 *
 * 提供弟子管理功能，包括：
 * - 弟子信息查询和管理
 * - 角色关系管理
 * - 师徒关系管理
 *
 * 使用方式：
 * ```kotlin
 * world.install(disciplesAddon)
 * val discipleInfoService by world.di.instance<DiscipleInfoService>()
 * val relationshipService by world.di.instance<RelationshipService>()
 * val masterApprenticeService by world.di.instance<MasterApprenticeService>()
 *
 * // 获取所有弟子信息
 * val disciples = discipleInfoService.getAllDisciples()
 *
 * // 建立师徒关系
 * masterApprenticeService.apprenticeToMaster(apprenticeId, masterId)
 *
 * // 查询关系
 * val relationships = relationshipService.getRelationships(entityId)
 * ```
 */
package cn.jzl.sect.disciples

import cn.jzl.di.new
import cn.jzl.di.singleton
import cn.jzl.ecs.World
import cn.jzl.ecs.addon.Phase
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.observer.emit
import cn.jzl.ecs.observer.observeWithData
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.cultivation.events.BreakthroughSuccessEvent
import cn.jzl.sect.disciples.components.Relationship
import cn.jzl.sect.disciples.events.DisciplePromotedEvent
import cn.jzl.sect.disciples.services.DiscipleInfoService
import cn.jzl.sect.disciples.services.MasterApprenticeService
import cn.jzl.sect.disciples.services.RelationshipService

/**
 * 弟子系统 Addon
 *
 * 负责注册弟子系统相关组件和服务：
 * - [Relationship] 组件：角色关系数据
 * - [DiscipleInfoService] 服务：弟子信息查询和管理
 * - [RelationshipService] 服务：角色关系管理
 * - [MasterApprenticeService] 服务：师徒关系管理
 *
 * 依赖 [cultivationAddon] 提供修炼系统支持
 *
 * 示例：
 * ```kotlin
 * world.install(disciplesAddon)
 * ```
 */
val disciplesAddon = createAddon("disciplesAddon") {
    // 依赖修炼系统
    install(cn.jzl.sect.cultivation.cultivationAddon)

    // 注册组件
    components {
        world.componentId<Relationship>()
        world.componentId<DisciplePromotedEvent>()
    }

    // 注册服务
    injects {
        this bind singleton { new(::DiscipleInfoService) }
        this bind singleton { new(::RelationshipService) }
        this bind singleton { new(::MasterApprenticeService) }
    }

    // 生命周期回调 - 模块启用时
    on(Phase.ENABLE) {
        // 订阅突破成功事件，处理弟子晋升
        val addon = this
        world.observeWithData<BreakthroughSuccessEvent>().exec {
            handleBreakthroughSuccess(addon.world, this.entity, this.event)
        }
    }
}

private fun handleBreakthroughSuccess(world: World, entity: cn.jzl.ecs.entity.Entity, event: BreakthroughSuccessEvent) {
    // 根据新境界判断是否晋升职位
    val newPosition = when (event.newRealm) {
        Realm.QI_REFINING -> SectPositionType.DISCIPLE_INNER
        Realm.FOUNDATION -> SectPositionType.ELDER
        else -> null
    }

    if (newPosition != null) {
        val query = world.query { PositionQueryContext(world) }
        var currentPosition: SectPositionType? = null
        
        query.forEach { ctx ->
            if (ctx.entity == entity) {
                currentPosition = ctx.position.position
            }
        }

        if (currentPosition != null && currentPosition!! < newPosition) {
            world.editor(entity) {
                it.addComponent(SectPositionInfo(newPosition))
            }

            world.emit(entity, DisciplePromotedEvent(
                entity = entity,
                oldPosition = currentPosition!!,
                newPosition = newPosition
            ))
        }
    }
}

private class PositionQueryContext(world: World) : EntityQueryContext(world) {
    val position: SectPositionInfo by component()
}
