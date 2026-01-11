package cn.jzl.ecs.serialization.validation

import cn.jzl.ecs.component.Component
import cn.jzl.ecs.serialization.core.SerializationContext
import kotlin.reflect.KClass

data class Schema(
    val kClass: KClass<*>,
    val requiredFields: List<String> = emptyList(),
    val optionalFields: List<String> = emptyList(),
    val validators: Map<String, DataValidator<*>> = emptyMap()
)

class SchemaValidator(
    private val schemas: Map<KClass<*>, Schema>,
    private val context: SerializationContext
) : DataValidator<Any> {
    override fun validate(data: Any): ValidationResult {
        val dataClass = data::class
        val schema = schemas[dataClass]

        if (schema == null) {
            return ValidationResult.warning("No schema defined for ${dataClass.simpleName}")
        }

        val errors = mutableListOf<ValidationError>()

        schema.requiredFields.forEach { fieldName ->
            if (!hasField(data, fieldName)) {
                errors.add(ValidationError(fieldName, "Required field '$fieldName' is missing", ErrorSeverity.ERROR))
            }
        }

        schema.validators.forEach { (fieldName, validator) ->
            val fieldValue = getFieldValue(data, fieldName)
            if (fieldValue != null) {
                @Suppress("UNCHECKED_CAST")
                val result = (validator as DataValidator<Any>).validate(fieldValue)
                result.errors.forEach { error ->
                    errors.add(ValidationError("${fieldName}.${error.path}", error.message, error.severity))
                }
            }
        }

        return if (errors.isEmpty()) ValidationResult.success()
        else ValidationResult(isValid = false, errors = errors)
    }

    private fun hasField(data: Any, fieldName: String): Boolean {
        return try {
            getFieldValue(data, fieldName) != null
        } catch (e: Exception) {
            false
        }
    }

    private fun getFieldValue(data: Any, fieldName: String): Any? {
        return try {
            val property = data::class.members.find { it.name == fieldName }
            if (property != null) {
                property.call(data)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun addSchema(schema: Schema) {
        schemas[schema.kClass] = schema
    }

    fun removeSchema(kClass: KClass<*>) {
        schemas.remove(kClass)
    }
}