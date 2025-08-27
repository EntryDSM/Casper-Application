package hs.kr.entrydsm.domain.application.values

/**
 * 학력 구분
 */
enum class EducationalStatus(val description: String) {
    PROSPECTIVE_GRADUATE("졸업예정자"),
    GRADUATE("졸업자"),
    QUALIFICATION_EXAM("검정고시");
    
    companion object {
        fun fromDescription(description: String): EducationalStatus? {
            return values().find { it.description == description }
        }
    }
}