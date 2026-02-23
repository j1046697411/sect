/**
 * 战斗服务
 *
 * 提供战斗核心逻辑功能：
 * - 伤害计算
 * - 暴击判定
 * - 闪避判定
 * - 攻击执行
 * - 按速度排序
 */
package cn.jzl.sect.combat.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.combat.components.CombatStats
import cn.jzl.sect.combat.components.Combatant
import cn.jzl.sect.combat.systems.CombatSystem

/**
 * 战斗服务
 *
 * 提供战斗核心逻辑功能的服务代理：
 * - 伤害计算
 * - 暴击判定
 * - 闪避判定
 * - 攻击执行
 * - 按速度排序
 *
 * 使用方式：
 * ```kotlin
 * val combatService by world.di.instance<CombatService>()
 * val result = combatService.executeAttack(attacker, attackerStats, defender, defenderStats)
 * ```
 *
 * @property world ECS 世界实例
 */
class CombatService(override val world: World) : EntityRelationContext {

    private val combatSystem by lazy {
        CombatSystem()
    }

    /**
     * 计算伤害
     * 基于攻击力和防御力计算实际伤害
     *
     * @param attackerStats 攻击者属性
     * @param defenderStats 防守者属性
     * @return 伤害值
     */
    fun calculateDamage(attackerStats: CombatStats, defenderStats: CombatStats): Int {
        return combatSystem.calculateDamage(attackerStats, defenderStats)
    }

    /**
     * 计算暴击伤害
     *
     * @param baseDamage 基础伤害
     * @return 暴击伤害
     */
    fun calculateCriticalDamage(baseDamage: Int): Int {
        return combatSystem.calculateCriticalDamage(baseDamage)
    }

    /**
     * 按速度排序战斗参与者
     * 速度快的先行动
     *
     * @param combatants 战斗参与者列表
     * @return 排序后的列表
     */
    fun sortBySpeed(
        combatants: List<Pair<Combatant, CombatStats>>
    ): List<Pair<Combatant, CombatStats>> {
        return combatSystem.sortBySpeed(combatants)
    }

    /**
     * 检查是否触发暴击
     *
     * @param stats 战斗属性
     * @return 是否暴击
     */
    fun checkCritical(stats: CombatStats): Boolean {
        return combatSystem.checkCritical(stats)
    }

    /**
     * 检查是否闪避
     *
     * @param stats 战斗属性
     * @return 是否闪避成功
     */
    fun checkDodge(stats: CombatStats): Boolean {
        return combatSystem.checkDodge(stats)
    }

    /**
     * 执行攻击
     *
     * @param attacker 攻击者
     * @param attackerStats 攻击者属性
     * @param defender 防守者
     * @param defenderStats 防守者属性
     * @return 攻击结果
     */
    fun executeAttack(
        attacker: Combatant,
        attackerStats: CombatStats,
        defender: Combatant,
        defenderStats: CombatStats
    ): CombatSystem.AttackResult {
        return combatSystem.executeAttack(attacker, attackerStats, defender, defenderStats)
    }
}
