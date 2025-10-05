package hs.kr.entrydsm.application.domain.application.calculator

import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import org.springframework.stereotype.Component
import kotlin.math.min

/**
 * 2026학년도 대덕소프트웨어마이스터고 입학전형 점수 계산기
 *
 * 계산 기준:
 * - 교과성적: 80점 만점 (전형별 배수 적용)
 * - 출석점수: 15점 만점
 * - 봉사활동점수: 15점 만점
 * - 가산점: 일반전형 3점, 특별전형 9점
 */
@Component
class ScoreCalculator {
    /**
     * 전형별 점수 계산
     */
    fun calculateScore(
        applicationType: ApplicationType,
        educationalStatus: EducationalStatus,
        scores: Map<String, Any>,
    ): ScoreResult {
        return try {
            val scoreInput = ScoreInput.from(scores)

            val subjectScore =
                calculateSubjectScore(
                    applicationType,
                    educationalStatus,
                    scoreInput,
                )

            val attendanceScore = calculateAttendanceScore(scoreInput)
            val volunteerScore = calculateVolunteerScore(scoreInput)
            val bonusScore = calculateBonusScore(applicationType, scoreInput)

            val totalScore = subjectScore + attendanceScore + volunteerScore + bonusScore

            ScoreResult(
                subjectScore = subjectScore,
                attendanceScore = attendanceScore,
                volunteerScore = volunteerScore,
                bonusScore = bonusScore,
                totalScore = totalScore,
            )
        } catch (e: Exception) {
            throw ScoreCalculationException("점수 계산 중 오류 발생: ${e.message}", e)
        }
    }

    /**
     * 교과성적 계산
     */
    private fun calculateSubjectScore(
        applicationType: ApplicationType,
        educationalStatus: EducationalStatus,
        scoreInput: ScoreInput,
    ): Double {
        val baseScore =
            when (educationalStatus) {
            EducationalStatus.PROSPECTIVE_GRADUATE ->
                calculateProspectiveGraduateSubjectScore(scoreInput)
            EducationalStatus.GRADUATE ->
                calculateGraduateSubjectScore(scoreInput)
            EducationalStatus.QUALIFICATION_EXAM ->
                calculateQualificationExamSubjectScore(scoreInput)
        }

        // 전형별 배수 적용 (일반전형 1.75, 특별전형 1.0)
        return baseScore * applicationType.baseScoreMultiplier
    }

    /**
     * 졸업예정자 교과성적 계산
     * - 3학년 1학기: 50% (40점)
     * - 2학년 2학기: 25% (20점)
     * - 2학년 1학기: 25% (20점)
     */
    private fun calculateProspectiveGraduateSubjectScore(scoreInput: ScoreInput): Double {
        // 3학년 1학기 (7과목 평균 × 8 = 40점)
        val grade31 =
            scoreInput.grade3_1
                ?: throw ScoreCalculationException("졸업예정자는 3학년 1학기 성적이 필수입니다")
        val score31 = calculateSemesterScore(grade31) * 8.0

        // 2학년 2학기 (7과목 평균 × 4 = 20점)
        val grade22 =
            scoreInput.grade2_2
                ?: throw ScoreCalculationException("졸업예정자는 2학년 2학기 성적이 필수입니다")
        val score22 = calculateSemesterScore(grade22) * 4.0

        // 2학년 1학기 (7과목 평균 × 4 = 20점)
        val grade21 =
            scoreInput.grade2_1
                ?: throw ScoreCalculationException("졸업예정자는 2학년 1학기 성적이 필수입니다")
        val score21 = calculateSemesterScore(grade21) * 4.0

        return score31 + score22 + score21 // 최대 80점
    }

