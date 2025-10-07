package hs.kr.entrydsm.application.domain.calculator.presentation.dto.request

/**
 * 성적 계산 요청 DTO
 */
data class CalculateScoreRequest(
    val applicationType: String, // COMMON, MEISTER, SOCIAL
    val educationalStatus: String, // PROSPECTIVE_GRADUATE, GRADUATE, QUALIFICATION_EXAM
    val scores: ScoreData,
) {
    data class ScoreData(
        // 일반 학생 성적 (1~5점)
        val korean_3_2: Int? = null,
        val social_3_2: Int? = null,
        val history_3_2: Int? = null,
        val math_3_2: Int? = null,
        val science_3_2: Int? = null,
        val tech_3_2: Int? = null,
        val english_3_2: Int? = null,
        val korean_3_1: Int? = null,
        val social_3_1: Int? = null,
        val history_3_1: Int? = null,
        val math_3_1: Int? = null,
        val science_3_1: Int? = null,
        val tech_3_1: Int? = null,
        val english_3_1: Int? = null,
        val korean_2_2: Int? = null,
        val social_2_2: Int? = null,
        val history_2_2: Int? = null,
        val math_2_2: Int? = null,
        val science_2_2: Int? = null,
        val tech_2_2: Int? = null,
        val english_2_2: Int? = null,
        val korean_2_1: Int? = null,
        val social_2_1: Int? = null,
        val history_2_1: Int? = null,
        val math_2_1: Int? = null,
        val science_2_1: Int? = null,
        val tech_2_1: Int? = null,
        val english_2_1: Int? = null,
        // 검정고시 성적 (0~100점)
        val qualificationKorean: Int? = null,
        val qualificationSocial: Int? = null,
        val qualificationHistory: Int? = null,
        val qualificationMath: Int? = null,
        val qualificationScience: Int? = null,
        val qualificationEnglish: Int? = null,
        val qualificationTech: Int? = null,
        // 출결 정보
        val absence: Int? = null,
        val tardiness: Int? = null,
        val earlyLeave: Int? = null,
        val classExit: Int? = null,
        // 봉사활동
        val volunteer: Int? = null,
        // 가산점
        val algorithmAward: Boolean? = null,
        val infoProcessingCert: Boolean? = null,
    ) {
        fun toMap(): Map<String, Any> {
            val map = mutableMapOf<String, Any>()
            korean_3_2?.let { map["korean_3_2"] = it }
            social_3_2?.let { map["social_3_2"] = it }
            history_3_2?.let { map["history_3_2"] = it }
            math_3_2?.let { map["math_3_2"] = it }
            science_3_2?.let { map["science_3_2"] = it }
            tech_3_2?.let { map["tech_3_2"] = it }
            english_3_2?.let { map["english_3_2"] = it }
            korean_3_1?.let { map["korean_3_1"] = it }
            social_3_1?.let { map["social_3_1"] = it }
            history_3_1?.let { map["history_3_1"] = it }
            math_3_1?.let { map["math_3_1"] = it }
            science_3_1?.let { map["science_3_1"] = it }
            tech_3_1?.let { map["tech_3_1"] = it }
            english_3_1?.let { map["english_3_1"] = it }
            korean_2_2?.let { map["korean_2_2"] = it }
            social_2_2?.let { map["social_2_2"] = it }
            history_2_2?.let { map["history_2_2"] = it }
            math_2_2?.let { map["math_2_2"] = it }
            science_2_2?.let { map["science_2_2"] = it }
            tech_2_2?.let { map["tech_2_2"] = it }
            english_2_2?.let { map["english_2_2"] = it }
            korean_2_1?.let { map["korean_2_1"] = it }
            social_2_1?.let { map["social_2_1"] = it }
            history_2_1?.let { map["history_2_1"] = it }
            math_2_1?.let { map["math_2_1"] = it }
            science_2_1?.let { map["science_2_1"] = it }
            tech_2_1?.let { map["tech_2_1"] = it }
            english_2_1?.let { map["english_2_1"] = it }
            qualificationKorean?.let { map["qualificationKorean"] = it }
            qualificationSocial?.let { map["qualificationSocial"] = it }
            qualificationHistory?.let { map["qualificationHistory"] = it }
            qualificationMath?.let { map["qualificationMath"] = it }
            qualificationScience?.let { map["qualificationScience"] = it }
            qualificationEnglish?.let { map["qualificationEnglish"] = it }
            qualificationTech?.let { map["qualificationTech"] = it }
            absence?.let { map["absence"] = it }
            tardiness?.let { map["tardiness"] = it }
            earlyLeave?.let { map["earlyLeave"] = it }
            classExit?.let { map["classExit"] = it }
            volunteer?.let { map["volunteer"] = it }
            algorithmAward?.let { map["algorithmAward"] = it }
            infoProcessingCert?.let { map["infoProcessingCert"] = it }
            return map
        }
    }
}
