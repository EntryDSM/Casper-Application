package hs.kr.entrydsm.domain.application.values

data class ValidationResult(
    val valid: Boolean,
    val errors: List<String>,
    val missingFields: List<String>,
    val extraFields: List<String>
) {
    companion object {
        fun valid(): ValidationResult {
            return ValidationResult(
                valid = true,
                errors = emptyList(),
                missingFields = emptyList(),
                extraFields = emptyList()
            )
        }
        
        fun invalid(
            errors: List<String> = emptyList(),
            missingFields: List<String> = emptyList(),
            extraFields: List<String> = emptyList()
        ): ValidationResult {
            return ValidationResult(
                valid = false,
                errors = errors,
                missingFields = missingFields,
                extraFields = extraFields
            )
        }
    }
}