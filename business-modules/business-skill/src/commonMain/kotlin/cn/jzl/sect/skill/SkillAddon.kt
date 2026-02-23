/**
 * 功法系统 Addon
 *
 * 提供功法管理功能，包括：
 * - 功法学习管理
 * - 功法效果计算
 * - 功法传承机制
 *
 * 使用方式：
 * ```kotlin
 * world.install(skillAddon)
 * val skillLearningService by world.di.instance<SkillLearningService>()
 * val skillEffectService by world.di.instance<SkillEffectService>()
 * val skillInheritanceService by world.di.instance<SkillInheritanceService>()
 *
 * // 检查是否可以学习功法
 * val canLearn = skillLearningService.canLearnSkill(skill, realm, talent, learnedIds)
 *
 * // 计算功法效果
 * val effectValue = skillEffectService.applyEffect(effect, proficiency)
 *
 * // 检查是否可以传承功法
 * val canInherit = skillInheritanceService.canInherit(skill, learned, masterRealm, apprenticeRealm)
 * ```
 */
package cn.jzl.sect.skill

import cn.jzl.di.new
import cn.jzl.di.singleton
import cn.jzl.ecs.addon.Phase
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.sect.skill.components.Skill
import cn.jzl.sect.skill.components.SkillEffect
import cn.jzl.sect.skill.components.SkillLearned
import cn.jzl.sect.skill.services.SkillEffectService
import cn.jzl.sect.skill.services.SkillInheritanceService
import cn.jzl.sect.skill.services.SkillLearningService

/**
 * 功法系统 Addon
 *
 * 负责注册功法系统相关组件和服务：
 * - [Skill] 组件：功法基础信息
 * - [SkillLearned] 组件：已学习功法信息
 * - [SkillEffect] 组件：功法效果信息
 * - [SkillLearningService] 服务：处理功法学习条件和成功率计算
 * - [SkillEffectService] 服务：处理功法效果计算和应用
 * - [SkillInheritanceService] 服务：处理功法传承机制
 *
 * 示例：
 * ```kotlin
 * world.install(skillAddon)
 * ```
 */
val skillAddon = createAddon("skillAddon") {
    // 依赖弟子系统
    install(cn.jzl.sect.disciples.disciplesAddon)

    // 注册组件
    components {
        world.componentId<Skill>()
        world.componentId<SkillLearned>()
        world.componentId<SkillEffect>()
    }

    // 注册服务
    injects {
        this bind singleton { new(::SkillLearningService) }
        this bind singleton { new(::SkillEffectService) }
        this bind singleton { new(::SkillInheritanceService) }
    }

    // 生命周期回调 - 模块启用时
    on(Phase.ENABLE) {
        // 功法系统初始化逻辑（如需）
    }
}
