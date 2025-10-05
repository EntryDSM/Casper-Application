package hs.kr.entrydsm.domain.application.values

enum class EducationalStatus(val displayName: String) {
    GRADUATE("졸업"),
    PROSPECTIVE_GRADUATE("졸업예정"),
    QUALIFICATION_EXAM("검정고시"),
    ;

    companion object {
        fun fromString(value: String): EducationalStatus {
            return entries.find {
                it.name.equals(value, ignoreCase = true) ||
                    it.displayName == value
            } ?: throw IllegalArgumentException("지원하지 않는 교육 상태: $value")
        }
    }
}