    /**
     * 졸업자 교과성적 계산
     * - 3학년 2학기: 25% (20점)
     * - 3학년 1학기: 25% (20점)
     * - 2학년 2학기: 25% (20점)
     * - 2학년 1학기: 25% (20점)
     */
    private fun calculateGraduateSubjectScore(scoreInput: ScoreInput): Double {
        val grade32 =
            scoreInput.grade3_2
                ?: throw ScoreCalculationException("졸업자는 3학년 2학기 성적이 필수입니다")
        val score32 = calculateSemesterScore(grade32) * 4.0

        val grade31 =
            scoreInput.grade3_1
                ?: throw ScoreCalculationException("졸업자는 3학년 1학기 성적이 필수입니다")
        val score31 = calculateSemesterScore(grade31) * 4.0

        val grade22 =
            scoreInput.grade2_2
                ?: throw ScoreCalculationException("졸업자는 2학년 2학기 성적이 필수입니다")
        val score22 = calculateSemesterScore(grade22) * 4.0

        val grade21 =
            scoreInput.grade2_1
                ?: throw ScoreCalculationException("졸업자는 2학년 1학기 성적이 필수입니다")
        val score21 = calculateSemesterScore(grade21) * 4.0

        return score32 + score31 + score22 + score21 // 최대 80점
    }

    /**
     * 검정고시 교과성적 계산
     * 6개 과목 평균 → 80점 만점으로 환산
     */
    private fun calculateQualificationExamSubjectScore(scoreInput: ScoreInput): Double {
        val gedScores = scoreInput.gedScores
            ?: throw ScoreCalculationException("검정고시 출신자는 검정고시 성적이 필수입니다")

        val average = gedScores.values
            .map { it.toDouble() }
            .average()

        // 100점 만점 → 80점 만점으로 환산
        return (average / 100.0) * 80.0
    }

    /**
     * 학기별 7과목 평균 성적 계산 (5점 만점 기준)
     */
    private fun calculateSemesterScore(grades: SemesterGrades): Double {
        val gradeList = listOf(
            grades.korean,
            grades.social,
            grades.history,
            grades.math,
            grades.science,
            grades.tech,
            grades.english
        )

        // 1~5 범위 검증
        gradeList.forEach { grade ->
            if (grade !in 1..5) {
                throw ScoreCalculationException("성적은 1~5 사이여야 합니다: $grade")
            }
        }

        return gradeList.average()
    }

