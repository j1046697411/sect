package cn.jzl.ecs.serialization.core

data class SerializationConfig(
    val enableValidation: Boolean = true,
    val enableVersioning: Boolean = true,
    val enableCompression: Boolean = false,
    val onMissingSerializer: OnMissingStrategy = OnMissingStrategy.WARN,
    val onValidationError: OnValidationError = OnValidationError.THROW,
    val skipMalformedComponents: Boolean = true,
    val namespaces: List<String> = listOf("lko-ecs"),
    val prefix: String = ""
)