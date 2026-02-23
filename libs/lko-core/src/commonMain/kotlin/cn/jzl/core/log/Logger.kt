package cn.jzl.core.log

/**
 * 日志接口
 *
 * 提供统一的日志记录接口，支持不同级别的日志输出
 */
interface Logger {
    fun verbose(error: Throwable? = null, block: () -> Any)
    fun debug(error: Throwable? = null, block: () -> Any)
    fun info(error: Throwable? = null, block: () -> Any)
    fun warn(error: Throwable? = null, block: () -> Any)
    fun error(error: Throwable? = null, block: () -> Any)
}
