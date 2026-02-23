package cn.jzl.sect.quest.services

import cn.jzl.sect.quest.components.PolicyComponent

/**
 * 政策更新结果
 */
sealed class PolicyUpdateResult {
    data class Success(val policy: PolicyComponent) : PolicyUpdateResult()
    data class Failure(val reasons: List<String>) : PolicyUpdateResult()
}