    /**
     * 출석점수 계산 (15점 만점)
     * 환산 결석 = 결석 + (지각+조퇴+결과)/3
     */
    private fun calculateAttendanceScore(scoreInput: ScoreInput): Double {
        val attendance = scoreInput.attendance ?: AttendanceInfo()

        val convertedAbsence = attendance.absence +
            (attendance.tardiness + attendance.earlyLeave + attendance.classExit) / 3.0

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
    private fun calculateVolunteerScore(scoreInput: ScoreInput): Double {
        val volunteer = scoreInput.volunteerHours ?: 0
        return min(volunteer.toDouble(), 15.0)
    }

    /**
     * 가산점 계산
     * - 알고리즘 경진대회 수상: 3점 (모든 전형)
     * - 정보처리기능사: 6점 (특별전형만)
     */
    private fun calculateBonusScore(
        applicationType: ApplicationType,
        scoreInput: ScoreInput,
    ): Double {
        var bonusScore = 0.0

        // 알고리즘 경진대회 수상 (모든 전형 3점)
        if (scoreInput.algorithmAward == true) {
            bonusScore += 3.0
        }

        // 정보처리기능사 (특별전형만 6점)
        if (applicationType != ApplicationType.COMMON) {
            if (scoreInput.infoProcessingCert == true) {
                bonusScore += 6.0
            }
        }

        return bonusScore
    }

    /**
     * 점수 계산 결과
     */
    data class ScoreResult(
        val subjectScore: Double, // 교과성적
        val attendanceScore: Double, // 출석점수
        val volunteerScore: Double, // 봉사활동점수
        val bonusScore: Double, // 가산점
        val totalScore: Double, // 총점
    )

    /**
     * 점수 입력 데이터
     */
    data class ScoreInput(
        val grade3_2: SemesterGrades? = null, // 3학년 2학기 (졸업자만)
        val grade3_1: SemesterGrades? = null, // 3학년 1학기
        val grade2_2: SemesterGrades? = null, // 2학년 2학기
        val grade2_1: SemesterGrades? = null, // 2학년 1학기
        val gedScores: Map<String, Int>? = null, // 검정고시 성적
        val attendance: AttendanceInfo? = null, // 출결 정보
        val volunteerHours: Int? = null, // 봉사활동 시간
        val algorithmAward: Boolean? = null, // 알고리즘 대회 수상
        val infoProcessingCert: Boolean? = null, // 정보처리기능사
    ) {
        companion object {
            fun from(scores: Map<String, Any>): ScoreInput {
                return ScoreInput(
                    grade3_2 = extractSemesterGrades(scores, "3_2"),
                    grade3_1 = extractSemesterGrades(scores, "3_1"),
                    grade2_2 = extractSemesterGrades(scores, "2_2"),
                    grade2_1 = extractSemesterGrades(scores, "2_1"),
                    gedScores = extractGedScores(scores),
                    attendance = extractAttendance(scores),
                    volunteerHours = getIntOrNull(scores, "volunteer"),
                    algorithmAward = getBooleanOrNull(scores, "algorithmAward"),
                    infoProcessingCert = getBooleanOrNull(scores, "infoProcessingCert")
                )
            }

            private fun extractSemesterGrades(
                scores: Map<String, Any>,
                semester: String,
            ): SemesterGrades? {
                val korean = getIntOrNull(scores, "korean_$semester")
                val social = getIntOrNull(scores, "social_$semester")
                val history = getIntOrNull(scores, "history_$semester")
                val math = getIntOrNull(scores, "math_$semester")
                val science = getIntOrNull(scores, "science_$semester")
                val tech = getIntOrNull(scores, "tech_$semester")
                val english = getIntOrNull(scores, "english_$semester")

                // 모든 과목이 있어야 해당 학기 성적으로 인정
                return if (korean != null && social != null && history != null &&
                    math != null && science != null && tech != null && english != null
                ) {
                    SemesterGrades(korean, social, history, math, science, tech, english)
                } else {
                    null
                }
            }

            private fun extractGedScores(scores: Map<String, Any>): Map<String, Int>? {
                val gedScores = mutableMapOf<String, Int>()

                listOf("Korean", "Social", "Math", "Science", "English", "Tech").forEach { subject ->
                    val key = "ged$subject"
                    getIntOrNull(scores, key)?.let { gedScores[subject] = it }
                }

                return if (gedScores.isEmpty()) null else gedScores
            }

            private fun extractAttendance(scores: Map<String, Any>): AttendanceInfo? {
                val absence = getIntOrNull(scores, "absence")
                val tardiness = getIntOrNull(scores, "tardiness")
                val earlyLeave = getIntOrNull(scores, "earlyLeave")
                val classExit = getIntOrNull(scores, "classExit")

                return if (absence != null || tardiness != null ||
                    earlyLeave != null || classExit != null
                ) {
                    AttendanceInfo(
                        absence ?: 0,
                        tardiness ?: 0,
                        earlyLeave ?: 0,
                        classExit ?: 0,
                    )
                } else {
                    null
                }
            }

            private fun getIntOrNull(data: Map<String, Any>, key: String): Int? {
                return when (val value = data[key]) {
                    is Int -> value
                    is Number -> value.toInt()
                    is String -> value.toIntOrNull()
                    else -> null
                }
            }

            private fun getBooleanOrNull(data: Map<String, Any>, key: String): Boolean? {
                return when (val value = data[key]) {
                    is Boolean -> value
                    is String -> value.toBooleanStrictOrNull()
                    else -> null
                }
            }
        }
    }

    /**
     * 학기별 성적 (성취도 1~5)
     */
    data class SemesterGrades(
        val korean: Int,
        val social: Int,
        val history: Int,
        val math: Int,
        val science: Int,
        val tech: Int,
        val english: Int
    )

    /**
     * 출결 정보
     */
    data class AttendanceInfo(
        val absence: Int = 0, // 결석
        val tardiness: Int = 0, // 지각
        val earlyLeave: Int = 0, // 조퇴
        val classExit: Int = 0, // 결과
    )
}

/**
 * 점수 계산 예외
 */
class ScoreCalculationException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
