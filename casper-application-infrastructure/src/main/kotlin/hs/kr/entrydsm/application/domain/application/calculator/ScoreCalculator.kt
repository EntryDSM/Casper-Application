package hs.kr.entrydsm.application.domain.application.calculator

import hs.kr.entrydsm.application.domain.application.enums.ApplicationType
import hs.kr.entrydsm.application.domain.application.enums.EducationalStatus
import hs.kr.entrydsm.application.domain.application.enums.Achievement
import org.springframework.stereotype.Component

/**
 * 2026학년도 대덕소프트웨어마이스터고 입학전형 점수 계산기
 */
@Component
class ScoreCalculator {

    /**
     * 전형별 점수 계산
     */
    fun calculateScore(
        applicationType: ApplicationType,
        educationalStatus: EducationalStatus,
        scores: Map<String, Any>
    ): ScoreResult {
        return when (educationalStatus) {
            EducationalStatus.PROSPECTIVE_GRADUATE -> calculateForProspectiveGraduate(applicationType, scores)
            EducationalStatus.GRADUATE -> calculateForGraduate(applicationType, scores)
            EducationalStatus.QUALIFICATION_EXAM -> calculateForQualificationExam(applicationType, scores)
        }
    }

    /**
     * 졸업예정자 점수 계산 (최근 3개 학기)
     * - 3학년 1학기: 50% (40점)
     * - 2학년 2학기: 25% (20점)
     * - 2학년 1학기: 25% (20점)
     */
    private fun calculateForProspectiveGraduate(
        applicationType: ApplicationType,
        scores: Map<String, Any>
    ): ScoreResult {
        // 3학년 1학기 성적 (50%, 40점)
        val grade3_1_avg = calculateSemesterAverage(
            korean = getIntValue(scores, "korean_3_1"),
            social = getIntValue(scores, "social_3_1"),
            history = getIntValue(scores, "history_3_1"),
            math = getIntValue(scores, "math_3_1"),
            science = getIntValue(scores, "science_3_1"),
            tech = getIntValue(scores, "tech_3_1"),
            english = getIntValue(scores, "english_3_1")
        )
        val grade3_1_score = 8 * grade3_1_avg // 40점 만점

        // 2학년 2학기 성적 (25%, 20점)
        val grade2_2_avg = calculateSemesterAverage(
            korean = getIntValue(scores, "korean_2_2"),
            social = getIntValue(scores, "social_2_2"),
            history = getIntValue(scores, "history_2_2"),
            math = getIntValue(scores, "math_2_2"),
            science = getIntValue(scores, "science_2_2"),
            tech = getIntValue(scores, "tech_2_2"),
            english = getIntValue(scores, "english_2_2")
        )
        val grade2_2_score = 4 * grade2_2_avg // 20점 만점

        // 2학년 1학기 성적 (25%, 20점)
        val grade2_1_avg = calculateSemesterAverage(
            korean = getIntValue(scores, "korean_2_1"),
            social = getIntValue(scores, "social_2_1"),
            history = getIntValue(scores, "history_2_1"),
            math = getIntValue(scores, "math_2_1"),
            science = getIntValue(scores, "science_2_1"),
            tech = getIntValue(scores, "tech_2_1"),
            english = getIntValue(scores, "english_2_1")
        )
        val grade2_1_score = 4 * grade2_1_avg // 20점 만점

        val baseSubjectScore = grade3_1_score + grade2_2_score + grade2_1_score // 80점 만점
        val subjectScore = baseSubjectScore * applicationType.baseScoreMultiplier

        return calculateFinalScore(applicationType, subjectScore, scores)
    }

