package cn.jzl.ecs.serialization.validation

import cn.jzl.ecs.serialization.error.ErrorSeverity
import cn.jzl.ecs.serialization.error.ValidationError

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList()
) {
    companion object {
        fun success() = ValidationResult(isValid = true)

        fun error(message: String, path: String = "") = ValidationResult(
            isValid = false,
            errors = listOf(ValidationError(path, message, ErrorSeverity.ERROR))
        )

        fun warning(message: String, path: String = "") = ValidationResult(
            isValid = true,
            errors = listOf(ValidationError(path, message, ErrorSeverity.WARNING))
        )
    }

    fun combine(other: ValidationResult): ValidationResult {
        return ValidationResult(
            isValid = isValid && other.isValid,
            errors = errors + other.errors
        )
    }
}

interface DataValidator<T> {
    fun validate(data: T): ValidationResult
}

class TypeValidator<T> : DataValidator<T> {
    override fun validate(data: T): ValidationResult {
        return try {
            ValidationResult.success()
        } catch (e: ClassCastException) {
            ValidationResult.error("Type mismatch: ${e.message}")
        }
    }
}

class RangeValidator<T>(
    private val min: T?,
    private val max: T?,
) : DataValidator<T> {

    override fun validate(data: T): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        if (min != null && data is Comparable<*> && (data as Comparable<T>) < min!!) {
            errors.add(ValidationError("", "Value $data is less than minimum $min", ErrorSeverity.ERROR))
        }

        if (max != null && data is Comparable<*> && (data as Comparable<T>) > max!!) {
            errors.add(ValidationError("", "Value $data is greater than maximum $max", ErrorSeverity.ERROR))
        }

        return if (errors.isEmpty()) ValidationResult.success()
        else ValidationResult(isValid = false, errors = errors)
    }
}

class CompositeValidator<T>(private val validators: List<DataValidator<T>>) : DataValidator<T> {
    override fun validate(data: T): ValidationResult {
        return validators.fold(ValidationResult.success()) { acc, validator ->
            acc.combine(validator.validate(data))
        }
    }
}