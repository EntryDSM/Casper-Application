package hs.kr.entrydsm.global.interfaces

/**
 * 검증 결과를 나타내는 데이터 클래스입니다.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    companion object {
        fun success() = ValidationResult(true)
        fun failure(errors: List<String>) = ValidationResult(false, errors)
        fun failureWithWarnings(errors: List<String>, warnings: List<String>) = 
            ValidationResult(false, errors, warnings)
    }
}