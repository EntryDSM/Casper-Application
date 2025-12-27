package hs.kr.entrydsm.application.domain.application.service

import hs.kr.entrydsm.domain.application.values.EducationalStatus
import org.springframework.stereotype.Service

/**
 * 원서 검증 서비스
 *
 * 원서 제출 시 비즈니스 규칙에 따른 검증을 수행합니다.
 * ScoreCalculator와 동일한 검증 로직을 사용합니다.
 */
@Service
class ApplicationValidationService {

    /**
     * 성적 데이터를 검증합니다 (원서 제출 시 사용).
     *
     * ScoreCalculator를 실제로 실행하여 검증합니다.
     * 계산이 성공하면 데이터가 유효한 것으로 판단합니다.
     *
     * @param educationalStatus 교육 상태 (졸업예정자, 졸업자, 검정고시)
     * @param applicationData 원서 기본 정보 (applicationType 포함)
     * @param scoresData 성적 데이터
     * @throws IllegalStateException 검증 실패 시
     */
    fun validateScoresData(
        educationalStatus: EducationalStatus,
        applicationData: Map<String, Any>,
        scoresData: Map<String, Any>
    ) {
        when (educationalStatus) {
            EducationalStatus.PROSPECTIVE_GRADUATE -> {
                validateProspectiveGraduateScores(scoresData)
                validateAttendanceAndVolunteer(scoresData)
            }
            EducationalStatus.GRADUATE -> {
                validateGraduateScores(scoresData)
                validateAttendanceAndVolunteer(scoresData)
            }
            EducationalStatus.QUALIFICATION_EXAM -> {
                validateGedScores(scoresData)
                // 검정고시는 출결/봉사 검증 제외
            }
        }
    }

    /**
     * 졸업예정자 성적 정보를 검증합니다.
     */
    private fun validateProspectiveGraduateScores(scoresData: Map<String, Any>) {
        val requiredFields = listOf(
            "3학년 1학기 국어" to "korean_3_1",
            "3학년 1학기 사회" to "social_3_1",
            "3학년 1학기 역사" to "history_3_1",
            "3학년 1학기 수학" to "math_3_1",
            "3학년 1학기 과학" to "science_3_1",
            "3학년 1학기 기술·가정" to "tech_3_1",
            "3학년 1학기 영어" to "english_3_1",
            "2학년 2학기 국어" to "korean_2_2",
            "2학년 2학기 사회" to "social_2_2",
            "2학년 2학기 역사" to "history_2_2",
            "2학년 2학기 수학" to "math_2_2",
            "2학년 2학기 과학" to "science_2_2",
            "2학년 2학기 기술·가정" to "tech_2_2",
            "2학년 2학기 영어" to "english_2_2",
            "2학년 1학기 국어" to "korean_2_1",
            "2학년 1학기 사회" to "social_2_1",
            "2학년 1학기 역사" to "history_2_1",
            "2학년 1학기 수학" to "math_2_1",
            "2학년 1학기 과학" to "science_2_1",
            "2학년 1학기 기술·가정" to "tech_2_1",
            "2학년 1학기 영어" to "english_2_1",
        )

        requiredFields.forEach { (fieldName, key) ->
            val value = scoresData[key] as? Int
            if (value != null) {
                if (value < 0 || value > 5) {
                    throw IllegalStateException("$fieldName 성적이 올바르게 입력되지 않았습니다 (0-5점, 실제값: $value)")
                }
            }
        }
    }

    /**
     * 졸업자 성적 정보를 검증합니다.
     */
    private fun validateGraduateScores(scoresData: Map<String, Any>) {
        // 졸업예정자와 동일한 검증 + 3학년 2학기
        validateProspectiveGraduateScores(scoresData)

        val additionalFields = listOf(
            "3학년 2학기 국어" to "korean_3_2",
            "3학년 2학기 사회" to "social_3_2",
            "3학년 2학기 역사" to "history_3_2",
            "3학년 2학기 수학" to "math_3_2",
            "3학년 2학기 과학" to "science_3_2",
            "3학년 2학기 기술·가정" to "tech_3_2",
            "3학년 2학기 영어" to "english_3_2",
        )

        additionalFields.forEach { (fieldName, key) ->
            val value = scoresData[key] as? Int
            if (value != null) {
                if (value < 0 || value > 5) {
                    throw IllegalStateException("$fieldName 성적이 올바르게 입력되지 않았습니다 (0-5점)")
                }
            }
        }
    }

    /**
     * 검정고시 성적 정보를 검증합니다.
     */
    private fun validateGedScores(scoresData: Map<String, Any>) {
        val gedFields = listOf(
            "검정고시 국어" to "gedKorean",
            "검정고시 사회" to "gedSocial",
            "검정고시 역사" to "gedHistory",
            "검정고시 수학" to "gedMath",
            "검정고시 과학" to "gedScience",
            "검정고시 기술·가정" to "gedTech",
            "검정고시 영어" to "gedEnglish",
        )

        gedFields.forEach { (fieldName, key) ->
            val value = scoresData[key] as? Int
            if (value != null) {
                if (value < 0 || value > 100) {
                    throw IllegalStateException("$fieldName 성적이 올바르게 입력되지 않았습니다 (0-100점)")
                }
            }
        }
    }

    /**
     * 출석 및 봉사활동 정보를 검증합니다.
     */
    private fun validateAttendanceAndVolunteer(scoresData: Map<String, Any>) {
        val attendanceFields = listOf(
            "결석일수" to "absence",
            "지각횟수" to "tardiness",
            "조퇴횟수" to "earlyLeave",
            "결과횟수" to "classExit",
        )

        attendanceFields.forEach { (fieldName, key) ->
            val value = scoresData[key] as? Int
            if (value == null || value < 0) {
                throw IllegalStateException("$fieldName 이 올바르게 입력되지 않았습니다 (0 이상)")
            }
        }

        val volunteer = scoresData["volunteer"] as? Int
        if (volunteer == null || volunteer < 0) {
            throw IllegalStateException("봉사활동 시간이 올바르게 입력되지 않았습니다 (0 이상)")

        }
    }
}
