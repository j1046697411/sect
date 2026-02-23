/**
 * 战斗系统 Addon
 *
 * 提供战斗管理功能，包括：
 * - 战斗核心逻辑（伤害计算、暴击、闪避）
 * - 战斗结算（奖励计算、评价）
 * - 战斗实力评估
 *
 * 使用方式：
 * ```kotlin
 * world.install(combatAddon)
 * val combatService by world.di.instance<CombatService>()
 * val settlementService by world.di.instance<CombatSettlementService>()
 * val powerService by world.di.instance<CombatPowerService>()
 *
 * // 执行攻击
 * val result = combatService.executeAttack(attacker, attackerStats, defender, defenderStats)
 *
 * // 结算战斗
 * val report = settlementService.generateSettlementReport(...)
 *
 * // 计算战斗实力
 * val power = powerService.calculateCombatPower(realm, stats)
 * ```
 */
package cn.jzl.sect.combat

import cn.jzl.di.new
import cn.jzl.di.singleton
import cn.jzl.ecs.addon.Phase
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.sect.combat.components.CombatStats
import cn.jzl.sect.combat.components.Combatant
import cn.jzl.sect.combat.services.CombatPowerService
import cn.jzl.sect.combat.services.CombatService
import cn.jzl.sect.combat.services.CombatSettlementService

/**
 * 战斗系统 Addon
 *
 * 负责注册战斗系统相关组件和服务：
 * - [Combatant] 组件：战斗参与者
 * - [CombatStats] 组件：战斗属性
 * - [CombatService] 服务：处理战斗核心逻辑
 * - [CombatSettlementService] 服务：处理战斗结算
 * - [CombatPowerService] 服务：处理战斗实力计算
 *
 * 示例：
 * ```kotlin
 * world.install(combatAddon)
 * ```
 */
val combatAddon = createAddon("combatAddon") {
    // 依赖技能系统
    install(cn.jzl.sect.skill.skillAddon)

    // 注册组件
    components {
        world.componentId<Combatant>()
        world.componentId<CombatStats>()
    }

    // 注册服务
    injects {
        this bind singleton { new(::CombatService) }
        this bind singleton { new(::CombatSettlementService) }
        this bind singleton { new(::CombatPowerService) }
    }

    // 生命周期回调 - 模块启用时
    on(Phase.ENABLE) {
        // 战斗系统初始化逻辑（如需）
    }
}
