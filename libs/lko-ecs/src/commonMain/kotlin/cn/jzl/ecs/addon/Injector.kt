package cn.jzl.ecs.addon

import cn.jzl.di.DIMainBuilder

fun interface Injector {
    fun inject(builder: DIMainBuilder.() -> Unit)
}

