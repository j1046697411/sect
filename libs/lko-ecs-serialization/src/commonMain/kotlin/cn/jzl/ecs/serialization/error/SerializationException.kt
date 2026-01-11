package cn.jzl.ecs.serialization.error

sealed class SerializationException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    class MissingSerializerException(key: String, cause: Throwable? = null) :
        SerializationException("Missing serializer for component: $key", cause)

    class ValidationException(errors: List<ValidationError>, cause: Throwable? = null) :
        SerializationException("Validation failed with ${errors.size} error(s): ${errors.joinToString { it.message }}", cause)

    class VersionMismatchException(expected: String, actual: String, cause: Throwable? = null) :
        SerializationException("Version mismatch: expected $expected, got $actual", cause)

    class DataCorruptedException(message: String, cause: Throwable? = null) :
        SerializationException("Data corrupted: $message", cause)

    class FormatNotSupportedException(format: String, cause: Throwable? = null) :
        SerializationException("Format not supported: $format", cause)

    class ComponentSerializationException(componentName: String, cause: Throwable? = null) :
        SerializationException("Failed to serialize component: $componentName", cause)

    class ComponentDeserializationException(componentName: String, cause: Throwable? = null) :
        SerializationException("Failed to deserialize component: $componentName", cause)

    class EntitySerializationException(entityId: String, cause: Throwable? = null) :
        SerializationException("Failed to serialize entity: $entityId", cause)

    class EntityDeserializationException(cause: Throwable? = null) :
        SerializationException("Failed to deserialize entity", cause)
}

data class ValidationError(
    val path: String,
    val message: String,
    val severity: ErrorSeverity
)

enum class ErrorSeverity {
    ERROR,
    WARNING,
    INFO
}