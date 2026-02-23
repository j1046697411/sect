/**
 * 任务系统 Addon
 *
 * 提供任务管理功能，包括：
 * - 任务执行管理（成功率计算、伤亡计算）
 * - 团队组建（1长老 + 3-5内门 + 10-20外门）
 * - 弟子晋升管理（外门晋升内门）
 * - 长老评估系统（根据性格评估弟子）
 * - 选拔任务管理（周期检测、名额计算）
 * - 政策配置管理（选拔周期、比例、资源分配）
 *
 * 使用方式：
 * ```kotlin
 * world.install(questAddon)
 * val questExecutionService by world.di.instance<QuestExecutionService>()
 * val teamFormationService by world.di.instance<TeamFormationService>()
 * val promotionService by world.di.instance<PromotionService>()
 * val elderEvaluationService by world.di.instance<ElderEvaluationService>()
 * val selectionTaskService by world.di.instance<SelectionTaskService>()
 * val policyService by world.di.instance<PolicyService>()
 *
 * // 组建团队
 * val team = teamFormationService.formTeam(questId)
 *
 * // 执行任务
 * val result = questExecutionService.executeQuest(questId)
 *
 * // 晋升弟子
 * val promotionResult = promotionService.promoteDisciple(discipleId)
 * ```
 */
package cn.jzl.sect.quest

import cn.jzl.di.new
import cn.jzl.di.singleton
import cn.jzl.ecs.addon.Phase
import cn.jzl.ecs.addon.createAddon
import cn.jzl.sect.quest.services.ElderEvaluationService
import cn.jzl.sect.quest.services.PolicyService
import cn.jzl.sect.quest.services.PromotionService
import cn.jzl.sect.quest.services.QuestExecutionService
import cn.jzl.sect.quest.services.SelectionTaskService
import cn.jzl.sect.quest.services.TeamFormationService

/**
 * 任务系统 Addon
 *
 * 负责注册任务系统相关组件和服务：
 * - [QuestExecutionService] 服务：任务执行管理
 * - [TeamFormationService] 服务：团队组建管理
 * - [PromotionService] 服务：弟子晋升管理
 * - [ElderEvaluationService] 服务：长老评估管理
 * - [SelectionTaskService] 服务：选拔任务管理
 * - [PolicyService] 服务：政策配置管理
 *
 * 依赖 [disciplesAddon] 提供弟子系统支持
 * 依赖 [combatAddon] 提供战斗系统支持
 *
 * 示例：
 * ```kotlin
 * world.install(questAddon)
 * ```
 */
val questAddon = createAddon("questAddon") {
    // 依赖弟子系统
    install(cn.jzl.sect.disciples.disciplesAddon)

    // 依赖战斗系统
    install(cn.jzl.sect.combat.combatAddon)

    // 注册服务
    injects {
        this bind singleton { new(::QuestExecutionService) }
        this bind singleton { new(::TeamFormationService) }
        this bind singleton { new(::PromotionService) }
        this bind singleton { new(::ElderEvaluationService) }
        this bind singleton { new(::SelectionTaskService) }
        this bind singleton { new(::PolicyService) }
    }

    // 生命周期回调 - 模块启用时
    on(Phase.ENABLE) {
        // 任务系统初始化逻辑（如需）
    }
}
