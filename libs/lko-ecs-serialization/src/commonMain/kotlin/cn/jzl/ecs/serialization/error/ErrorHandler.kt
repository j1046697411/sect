package cn.jzl.ecs.serialization.error

data class ErrorHandlingResult(
    val shouldContinue: Boolean,
    val fallbackValue: Any? = null,
    val logged: Boolean = false
)

interface ErrorHandler {
    fun handle(exception: SerializationException, context: Map<String, Any> = emptyMap()): ErrorHandlingResult
}

class DefaultErrorHandler(
    private val logErrors: Boolean = true,
    private val throwOnCriticalErrors: Boolean = true
) : ErrorHandler {
    override fun handle(exception: SerializationException, context: Map<String, Any>): ErrorHandlingResult {
        if (logErrors) {
            val contextStr = context.entries.joinToString(", ") { "${it.key}=${it.value}" }
            println("[Serialization Error] ${exception::class.simpleName}: ${exception.message}. Context: $contextStr")
        }

        return when (exception) {
            is SerializationException.MissingSerializerException -> {
                if (throwOnCriticalErrors) {
                    throw exception
                }
                ErrorHandlingResult(shouldContinue = false, logged = true)
            }
            is SerializationException.ValidationException -> {
                if (throwOnCriticalErrors) {
                    throw exception
                }
                ErrorHandlingResult(shouldContinue = false, logged = true)
            }
            is SerializationException.VersionMismatchException -> {
                if (throwOnCriticalErrors) {
                    throw exception
                }
                ErrorHandlingResult(shouldContinue = false, logged = true)
            }
            is SerializationException.DataCorruptedException -> {
                if (throwOnCriticalErrors) {
                    throw exception
                }
                ErrorHandlingResult(shouldContinue = false, logged = true)
            }
            else -> {
                ErrorHandlingResult(shouldContinue = false, logged = true)
            }
        }
    }
}

class RetryErrorHandler(
    private val maxRetries: Int = 3,
    private val delegate: ErrorHandler = DefaultErrorHandler()
) : ErrorHandler {
    override fun handle(exception: SerializationException, context: Map<String, Any>): ErrorHandlingResult {
        var attempt = 0
        var lastResult: ErrorHandlingResult = delegate.handle(exception, context)

        while (attempt < maxRetries && !lastResult.shouldContinue) {
            attempt++
            println("[Retry] Attempt $attempt/${maxRetries} for ${exception::class.simpleName}")
            lastResult = delegate.handle(exception, context)
        }

        return lastResult
    }
}