package hs.kr.entrydsm.domain.application.values

/**
 * 사회 배려 대상 구분
 */
enum class ApplicationRemark(val description: String) {
    NOTHING("해당없음"),
    ONE_PARENT("한부모가족"),
    FROM_NORTH_KOREA("북한이탈주민"),
    MULTICULTURAL_FAMILY("다문화가족"),
    BASIC_LIVING("기초생활수급권자"),
    LOWEST_INCOME("차상위계층"),
    TEEN_HOUSEHOLDER("소년소녀가장");
    
    companion object {
        fun fromDescription(description: String): ApplicationRemark? {
            return values().find { it.description == description }
        }
    }
}