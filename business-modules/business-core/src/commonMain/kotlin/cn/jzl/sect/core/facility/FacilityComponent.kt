package cn.jzl.sect.core.facility

data class Facility(
    val type: FacilityType,
    val level: Int = 1,
    val capacity: Int = 0,
    val efficiency: Float = 1.0f
)
