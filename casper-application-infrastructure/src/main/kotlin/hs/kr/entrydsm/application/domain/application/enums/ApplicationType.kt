package hs.kr.entrydsm.application.domain.application.enums

enum class ApplicationType(
    val displayName: String,
    val description: String,
    val baseScoreMultiplier: Double
) {
    COMMON("일반전형", "소프트웨어 분야에 소질과 적성이 있는 자", 1.75),
    MEISTER("마이스터 인재 전형", "SW분야 마이스터로 성장할 잠재력이 있는 자", 1.0),
    SOCIAL("사회통합 전형", "사회배려대상자", 1.0);

    companion object {
        fun fromString(value: String): ApplicationType {
            return values().find { 
                it.name.equals(value, ignoreCase = true) || 
                it.displayName == value 
            } ?: throw IllegalArgumentException("지원하지 않는 전형 타입: $value")
        }
    }
}