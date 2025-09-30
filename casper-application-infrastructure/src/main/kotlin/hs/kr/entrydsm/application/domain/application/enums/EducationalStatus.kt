package hs.kr.entrydsm.application.domain.application.enums

enum class EducationalStatus(
    val displayName: String,
    val description: String
) {
    PROSPECTIVE_GRADUATE("졸업예정자", "2025년도 중학교 졸업예정자"),
    GRADUATE("졸업자", "중학교 졸업자"),
    QUALIFICATION_EXAM("검정고시", "중학교 졸업학력 검정고시 합격자");

    companion object {
        fun fromString(value: String): EducationalStatus {
            return values().find { 
                it.name.equals(value, ignoreCase = true) || 
                it.displayName == value 
            } ?: throw IllegalArgumentException("지원하지 않는 학력 상태: $value")
        }
    }
}