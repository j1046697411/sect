package cn.jzl.sect.cultivation.events

import cn.jzl.sect.core.cultivation.Realm

data class BreakthroughFailedEvent(
    val currentRealm: Realm,
    val currentLayer: Int,
    val attemptedRealm: Realm,
    val attemptedLayer: Int
)
