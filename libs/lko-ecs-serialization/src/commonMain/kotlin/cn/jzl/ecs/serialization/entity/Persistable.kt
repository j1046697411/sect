package cn.jzl.ecs.serialization.entity

data class Persistable(
    val hash: Int = 0,
    val version: String = "1.0"
) {
    fun updateHash(data: Any): Persistable {
        return copy(hash = data.hashCode())
    }

    fun hasChanged(data: Any): Boolean {
        return hash != data.hashCode()
    }
}