package hs.kr.entrydsm.application.domain.application.presentation.dto.request

import hs.kr.entrydsm.domain.application.usecase.CreateScoreCommand
import java.math.BigDecimal
import java.util.UUID

data class CreateScoreRequest(
    // 기본 점수
    val attendanceScore: Int?,
    val volunteerScore: BigDecimal?,
    
    // 학기별 과목별 성적 - 3학년 1학기
    val korean_3_1: String? = null,
    val social_3_1: String? = null,
    val history_3_1: String? = null,
    val math_3_1: String? = null,
    val science_3_1: String? = null,
    val english_3_1: String? = null,
    val techAndHome_3_1: String? = null,
    
    // 학기별 과목별 성적 - 3학년 2학기 (기존 필드명 유지)
    val koreanGrade: String? = null,
    val socialGrade: String? = null,
    val historyGrade: String? = null,
    val mathGrade: String? = null,
    val scienceGrade: String? = null,
    val englishGrade: String? = null,
    val techAndHomeGrade: String? = null,
    
    // 학기별 과목별 성적 - 2학년 2학기
    val korean_2_2: String? = null,
    val social_2_2: String? = null,
    val history_2_2: String? = null,
    val math_2_2: String? = null,
    val science_2_2: String? = null,
    val english_2_2: String? = null,
    val techAndHome_2_2: String? = null,
    
    // 학기별 과목별 성적 - 2학년 1학기
    val korean_2_1: String? = null,
    val social_2_1: String? = null,
    val history_2_1: String? = null,
    val math_2_1: String? = null,
    val science_2_1: String? = null,
    val english_2_1: String? = null,
    val techAndHome_2_1: String? = null,
    
    // 검정고시용
    val qualificationScore: BigDecimal? = null,
    val qualificationKorean: BigDecimal? = null,
    val qualificationSocial: BigDecimal? = null,
    val qualificationMath: BigDecimal? = null,
    val qualificationScience: BigDecimal? = null,
    val qualificationEnglish: BigDecimal? = null,
    val qualificationOpt: BigDecimal? = null,
    
    // 졸업/졸업예정자용
    val thirdGradeScore: BigDecimal? = null,
    val thirdBeforeScore: BigDecimal? = null,
    val thirdBeforeBeforeScore: BigDecimal? = null,
    val thirdScore: BigDecimal? = null,
    
    // 가산점
    val extraScore: BigDecimal = BigDecimal.ZERO
) {
    fun toCommand(userId: UUID): CreateScoreCommand {
        return CreateScoreCommand(
            userId = userId,
            attendanceScore = attendanceScore,
            volunteerScore = volunteerScore,
            // 학기별 과목별 성적
            korean_3_1 = korean_3_1,
            social_3_1 = social_3_1,
            history_3_1 = history_3_1,
            math_3_1 = math_3_1,
            science_3_1 = science_3_1,
            english_3_1 = english_3_1,
            techAndHome_3_1 = techAndHome_3_1,
            koreanGrade = koreanGrade,
            socialGrade = socialGrade,
            historyGrade = historyGrade,
            mathGrade = mathGrade,
            scienceGrade = scienceGrade,
            englishGrade = englishGrade,
            techAndHomeGrade = techAndHomeGrade,
            korean_2_2 = korean_2_2,
            social_2_2 = social_2_2,
            history_2_2 = history_2_2,
            math_2_2 = math_2_2,
            science_2_2 = science_2_2,
            english_2_2 = english_2_2,
            techAndHome_2_2 = techAndHome_2_2,
            korean_2_1 = korean_2_1,
            social_2_1 = social_2_1,
            history_2_1 = history_2_1,
            math_2_1 = math_2_1,
            science_2_1 = science_2_1,
            english_2_1 = english_2_1,
            techAndHome_2_1 = techAndHome_2_1,
            // 검정고시
            qualificationScore = qualificationScore,
            qualificationKorean = qualificationKorean,
            qualificationSocial = qualificationSocial,
            qualificationMath = qualificationMath,
            qualificationScience = qualificationScience,
            qualificationEnglish = qualificationEnglish,
            qualificationOpt = qualificationOpt,
            // 학기별 점수
            thirdGradeScore = thirdGradeScore,
            thirdBeforeScore = thirdBeforeScore,
            thirdBeforeBeforeScore = thirdBeforeBeforeScore,
            thirdScore = thirdScore,
            extraScore = extraScore
        )
    }
}