    /**
     * 졸업자 점수 계산 (최근 4개 학기)
     * - 3학년 2학기: 25% (20점)
     * - 3학년 1학기: 25% (20점) 
     * - 2학년 2학기: 25% (20점)
     * - 2학년 1학기: 25% (20점)
     */
    private fun calculateForGraduate(
        applicationType: ApplicationType,
        scores: Map<String, Any>
    ): ScoreResult {
        // 각 학기별 성적 (각 25%, 20점)
        val grade3_2_avg = calculateSemesterAverage(
            korean = getIntValue(scores, "korean_3_2"),
            social = getIntValue(scores, "social_3_2"),
            history = getIntValue(scores, "history_3_2"),
            math = getIntValue(scores, "math_3_2"),
            science = getIntValue(scores, "science_3_2"),
            tech = getIntValue(scores, "tech_3_2"),
            english = getIntValue(scores, "english_3_2")
        )
        val grade3_2_score = 4 * grade3_2_avg

        val grade3_1_avg = calculateSemesterAverage(
            korean = getIntValue(scores, "korean_3_1"),
            social = getIntValue(scores, "social_3_1"),
            history = getIntValue(scores, "history_3_1"),
            math = getIntValue(scores, "math_3_1"),
            science = getIntValue(scores, "science_3_1"),
            tech = getIntValue(scores, "tech_3_1"),
            english = getIntValue(scores, "english_3_1")
        )
        val grade3_1_score = 4 * grade3_1_avg

        val grade2_2_avg = calculateSemesterAverage(
            korean = getIntValue(scores, "korean_2_2"),
            social = getIntValue(scores, "social_2_2"),
            history = getIntValue(scores, "history_2_2"),
            math = getIntValue(scores, "math_2_2"),
            science = getIntValue(scores, "science_2_2"),
            tech = getIntValue(scores, "tech_2_2"),
            english = getIntValue(scores, "english_2_2")
        )
        val grade2_2_score = 4 * grade2_2_avg

        val grade2_1_avg = calculateSemesterAverage(
            korean = getIntValue(scores, "korean_2_1"),
            social = getIntValue(scores, "social_2_1"),
            history = getIntValue(scores, "history_2_1"),
            math = getIntValue(scores, "math_2_1"),
            science = getIntValue(scores, "science_2_1"),
            tech = getIntValue(scores, "tech_2_1"),
            english = getIntValue(scores, "english_2_1")
        )
        val grade2_1_score = 4 * grade2_1_avg

        val baseSubjectScore = grade3_2_score + grade3_1_score + grade2_2_score + grade2_1_score // 80점 만점
        val subjectScore = baseSubjectScore * applicationType.baseScoreMultiplier

        return calculateFinalScore(applicationType, subjectScore, scores)
    }

    /**
     * 검정고시 점수 계산
     */
    private fun calculateForQualificationExam(
        applicationType: ApplicationType,
        scores: Map<String, Any>
    ): ScoreResult {
        // 검정고시는 별도 환산점수 적용 (입학전형위원회 결정)
        // 임시로 평균 점수 계산
        val korean = getIntValue(scores, "gedKorean") ?: 0
        val social = getIntValue(scores, "gedSocial") ?: 0
        val math = getIntValue(scores, "gedMath") ?: 0
        val science = getIntValue(scores, "gedScience") ?: 0
        val english = getIntValue(scores, "gedEnglish") ?: 0
        val tech = getIntValue(scores, "gedTech") ?: 0

        val average = (korean + social + math + science + english + tech) / 6.0
        val baseSubjectScore = (average / 100.0) * 80.0 // 80점 만점으로 환산
        val subjectScore = baseSubjectScore * applicationType.baseScoreMultiplier

        return calculateFinalScore(applicationType, subjectScore, scores)
    }

    /**
     * 학기별 7개 교과 평균 계산
     */
    private fun calculateSemesterAverage(
        korean: Int?,
        social: Int?,
        history: Int?,
        math: Int?,
        science: Int?,
        tech: Int?,
        english: Int?
    ): Double {
        val grades = listOf(korean, social, history, math, science, tech, english)
        val gradePoints = grades.map { Achievement.getGradePoint(it) }
        return gradePoints.average()
    }

