package cn.jzl.sect.cultivation.components

import cn.jzl.sect.core.cultivation.Realm

/**
 * 修炼进度组件 - 存储弟子的修炼进度信息
 */
data class CultivationProgress(
    val realm: Realm = Realm.MORTAL,
    val layer: Int = 1,
    val cultivation: Long = 0L,
    val maxCultivation: Long = 1000L
)
