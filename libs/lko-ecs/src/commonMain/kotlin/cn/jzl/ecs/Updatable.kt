package cn.jzl.ecs

import kotlin.time.Duration

fun interface Updatable {
    fun update(delta: Duration)
}