package cn.jzl.core.log

/**
 * 控制台日志实现
 *
 * 将日志输出到控制台的标准输出
 *
 * @param logLevel 最低日志级别，低于此级别的日志将被忽略
 * @param tag 日志标签，用于标识日志来源
 */
class ConsoleLogger(
    private val logLevel: LogLevel,
    private val tag: String
) : Logger {

    private inline fun log(logLevel: LogLevel, error: Throwable?, block: () -> Any) {
        if (logLevel.ordinal < this.logLevel.ordinal) return
        val msg = block()
        println("$tag: $msg")
        error?.printStackTrace()
    }

    override fun verbose(error: Throwable?, block: () -> Any): Unit =
        log(LogLevel.VERBOSE, error, block)

    override fun debug(error: Throwable?, block: () -> Any): Unit =
        log(LogLevel.DEBUG, error, block)

    override fun info(error: Throwable?, block: () -> Any): Unit = log(LogLevel.INFO, error, block)

    override fun warn(error: Throwable?, block: () -> Any): Unit = log(LogLevel.WARN, error, block)

    override fun error(error: Throwable?, block: () -> Any): Unit =
        log(LogLevel.ERROR, error, block)
}
