package cn.jzl.log

import cn.jzl.di.argPrototype
import cn.jzl.di.instance
import cn.jzl.ecs.addon.createAddon

interface Logger {
    fun verbose(error: Throwable? = null, block: () -> Any)
    fun debug(error: Throwable? = null, block: () -> Any)
    fun info(error: Throwable? = null, block: () -> Any)
    fun warn(error: Throwable? = null, block: () -> Any)
    fun error(error: Throwable? = null, block: () -> Any)
}

val logAddon = createAddon("logAddon") {
    injects {
        this bind argPrototype { ConsoleLogger(instance(), it) }
    }
}