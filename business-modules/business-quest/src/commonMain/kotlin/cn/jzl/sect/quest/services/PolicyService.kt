/**
 * 政策服务
 *
 * 提供政策配置管理功能：
 * - 获取当前政策配置
 * - 更新政策配置
 * - 验证政策配置
 * - 重置为默认配置
 */
package cn.jzl.sect.quest.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.core.quest.PolicyComponent
import cn.jzl.sect.core.quest.PolicyValidationResult
import cn.jzl.sect.core.quest.ResourceAllocation
import cn.jzl.sect.quest.systems.PolicySystem
import cn.jzl.sect.quest.systems.PolicyUpdateResult

/**
 * 政策服务
 *
 * 提供政策配置管理功能的核心服务：
 * - 获取当前政策配置
 * - 更新政策配置
 * - 验证政策配置
 * - 重置为默认配置
 *
 * 使用方式：
 * ```kotlin
 * val policyService by world.di.instance<PolicyService>()
 * val policy = policyService.getCurrentPolicy()
 * ```
 *
 * @property world ECS 世界实例
 */
class PolicyService(override val world: World) : EntityRelationContext {

    private val policySystem by lazy {
        PolicySystem(world)
    }

    /**
     * 获取当前政策配置
     *
     * @return 当前政策配置，如果不存在则返回默认配置
     */
    fun getCurrentPolicy(): PolicyComponent {
        return policySystem.getCurrentPolicy(world)
    }

    /**
     * 获取政策配置实体
     *
     * @return 政策配置实体，如果不存在则返回null
     */
    fun getPolicyEntity(): Entity? {
        return policySystem.getPolicyEntity(world)
    }

    /**
     * 更新政策配置
     *
     * @param newPolicy 新的政策配置
     * @return 是否更新成功
     */
    fun updatePolicy(newPolicy: PolicyComponent): Boolean {
        return policySystem.updatePolicy(world, newPolicy)
    }

    /**
     * 验证政策配置
     *
     * @param policy 要验证的政策配置
     * @return 验证结果
     */
    fun validatePolicy(policy: PolicyComponent): PolicyValidationResult {
        return policySystem.validatePolicy(policy)
    }

    /**
     * 重置为默认配置
     *
     * @return 重置后的默认配置
     */
    fun resetToDefault(): PolicyComponent {
        return policySystem.resetToDefault(world)
    }

    /**
     * 初始化政策系统（如果不存在则创建默认配置）
     *
     * @return 当前政策配置
     */
    fun initialize(): PolicyComponent {
        return policySystem.initialize(world)
    }

    /**
     * 创建自定义政策配置
     *
     * @param selectionCycleYears 选拔周期（年）
     * @param selectionRatio 选拔比例（0.0 - 1.0）
     * @param cultivationRatio 修炼分配比例（0-100）
     * @param facilityRatio 设施分配比例（0-100）
     * @param reserveRatio 储备分配比例（0-100）
     * @return 政策配置对象
     */
    fun createPolicy(
        selectionCycleYears: Int,
        selectionRatio: Float,
        cultivationRatio: Int,
        facilityRatio: Int,
        reserveRatio: Int
    ): PolicyComponent {
        return policySystem.createPolicy(
            selectionCycleYears,
            selectionRatio,
            cultivationRatio,
            facilityRatio,
            reserveRatio
        )
    }
}

/**
 * 扩展函数：使用验证结果更新政策
 *
 * @param newPolicy 新的政策配置
 * @return 更新结果
 */
fun PolicyService.updatePolicyWithResult(newPolicy: PolicyComponent): PolicyUpdateResult {
    val validationResult = validatePolicy(newPolicy)

    return when (validationResult) {
        is PolicyValidationResult.Valid -> {
            val success = updatePolicy(newPolicy)
            if (success) {
                PolicyUpdateResult.Success(newPolicy)
            } else {
                PolicyUpdateResult.Failure(listOf("更新政策配置失败"))
            }
        }
        is PolicyValidationResult.Invalid -> {
            PolicyUpdateResult.Failure(validationResult.reasons)
        }
    }
}

/**
 * 扩展函数：检查政策是否已配置
 *
 * @return 是否已配置政策
 */
fun PolicyService.isPolicyConfigured(): Boolean {
    return getPolicyEntity() != null
}

/**
 * 扩展函数：获取资源分配详情
 *
 * @return 资源分配配置
 */
fun PolicyService.getResourceAllocation(): ResourceAllocation {
    return getCurrentPolicy().resourceAllocation
}