    /**
     * 최종 점수 계산 (교과성적 + 출석점수 + 봉사활동점수 + 가산점)
     */
    private fun calculateFinalScore(
        applicationType: ApplicationType,
        subjectScore: Double,
        scores: Map<String, Any>
    ): ScoreResult {
        val attendanceScore = calculateAttendanceScore(scores)
        val volunteerScore = calculateVolunteerScore(scores)
        val bonusScore = calculateBonusScore(applicationType, scores)

        val totalScore = subjectScore + attendanceScore + volunteerScore + bonusScore

        return ScoreResult(
            subjectScore = subjectScore,
            attendanceScore = attendanceScore,
            volunteerScore = volunteerScore,
            bonusScore = bonusScore,
            totalScore = totalScore
        )
    }

    /**
     * 출석점수 계산 (15점 만점)
     * 환산결석 = 결석일수 + (지각횟수/3) + (조퇴횟수/3) + (결과횟수/3)
     */
    private fun calculateAttendanceScore(scores: Map<String, Any>): Double {
        val absence = getIntValue(scores, "absence") ?: 0
        val tardiness = getIntValue(scores, "tardiness") ?: 0
        val earlyLeave = getIntValue(scores, "earlyLeave") ?: 0
        val classExit = getIntValue(scores, "classExit") ?: 0

        val convertedAbsence = absence + (tardiness / 3.0) + (earlyLeave / 3.0) + (classExit / 3.0)

        return when {
            convertedAbsence >= 15 -> 0.0
            convertedAbsence >= 13 -> 2.0
            convertedAbsence >= 12 -> 3.0
            convertedAbsence >= 11 -> 4.0
            convertedAbsence >= 10 -> 5.0
            convertedAbsence >= 9 -> 6.0
            convertedAbsence >= 8 -> 7.0
            convertedAbsence >= 7 -> 8.0
            convertedAbsence >= 6 -> 9.0
            convertedAbsence >= 5 -> 10.0
            convertedAbsence >= 4 -> 11.0
            convertedAbsence >= 3 -> 12.0
            convertedAbsence >= 2 -> 13.0
            convertedAbsence >= 1 -> 14.0
            else -> 15.0
        }
    }

    /**
     * 봉사활동점수 계산 (15점 만점)
     * 15시간 이상: 15점, 14시간 이하: 시간 = 점수
     */
    private fun calculateVolunteerScore(scores: Map<String, Any>): Double {
        val volunteer = getIntValue(scores, "volunteer") ?: 0
        return minOf(volunteer.toDouble(), 15.0)
    }

    /**
     * 가산점 계산
     * - 알고리즘 경진대회 수상: 3점
     * - 특별전형 정보처리기능사: 6점
     */
    private fun calculateBonusScore(applicationType: ApplicationType, scores: Map<String, Any>): Double {
        var bonusScore = 0.0

        // 알고리즘 경진대회 수상 (모든 전형 3점)
        val algorithmAward = getBooleanValue(scores, "algorithmAward") ?: false
        if (algorithmAward) {
            bonusScore += 3.0
        }

        // 정보처리기능사 (특별전형만 6점)
        if (applicationType == ApplicationType.MEISTER || applicationType == ApplicationType.SOCIAL) {
            val infoProcessingCert = getBooleanValue(scores, "infoProcessingCert") ?: false
            if (infoProcessingCert) {
                bonusScore += 6.0
            }
        }

        return bonusScore
    }

    private fun getIntValue(data: Map<String, Any>, key: String): Int? {
        return when (val value = data[key]) {
            is Int -> value
            is Number -> value.toInt()
            is String -> value.toIntOrNull()
            else -> null
        }
    }

    private fun getBooleanValue(data: Map<String, Any>, key: String): Boolean? {
        return when (val value = data[key]) {
            is Boolean -> value
            is String -> value.toBoolean()
            else -> null
        }
    }

    /**
     * 점수 계산 결과
     */
    data class ScoreResult(
        val subjectScore: Double,      // 교과성적
        val attendanceScore: Double,   // 출석점수
        val volunteerScore: Double,    // 봉사활동점수
        val bonusScore: Double,        // 가산점
        val totalScore: Double         // 총점
    )
}