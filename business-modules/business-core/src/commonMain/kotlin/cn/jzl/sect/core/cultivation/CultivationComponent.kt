package cn.jzl.sect.core.cultivation

data class CultivationComponent(
    val realm: Realm = Realm.MORTAL,
    val layer: Int = 1,
    val cultivation: Long = 0L,
    val maxCultivation: Long = 1000L
)
