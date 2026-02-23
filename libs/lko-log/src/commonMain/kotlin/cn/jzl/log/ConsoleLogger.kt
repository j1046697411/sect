package cn.jzl.log

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