package hs.kr.entrydsm.application.domain.application.calculator

import hs.kr.entrydsm.application.domain.application.exception.ScoreCalculationException
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.math.min

/**
 * 입학전형 점수 계산기
 */
@Component
class ScoreCalculator {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private val ATTENDANCE_SCORE_TABLE = listOf(
            15.0, 14.0, 13.0, 12.0, 11.0, 10.0,
            9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0
        )

        private val GED_CONVERT_TABLE = listOf(
            98 to 5, 94 to 4, 90 to 3, 86 to 2, 0 to 1
        )
    }

    fun calculateScore(
        applicationType: ApplicationType,
        educationalStatus: EducationalStatus,
        scores: Map<String, Any>
    ): ScoreResult {
        try {
            val input = ScoreInput.from(scores)
            logger.info("점수 계산 시작 - 전형: $applicationType, 학력: $educationalStatus")

            val subjectScore = calculateSubjectScore(applicationType, educationalStatus, input)
            val attendanceScore = if (educationalStatus == EducationalStatus.QUALIFICATION_EXAM) 0.0
            else calculateAttendanceScore(input)
            val volunteerScore = if (educationalStatus == EducationalStatus.QUALIFICATION_EXAM) 0.0
            else calculateVolunteerScore(input)
            val bonusScore = calculateBonusScore(applicationType, input)

            val total = subjectScore + attendanceScore + volunteerScore + bonusScore

            return ScoreResult(subjectScore, attendanceScore, volunteerScore, bonusScore, total).also {
                logger.info("계산 완료 - 교과: $subjectScore, 출결: $attendanceScore, 봉사: $volunteerScore, 가산점: $bonusScore, 총점: $total")
            }
        } catch (e: ScoreCalculationException) {
            throw e
        } catch (e: Exception) {
            throw ScoreCalculationException("점수 계산 오류: ${e.message}", e)
        }
    }

    private fun calculateSubjectScore(
        type: ApplicationType,
        status: EducationalStatus,
        input: ScoreInput
    ): Double {
        return when (status) {
            EducationalStatus.QUALIFICATION_EXAM -> calculateGedSubjectScore(type, input)
            EducationalStatus.GRADUATE -> calculateRegularSubjectScore(type, input, isGraduate = true)
            EducationalStatus.PROSPECTIVE_GRADUATE -> calculateRegularSubjectScore(type, input, isGraduate = false)
        }
    }

    private fun calculateRegularSubjectScore(
        type: ApplicationType,
        input: ScoreInput,
        isGraduate: Boolean
    ): Double {
        // 필수 검증
        requireNotNull(input.grade3_1) { "3학년 1학기 성적이 필수입니다" }
        if (isGraduate) requireNotNull(input.grade3_2) { "졸업자는 3학년 2학기 성적이 필수입니다" }

        val j3 = if (isGraduate) {
            calculateSemesterScore(input.grade3_2) * 4 + calculateSemesterScore(input.grade3_1) * 4
        } else {
            calculateSemesterScore(input.grade3_1) * 8
        }

        val jA = input.grade2_2?.let { calculateSemesterScore(it) * 4 } ?: 0.0
        val jB = input.grade2_1?.let { calculateSemesterScore(it) * 4 } ?: 0.0

        val total = when {
            input.grade2_2 != null && input.grade2_1 != null -> j3 + jA + jB
            input.grade2_2 != null -> j3 + jA + (j3 + jA) / 3
            input.grade2_1 != null -> j3 + jB + (j3 + jB) / 3
            else -> j3 * 2.0
        }

        val multiplier = type.baseScoreMultiplier
        return total * multiplier
    }

    private fun calculateGedSubjectScore(type: ApplicationType, input: ScoreInput): Double {
        val scores = requireNotNull(input.gedScores) { "검정고시 성적이 필수입니다" }
        require(scores.isNotEmpty()) { "검정고시 성적이 비어있습니다" }

        val converted = scores.values.map { score ->
            GED_CONVERT_TABLE.first { score >= it.first }.second
        }

        val avg = converted.average()
        val factor = if (type == ApplicationType.COMMON) 34.0 else 22.0
        return avg * factor
    }

    private fun calculateAttendanceScore(input: ScoreInput): Double {
        val att = input.attendance ?: AttendanceInfo()
        val absence = (att.absence + (att.tardiness + att.earlyLeave + att.classExit) / 3.0).toInt()
        val idx = absence.coerceAtMost(14)
        return ATTENDANCE_SCORE_TABLE.getOrElse(idx) { 0.0 }
    }

    private fun calculateVolunteerScore(input: ScoreInput): Double =
        min((input.volunteerHours ?: 0).toDouble(), 15.0)

    private fun calculateBonusScore(type: ApplicationType, input: ScoreInput): Double {
        var bonus = 0.0
        if (input.algorithmAward == true) bonus += 3.0
        if (type != ApplicationType.COMMON && input.infoProcessingCert == true) bonus += 6.0
        return bonus
    }

    private fun calculateSemesterScore(grade: SemesterGrades?): Double {
        val scores = listOfNotNull(
            grade?.korean, grade?.social, grade?.history, grade?.math,
            grade?.science, grade?.tech, grade?.english
        ).filter { it in 1..5 }

        require(scores.isNotEmpty()) { "유효한 성적이 없습니다" }
        return scores.average()
    }

    data class ScoreResult(
        val subjectScore: Double,
        val attendanceScore: Double,
        val volunteerScore: Double,
        val bonusScore: Double,
        val totalScore: Double
    )

    data class ScoreInput(
        val grade3_2: SemesterGrades? = null,
        val grade3_1: SemesterGrades? = null,
        val grade2_2: SemesterGrades? = null,
        val grade2_1: SemesterGrades? = null,
        val gedScores: Map<String, Int>? = null,
        val attendance: AttendanceInfo? = null,
        val volunteerHours: Int? = null,
        val algorithmAward: Boolean? = null,
        val infoProcessingCert: Boolean? = null
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

            private fun extractSemesterGrades(scores: Map<String, Any>, semester: String): SemesterGrades? {
                val korean = getIntOrNull(scores, "korean_$semester") ?: 0
                val social = getIntOrNull(scores, "social_$semester") ?: 0
                val history = getIntOrNull(scores, "history_$semester") ?: 0
                val math = getIntOrNull(scores, "math_$semester") ?: 0
                val science = getIntOrNull(scores, "science_$semester") ?: 0
                val tech = getIntOrNull(scores, "tech_$semester") ?: 0
                val english = getIntOrNull(scores, "english_$semester") ?: 0

                return if (korean > 0 || social > 0 || history > 0 || math > 0 || science > 0 || tech > 0 || english > 0) {
                    SemesterGrades(korean, social, history, math, science, tech, english)
                } else null
            }

            private fun extractGedScores(scores: Map<String, Any>): Map<String, Int>? {
                val gedScores = mutableMapOf<String, Int>()
                listOf("Korean", "Social", "History", "Math", "Science", "English", "Tech").forEach { subject ->
                    val score = getIntOrNull(scores, "qualification$subject") ?: getIntOrNull(scores, "ged$subject")
                    score?.let { gedScores[subject] = it }
                }
                return gedScores.ifEmpty { null }
            }

            private fun extractAttendance(scores: Map<String, Any>): AttendanceInfo? {
                val absence = getIntOrNull(scores, "absence")
                val tardiness = getIntOrNull(scores, "tardiness")
                val earlyLeave = getIntOrNull(scores, "earlyLeave")
                val classExit = getIntOrNull(scores, "classExit")

                return if (absence != null || tardiness != null || earlyLeave != null || classExit != null) {
                    AttendanceInfo(absence ?: 0, tardiness ?: 0, earlyLeave ?: 0, classExit ?: 0)
                } else null
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

    data class SemesterGrades(
        val korean: Int,
        val social: Int,
        val history: Int,
        val math: Int,
        val science: Int,
        val tech: Int,
        val english: Int
    )

    data class AttendanceInfo(
        val absence: Int = 0,
        val tardiness: Int = 0,
        val earlyLeave: Int = 0,
        val classExit: Int = 0
    )
}