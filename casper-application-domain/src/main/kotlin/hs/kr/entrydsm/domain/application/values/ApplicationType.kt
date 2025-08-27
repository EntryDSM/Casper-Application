package hs.kr.entrydsm.domain.application.values

/**
 * 전형 구분
 */
enum class ApplicationType(val description: String) {
    COMMON("일반전형"),
    MEISTER("마이스터전형"),
    SOCIAL("사회통합전형");
    
    companion object {
        fun fromDescription(description: String): ApplicationType? {
            return values().find { it.description == description }
        }
    }
}