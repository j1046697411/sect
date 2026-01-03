package cn.jzl.ecs.query

import kotlin.coroutines.cancellation.CancellationException

@PublishedApi
internal data class AbortQueryException(val queryStream: QueryStreamScope) : CancellationException(null)