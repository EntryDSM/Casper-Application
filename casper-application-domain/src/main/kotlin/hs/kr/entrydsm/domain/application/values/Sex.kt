package hs.kr.entrydsm.domain.application.values

/**
 * 성별
 */
enum class Sex(val description: String) {
    MALE("남성"),
    FEMALE("여성");
    
    companion object {
        fun fromDescription(description: String): Sex? {
            return values().find { it.description == description }
        }
    }
}