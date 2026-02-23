package cn.jzl.sect.cultivation.events

import cn.jzl.sect.core.cultivation.Realm

data class BreakthroughSuccessEvent(
    val oldRealm: Realm,
    val oldLayer: Int,
    val newRealm: Realm,
    val newLayer: Int
)
