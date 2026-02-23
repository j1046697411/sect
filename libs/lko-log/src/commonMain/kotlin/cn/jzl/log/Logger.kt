package cn.jzl.log

import cn.jzl.core.log.ConsoleLogger as CoreConsoleLogger
import cn.jzl.core.log.Logger as CoreLogger
import cn.jzl.di.argPrototype
import cn.jzl.di.instance
import cn.jzl.di.singleton
import cn.jzl.ecs.addon.createAddon

/**
 * 日志接口（类型别名）
 */
typealias Logger = CoreLogger

/**
 * 控制台日志实现（类型别名）
 */
typealias ConsoleLogger = CoreConsoleLogger

val logAddon = createAddon("logAddon") {
    injects {
        // 绑定默认的 LogLevel 为 DEBUG
        this bind singleton<Any, LogLevel> { LogLevel.DEBUG }
        // 绑定 Logger 工厂，使用 argPrototype 接收标签参数
        this bind argPrototype<Any, String, Logger> { ConsoleLogger(instance(), it) }
    }
}
