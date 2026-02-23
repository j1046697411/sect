/**
 * 功法效果服务
 *
 * 提供功法效果计算和应用功能：
 * - 根据熟练度计算实际效果值
 * - 计算属性加成和效率加成
 * - 获取指定类型的效果列表
 */
package cn.jzl.sect.skill.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.skill.components.SkillEffect
import cn.jzl.sect.skill.components.SkillEffectType
import cn.jzl.sect.skill.systems.SkillEffectSystem

/**
 * 功法效果服务
 *
 * 提供功法效果计算和应用功能的核心服务：
 * - 根据熟练度计算实际效果值
 * - 计算属性加成和效率加成
 * - 获取指定类型的效果列表
 *
 * 使用方式：
 * ```kotlin
 * val skillEffectService by world.di.instance<SkillEffectService>()
 * val effectValue = skillEffectService.applyEffect(effect, proficiency)
 * val totalBonus = skillEffectService.calculateTotalAttributeBonus(effects, attribute, proficiency)
 * ```
 *
 * @property world ECS 世界实例
 */
class SkillEffectService(override val world: World) : EntityRelationContext {

    private val skillEffectSystem by lazy {
        SkillEffectSystem()
    }

    /**
     * 应用效果
     * 根据熟练度计算实际效果值
     *
     * @param effect 功法效果
     * @param proficiency 熟练度(0-100)
     * @return 实际效果值
     */
    fun applyEffect(effect: SkillEffect, proficiency: Int): Double {
        return skillEffectSystem.applyEffect(effect, proficiency)
    }

    /**
     * 计算总属性加成
     * 累加所有指定属性的属性加成效果
     *
     * @param effects 效果列表
     * @param attribute 目标属性
     * @param proficiency 熟练度
     * @return 总加成值
     */
    fun calculateTotalAttributeBonus(
        effects: List<SkillEffect>,
        attribute: String,
        proficiency: Int
    ): Double {
        return skillEffectSystem.calculateTotalAttributeBonus(effects, attribute, proficiency)
    }

    /**
     * 计算总效率加成
     * 累加所有指定活动的效率加成效果
     *
     * @param effects 效果列表
     * @param activity 目标活动
     * @param proficiency 熟练度
     * @return 总加成值
     */
    fun calculateTotalEfficiencyBonus(
        effects: List<SkillEffect>,
        activity: String,
        proficiency: Int
    ): Double {
        return skillEffectSystem.calculateTotalEfficiencyBonus(effects, activity, proficiency)
    }

    /**
     * 获取所有被动技能效果
     *
     * @param effects 效果列表
     * @return 被动技能效果列表
     */
    fun getPassiveSkillEffects(effects: List<SkillEffect>): List<SkillEffect> {
        return skillEffectSystem.getPassiveSkillEffects(effects)
    }

    /**
     * 获取所有主动技能效果
     *
     * @param effects 效果列表
     * @return 主动技能效果列表
     */
    fun getActiveSkillEffects(effects: List<SkillEffect>): List<SkillEffect> {
        return skillEffectSystem.getActiveSkillEffects(effects)
    }

    /**
     * 获取指定类型的所有效果
     *
     * @param effects 效果列表
     * @param type 效果类型
     * @return 指定类型的效果列表
     */
    fun getEffectsByType(effects: List<SkillEffect>, type: SkillEffectType): List<SkillEffect> {
        return skillEffectSystem.getEffectsByType(effects, type)
    }

    /**
     * 计算修炼效率加成
     * 专门用于计算修炼相关的效率加成
     *
     * @param effects 效果列表
     * @param proficiency 熟练度
     * @return 修炼效率加成百分比
     */
    fun calculateCultivationEfficiencyBonus(effects: List<SkillEffect>, proficiency: Int): Double {
        return skillEffectSystem.calculateCultivationEfficiencyBonus(effects, proficiency)
    }

    /**
     * 计算战斗属性加成
     * 专门用于计算战斗相关的属性加成
     *
     * @param effects 效果列表
     * @param attribute 战斗属性(attack/defense/speed等)
     * @param proficiency 熟练度
     * @return 属性加成值
     */
    fun calculateCombatAttributeBonus(
        effects: List<SkillEffect>,
        attribute: String,
        proficiency: Int
    ): Double {
        return skillEffectSystem.calculateCombatAttributeBonus(effects, attribute, proficiency)
    }
}
