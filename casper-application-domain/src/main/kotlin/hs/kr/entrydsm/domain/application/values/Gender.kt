package hs.kr.entrydsm.domain.application.values

/**
 * 성별
 */
enum class Gender(
    val displayName: String,
    val koreanName: String,
) {
    MALE("Male", "남"),
    FEMALE("Female", "여"),
    ;

    companion object {
        fun fromString(value: String?): Gender? {
            if (value.isNullOrBlank()) return null

            return when (value.uppercase().trim()) {
                "MALE", "M", "남" -> MALE
                "FEMALE", "F", "여" -> FEMALE
                else -> null
            }
        }
    }
}
