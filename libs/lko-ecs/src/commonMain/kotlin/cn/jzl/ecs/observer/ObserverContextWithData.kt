package cn.jzl.ecs.observer

interface ObserverContextWithData<E> : ObserverContext {
    val event: E
}