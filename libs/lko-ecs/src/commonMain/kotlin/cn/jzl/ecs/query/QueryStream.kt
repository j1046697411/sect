package cn.jzl.ecs.query

interface QueryStream<T> : AutoCloseable {
    fun collect(collector: QueryCollector<T>)
}

