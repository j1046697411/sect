/**
 * 宗门状态服务
 *
 * 提供宗门健康状况检测和财务摘要功能：
 * - 宗门状态检测（正常/警告/危急/解散）
 * - 财务摘要获取
 * - 破产风险评估
 *
 * 使用方式：
 * ```kotlin
 * val sectStatusService by world.di.instance<SectStatusService>()
 *
 * // 检查宗门状态
 * val status = sectStatusService.checkSectStatus()
 *
 * // 获取财务摘要
 * val summary = sectStatusService.getFinancialSummary()
 * ```
 */
package cn.jzl.sect.facility.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.facility.systems.FinancialSummary
import cn.jzl.sect.facility.systems.SectStatus
import cn.jzl.sect.facility.systems.SectStatusSystem

/**
 * 宗门状态服务
 *
 * 代理 [SectStatusSystem] 的功能，提供宗门状态检测和财务摘要服务
 *
 * @property world ECS 世界实例
 */
class SectStatusService(override val world: World) : EntityRelationContext {

    private val sectStatusSystem by lazy {
        SectStatusSystem(world)
    }

    /**
     * 检查宗门状态
     * @return 宗门状态评估
     */
    fun checkSectStatus(): SectStatus {
        return sectStatusSystem.checkSectStatus()
    }

    /**
     * 获取宗门财务摘要
     * @return 财务摘要信息
     */
    fun getFinancialSummary(): FinancialSummary {
        return sectStatusSystem.getFinancialSummary()
    }
}
