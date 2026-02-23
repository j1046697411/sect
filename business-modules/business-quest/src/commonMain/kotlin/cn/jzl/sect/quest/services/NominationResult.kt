package cn.jzl.sect.quest.services

import cn.jzl.sect.core.quest.CandidateScore

/**
 * 候选人提名结果
 */
data class NominationResult(
    val elderId: Long,
    val candidates: List<CandidateScore>,
    val quota: Int,
    val actualCount: Int
)
