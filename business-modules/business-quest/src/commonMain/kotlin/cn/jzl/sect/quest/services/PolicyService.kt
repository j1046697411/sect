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

import cn.jzl.di.instance
import cn.jzl.ecs.World
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.entity
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.log.Logger
import cn.jzl.sect.quest.components.PolicyComponent
import cn.jzl.sect.quest.components.PolicyValidationResult
import cn.jzl.sect.quest.components.ResourceAllocation
import cn.jzl.sect.quest.components.defaultPolicy
import cn.jzl.sect.quest.components.validatePolicy

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

    private val log: Logger by world.di.instance(argProvider = { "PolicyService" })

    companion object {
        // 配置约束常量
        const val MIN_SELECTION_CYCLE = 3
        const val MAX_SELECTION_CYCLE = 10
        const val MIN_SELECTION_RATIO = 0.03f
        const val MAX_SELECTION_RATIO = 0.10f
    }

    /**
     * 获取当前政策配置
     *
     * @return 当前政策配置，如果不存在则返回默认配置
     */
    fun getCurrentPolicy(): PolicyComponent {
        val query = world.query { PolicyQueryContext(world) }
        var policy: PolicyComponent? = null

        query.forEach { ctx ->
            policy = ctx.policy
        }

        return policy ?: defaultPolicy()
    }

    /**
     * 获取政策配置实体
     *
     * @return 政策配置实体，如果不存在则返回null
     */
    fun getPolicyEntity(): Entity? {
        val query = world.query { PolicyQueryContext(world) }
        var entity: Entity? = null

        query.forEach { ctx ->
            entity = ctx.entity
        }

        return entity
    }

    /**
     * 更新政策配置
     *
     * @param newPolicy 新的政策配置
     * @return 是否更新成功
     */
    fun updatePolicy(newPolicy: PolicyComponent): Boolean {
        // 验证新配置
        val validationResult = validatePolicy(newPolicy)
        if (validationResult is PolicyValidationResult.Invalid) {
            return false
        }

        // 查找现有政策实体
        val existingEntity = getPolicyEntity()

        if (existingEntity != null) {
            // 使用 editor 更新组件
            world.editor(existingEntity) {
                it.addComponent(newPolicy)
            }
        } else {
            // 创建新政策实体
            world.entity {
                it.addComponent(newPolicy)
            }
        }

        log.debug { "更新政策配置完成: 成功" }
        return true
    }

    /**
     * 验证政策配置
     *
     * @param policy 要验证的政策配置
     * @return 验证结果
     */
    fun validatePolicy(policy: PolicyComponent): PolicyValidationResult {
        return cn.jzl.sect.quest.components.validatePolicy(policy)
    }

    /**
     * 重置为默认配置
     *
     * @return 重置后的默认配置
     */
    fun resetToDefault(): PolicyComponent {
        log.debug { "开始重置为默认配置" }
        val defaultConfig = defaultPolicy()
        updatePolicy(defaultConfig)
        log.debug { "重置为默认配置完成" }
        return defaultConfig
    }

    /**
     * 初始化政策系统（如果不存在则创建默认配置）
     *
     * @return 当前政策配置
     */
    fun initialize(): PolicyComponent {
        log.debug { "开始初始化政策系统" }
        val existingEntity = getPolicyEntity()
        val result = if (existingEntity == null) {
            val defaultConfig = defaultPolicy()
            world.entity {
                it.addComponent(defaultConfig)
            }
            log.debug { "初始化政策系统完成: 创建默认配置" }
            defaultConfig
        } else {
            log.debug { "初始化政策系统完成: 使用现有配置" }
            getCurrentPolicy()
        }
        return result
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
        return PolicyComponent(
            selectionCycleYears = selectionCycleYears,
            selectionRatio = selectionRatio,
            resourceAllocationRatio = 1.0f,
            resourceAllocation = ResourceAllocation(
                cultivation = cultivationRatio,
                facility = facilityRatio,
                reserve = reserveRatio
            )
        )
    }

    /**
     * 查询上下文 - 政策配置
     */
    class PolicyQueryContext(world: World) : EntityQueryContext(world) {
        val policy: PolicyComponent by component()
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
