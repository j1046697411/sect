package cn.jzl.sect.quest.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.quest.PolicyComponent
import cn.jzl.sect.core.quest.PolicyValidationResult
import cn.jzl.sect.core.quest.ResourceAllocation
import cn.jzl.sect.core.quest.defaultPolicy
import cn.jzl.sect.core.quest.validatePolicy

/**
 * 政策系统 - 管理宗门政策配置
 *
 * 功能：
 * - 管理宗门政策配置，包括选拔周期、选拔比例、资源分配比例
 * - 默认配置：
 *   - 选拔周期：5年
 *   - 选拔比例：5%
 *   - 资源分配：修炼40%、设施30%、储备30%
 */
class PolicySystem(private val world: World) {

    companion object {
        // 配置约束常量
        const val MIN_SELECTION_CYCLE = 3
        const val MAX_SELECTION_CYCLE = 10
        const val MIN_SELECTION_RATIO = 0.03f
        const val MAX_SELECTION_RATIO = 0.10f
    }

    /**
     * 获取当前政策配置
     * @param world ECS世界
     * @return 当前政策配置，如果不存在则返回默认配置
     */
    fun getCurrentPolicy(world: World): PolicyComponent {
        val query = world.query { PolicyQueryContext(world) }
        var policy: PolicyComponent? = null

        query.forEach { ctx ->
            policy = ctx.policy
        }

        return policy ?: defaultPolicy()
    }

    /**
     * 获取政策配置实体
     * @param world ECS世界
     * @return 政策配置实体，如果不存在则返回null
     */
    fun getPolicyEntity(world: World): Entity? {
        val query = world.query { PolicyQueryContext(world) }
        var entity: Entity? = null

        query.forEach { ctx ->
            entity = ctx.entity
        }

        return entity
    }

    /**
     * 更新政策配置
     * @param world ECS世界
     * @param newPolicy 新的政策配置
     * @return 是否更新成功
     */
    fun updatePolicy(world: World, newPolicy: PolicyComponent): Boolean {
        // 验证新配置
        val validationResult = validatePolicy(newPolicy)
        if (validationResult is PolicyValidationResult.Invalid) {
            return false
        }

        // 查找现有政策实体
        val existingEntity = getPolicyEntity(world)

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

        return true
    }

    /**
     * 验证政策配置
     * @param policy 要验证的政策配置
     * @return 验证结果
     */
    fun validatePolicy(policy: PolicyComponent): PolicyValidationResult {
        return cn.jzl.sect.core.quest.validatePolicy(policy)
    }

    /**
     * 重置为默认配置
     * @param world ECS世界
     * @return 重置后的默认配置
     */
    fun resetToDefault(world: World): PolicyComponent {
        val defaultConfig = defaultPolicy()
        updatePolicy(world, defaultConfig)
        return defaultConfig
    }

    /**
     * 初始化政策系统（如果不存在则创建默认配置）
     * @param world ECS世界
     * @return 当前政策配置
     */
    fun initialize(world: World): PolicyComponent {
        val existingEntity = getPolicyEntity(world)
        if (existingEntity == null) {
            val defaultConfig = defaultPolicy()
            world.entity {
                it.addComponent(defaultConfig)
            }
            return defaultConfig
        }
        return getCurrentPolicy(world)
    }

    /**
     * 创建自定义政策配置
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
 * 政策更新结果
 */
sealed class PolicyUpdateResult {
    data class Success(val policy: PolicyComponent) : PolicyUpdateResult()
    data class Failure(val reasons: List<String>) : PolicyUpdateResult()
}

/**
 * 扩展函数：使用验证结果更新政策
 * @param world ECS世界
 * @param newPolicy 新的政策配置
 * @return 更新结果
 */
fun PolicySystem.updatePolicyWithResult(
    world: World,
    newPolicy: PolicyComponent
): PolicyUpdateResult {
    val validationResult = validatePolicy(newPolicy)

    return when (validationResult) {
        is PolicyValidationResult.Valid -> {
            val success = updatePolicy(world, newPolicy)
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
 * @param world ECS世界
 * @return 是否已配置政策
 */
fun PolicySystem.isPolicyConfigured(world: World): Boolean {
    return getPolicyEntity(world) != null
}

/**
 * 扩展函数：获取资源分配详情
 * @param world ECS世界
 * @return 资源分配配置
 */
fun PolicySystem.getResourceAllocation(world: World): ResourceAllocation {
    return getCurrentPolicy(world).resourceAllocation
}
