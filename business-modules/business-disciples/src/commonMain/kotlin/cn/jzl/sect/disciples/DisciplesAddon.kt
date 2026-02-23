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
import cn.jzl.ecs.addon.Phase
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.sect.disciples.components.Relationship
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
    }

    // 注册服务
    injects {
        this bind singleton { new(::DiscipleInfoService) }
        this bind singleton { new(::RelationshipService) }
        this bind singleton { new(::MasterApprenticeService) }
    }

    // 生命周期回调 - 模块启用时
    on(Phase.ENABLE) {
        // 弟子系统初始化逻辑（如需）
    }
}
