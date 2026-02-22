package cn.jzl.ecs.serialization.validation

import cn.jzl.ecs.component.Component
import cn.jzl.ecs.serialization.core.SerializationContext
import cn.jzl.ecs.serialization.error.ErrorSeverity
import cn.jzl.ecs.serialization.error.ValidationError
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * 模式定义
 *
 * @param kClass 组件类
 * @param requiredFields 必需字段列表
 * @param optionalFields 可选字段列表
 * @param validators 字段验证器映射
 */
data class Schema(
    val kClass: KClass<*>,
    val requiredFields: List<String> = emptyList(),
    val optionalFields: List<String> = emptyList(),
    val validators: Map<String, DataValidator<*>> = emptyMap()
)

/**
 * 模式验证器
 *
 * 根据预定义的模式验证组件数据
 */
class SchemaValidator(
    private val schemas: MutableMap<KClass<*>, Schema>,
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

    @Suppress("UNCHECKED_CAST")
    private fun getFieldValue(data: Any, fieldName: String): Any? {
        return try {
            val property = data::class.memberProperties.find { it.name == fieldName }
            (property as? kotlin.reflect.KProperty1<Any, *>)?.get(data)
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
