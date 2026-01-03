package cn.jzl.ecs.query

fun interface QueryCollector<T> {
    fun QueryStreamScope.emit(value: T)
}