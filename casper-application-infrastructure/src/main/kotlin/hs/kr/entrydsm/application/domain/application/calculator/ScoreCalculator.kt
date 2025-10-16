package hs.kr.entrydsm.application.domain.application.calculator

import hs.kr.entrydsm.application.domain.application.exception.ScoreCalculationException
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(ScoreCalculator::class.java)

    /**
     * 전형별 점수 계산
     */
    fun calculateScore(
        applicationType: ApplicationType,
        educationalStatus: EducationalStatus,
        scores: Map<String, Any>,
    ): ScoreResult {
        return try {
            logger.info("=== ScoreCalculator 입력 ===")
            logger.info("전형: $applicationType, 학력: $educationalStatus")
            logger.info("입력 scores 맵 크기: ${scores.size}, 내용: $scores")

            val scoreInput = ScoreInput.from(scores)
            logger.info("파싱된 ScoreInput: $scoreInput")

            val subjectScore =
                calculateSubjectScore(
                    applicationType,
                    educationalStatus,
                    scoreInput,
                )

            // 검정고시는 출석/봉사 점수 없음 (교과만 반영)
            val attendanceScore =
                if (educationalStatus == EducationalStatus.QUALIFICATION_EXAM) {
                    logger.info("검정고시 - 출석점수 없음 (0점)")
                    0.0
                } else {
                    calculateAttendanceScore(scoreInput)
                }

            val volunteerScore =
                if (educationalStatus == EducationalStatus.QUALIFICATION_EXAM) {
                    logger.info("검정고시 - 봉사점수 없음 (0점)")
                    0.0
                } else {
                    calculateVolunteerScore(scoreInput)
                }

            val bonusScore = calculateBonusScore(applicationType, scoreInput)

            val totalScore = subjectScore + attendanceScore + volunteerScore + bonusScore

            logger.info("=== 최종 점수 ===")
            logger.info("교과: $subjectScore, 출결: $attendanceScore, 봉사: $volunteerScore, 가산점: $bonusScore, 총점: $totalScore")

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
        // 검정고시는 전형별 배수가 다르므로 내부에서 직접 처리
        if (educationalStatus == EducationalStatus.QUALIFICATION_EXAM) {
            return calculateQualificationExamSubjectScore(applicationType, scoreInput)
        }

        val baseScore =
            when (educationalStatus) {
                EducationalStatus.PROSPECTIVE_GRADUATE ->
                    calculateProspectiveGraduateSubjectScore(scoreInput)
                EducationalStatus.GRADUATE ->
                    calculateGraduateSubjectScore(scoreInput)
                else -> 0.0 // 이미 위에서 검정고시는 처리됨
            }

        // 전형별 배수 적용 (일반전형 1.75, 특별전형 1.0) - 고교생만
        return baseScore * applicationType.baseScoreMultiplier
    }

    /**
     * 졸업예정자 교과성적 계산 (80점 만점)
     *
     * 공식: J = J₃ + J_A + J_B
     * - J₃ (3학년 1학기): 8 × (S₃₁ / N₃₁) [50%, 40점]
     * - J_A (직전학기, 2-2학기): 4 × (S_A / N_A) [25%, 20점]
     * - J_B (직전전학기, 2-1학기): 4 × (S_B / N_B) [25%, 20점]
     *
     * 성적 부족 시 환산:
     * - 3학년만 있는 경우: J_A = J_B = J₃ / 2
     * - 3학년 + 직전학기만: J_B = (J₃ + J_A) × 1/3
     * - 3학년 + 직전전학기만: J_A = (J₃ + J_B) × 1/3
     */
    private fun calculateProspectiveGraduateSubjectScore(scoreInput: ScoreInput): Double {
        val grade31 =
            scoreInput.grade3_1
                ?: throw ScoreCalculationException("졸업예정자는 3학년 1학기 성적이 필수입니다")

        // S₃₁ / N₃₁ 계산
        val avg31 = calculateSemesterScore(grade31)

        // J₃ = 8 × (S₃₁ / N₃₁)
        val j3 = avg31 * 8.0

        logger.info("졸업예정자 계산 - J₃: $j3 (평균: $avg31)")

        val grade22 = scoreInput.grade2_2
        val grade21 = scoreInput.grade2_1

        return when {
            // 모든 학기 성적이 있는 경우: J = J₃ + J_A + J_B
            grade22 != null && grade21 != null -> {
                val avg22 = calculateSemesterScore(grade22)
                val avg21 = calculateSemesterScore(grade21)
                val jA = avg22 * 4.0
                val jB = avg21 * 4.0
                val total = j3 + jA + jB
                logger.info("모든 학기 - J_A: $jA, J_B: $jB, 총점: $total")
                total
            }
            // 3학년 + 2-2만 있는 경우: J_B = (J₃ + J_A) × 1/3
            grade22 != null && grade21 == null -> {
                val avg22 = calculateSemesterScore(grade22)
                val jA = avg22 * 4.0
                val jB = (j3 + jA) * (1.0 / 3.0)
                val total = j3 + jA + jB
                logger.info("3학년+2-2학기 - J_A: $jA, J_B(환산): $jB, 총점: $total")
                total
            }
            // 3학년 + 2-1만 있는 경우: J_A = (J₃ + J_B) × 1/3
            grade22 == null && grade21 != null -> {
                val avg21 = calculateSemesterScore(grade21)
                val jB = avg21 * 4.0
                val jA = (j3 + jB) * (1.0 / 3.0)
                val total = j3 + jA + jB
                logger.info("3학년+2-1학기 - J_A(환산): $jA, J_B: $jB, 총점: $total")
                total
            }
            // 3학년만 있는 경우: J_A = J_B = J₃ / 2
            else -> {
                val jA = j3 / 2.0
                val jB = j3 / 2.0
                val total = j3 + jA + jB
                logger.info("3학년만 - J_A(환산): $jA, J_B(환산): $jB, 총점: $total")
                total
            }
        }
    }

    /**
     * 졸업자 교과성적 계산 (80점 만점)
     *
     * 공식: J = J₃ + J_A + J_B
     * - J₃ (3학년): 4 × (S₃₂ / N₃₂) + 4 × (S₃₁ / N₃₁) [50%, 40점]
     * - J_A (직전학기, 2-2학기): 4 × (S_A / N_A) [25%, 20점]
     * - J_B (직전전학기, 2-1학기): 4 × (S_B / N_B) [25%, 20점]
     *
     * 성적 부족 시 환산:
     * - 3학년만 있는 경우: J_A = J_B = J₃ / 2
     * - 3학년 + 직전학기만: J_B = (J₃ + J_A) × 1/3
     * - 3학년 + 직전전학기만: J_A = (J₃ + J_B) × 1/3
     */
    private fun calculateGraduateSubjectScore(scoreInput: ScoreInput): Double {
        val grade32 =
            scoreInput.grade3_2
                ?: throw ScoreCalculationException("졸업자는 3학년 2학기 성적이 필수입니다")
        val avg32 = calculateSemesterScore(grade32)

        val grade31 =
            scoreInput.grade3_1
                ?: throw ScoreCalculationException("졸업자는 3학년 1학기 성적이 필수입니다")
        val avg31 = calculateSemesterScore(grade31)

        // J₃ = 4 × (S₃₂ / N₃₂) + 4 × (S₃₁ / N₃₁)
        val j3 = avg32 * 4.0 + avg31 * 4.0

        logger.info("졸업자 계산 - J₃: $j3 (3-2 평균: $avg32, 3-1 평균: $avg31)")

        val grade22 = scoreInput.grade2_2
        val grade21 = scoreInput.grade2_1

        return when {
            // 모든 학기 성적이 있는 경우: J = J₃ + J_A + J_B
            grade22 != null && grade21 != null -> {
                val avg22 = calculateSemesterScore(grade22)
                val avg21 = calculateSemesterScore(grade21)
                val jA = avg22 * 4.0
                val jB = avg21 * 4.0
                val total = j3 + jA + jB
                logger.info("모든 학기 - J_A: $jA, J_B: $jB, 총점: $total")
                total
            }
            // 3학년 + 2-2만 있는 경우: J_B = (J₃ + J_A) × 1/3
            grade22 != null && grade21 == null -> {
                val avg22 = calculateSemesterScore(grade22)
                val jA = avg22 * 4.0
                val jB = (j3 + jA) * (1.0 / 3.0)
                val total = j3 + jA + jB
                logger.info("3학년+2-2학기 - J_A: $jA, J_B(환산): $jB, 총점: $total")
                total
            }
            // 3학년 + 2-1만 있는 경우: J_A = (J₃ + J_B) × 1/3
            grade22 == null && grade21 != null -> {
                val avg21 = calculateSemesterScore(grade21)
                val jB = avg21 * 4.0
                val jA = (j3 + jB) * (1.0 / 3.0)
                val total = j3 + jA + jB
                logger.info("3학년+2-1학기 - J_A(환산): $jA, J_B: $jB, 총점: $total")
                total
            }
            // 3학년만 있는 경우: J_A = J_B = J₃ / 2
            else -> {
                val jA = j3 / 2.0
                val jB = j3 / 2.0
                val total = j3 + jA + jB
                logger.info("3학년만 - J_A(환산): $jA, J_B(환산): $jB, 총점: $total")
                total
            }
        }
    }

    /**
     * 검정고시 교과성적 계산
     *
     * 환산점 기준:
     * - 100~98점: 5점
     * - 98~94점: 4점
     * - 94~90점: 3점
     * - 90~86점: 2점
     * - 86점 미만: 1점
     *
     * 계산 방법:
     * - 일반전형: (T/N) × 34 (최대 170점)
     * - 특별전형: (T/N) × 22 (최대 110점)
     *   * T: 취득한 과목별 환산점의 총합
     *   * N: 취득한 과목수
     */
    private fun calculateQualificationExamSubjectScore(
        applicationType: ApplicationType,
        scoreInput: ScoreInput
    ): Double {
        val gedScores = scoreInput.gedScores

        logger.info("=== 검정고시 교과 점수 계산 (환산점 방식) ===")
        logger.info("원점수: $gedScores")

        // 1. 각 과목을 환산점으로 변환
        val convertedScores = gedScores?.mapValues { (subject, score) ->
            val converted = convertGedScoreToPoint(score)
            logger.info("  $subject: ${score}점 -> 환산점 ${converted}점")
            converted
        }

        // 2. 환산점 합계 및 평균 계산
        val totalPoints = convertedScores?.values?.sum()!!
        val subjectCount = convertedScores.size
        val averagePoints = totalPoints.toDouble() / subjectCount

        logger.info("환산점 합계(T): $totalPoints, 과목수(N): $subjectCount, 평균: $averagePoints")

        // 3. 전형별 배수 적용
        return if (applicationType == ApplicationType.COMMON) {
            // 일반전형: (T/N) × 34
            val finalScore = averagePoints * 34.0

            logger.info("전형: COMMON, 수식: ($totalPoints/$subjectCount) × 34, 교과점수: $finalScore")

            finalScore
        } else {
            // 특별전형: (T/N) × 22
            val finalScore = averagePoints * 22.0

            logger.info("전형: SPECIAL, 수식: ($totalPoints/$subjectCount) × 22, 교과점수: $finalScore")

            finalScore
        }
    }

    /**
     * 검정고시 100점 점수를 1-5점 환산점으로 변환
     */
    private fun convertGedScoreToPoint(score: Int?): Int {
        return score?.let {
            when {
                it >= 98 -> 5
                it >= 94 -> 4
                it >= 90 -> 3
                it >= 86 -> 2
                else -> 1
            }
        } ?: 1
    }

    /**
     * 학기별 7과목 평균 성적 계산 (5점 만점 기준)
     */
    private fun calculateSemesterScore(grades: SemesterGrades): Double {
        logger.info("  calculateSemesterScore 입력: $grades")

        val gradeList =
            listOf(
                grades.korean,
                grades.social,
                grades.history,
                grades.math,
                grades.science,
                grades.tech,
                grades.english,
            )

        logger.info("  gradeList: $gradeList")

        // 0이 아닌 성적만 필터링 (0은 성적이 없는 것으로 간주)
        val validGrades = gradeList.filter { it > 0 }

        logger.info("  validGrades (0 제외): $validGrades")

        // 유효한 성적이 없으면 0 반환
        if (validGrades.isEmpty()) {
            logger.warn("  유효한 성적이 없습니다. 0.0 반환")
            return 0.0
        }

        // 1~5 범위 검증
        validGrades.forEach { grade ->
            if (grade !in 1..5) {
                throw ScoreCalculationException("성적은 1~5 사이여야 합니다: $grade")
            }
        }

        val average = validGrades.average()
        logger.info("  평균 점수: $average")

        return average
    }

    /**
     * 출석점수 계산 (15점 만점)
     *
     * 환산결석(소수이하 버림) = 결석일수 + (지각횟수 + 조퇴횟수 + 결과횟수) / 3
     *
     * 환산표:
     * - 0일: 15점, 1일: 14점, 2일: 13점, 3일: 12점
     * - 4일: 11점, 5일: 10점, 6일: 9점, 7일: 8점
     * - 8일: 7점, 9일: 6점, 10일: 5점, 11일: 4점
     * - 12일: 3점, 13일: 2점, 14일: 1점, 15일 이상: 0점
     */
    private fun calculateAttendanceScore(scoreInput: ScoreInput): Double {
        val attendance = scoreInput.attendance ?: AttendanceInfo()

        // 환산결석 = 결석 + (지각 + 조퇴 + 결과) / 3 (소수점 버림)
        val convertedAbsence = (
            attendance.absence +
                (attendance.tardiness + attendance.earlyLeave + attendance.classExit) / 3.0
        ).toInt() // 소수점 이하 버림

        logger.info("출석 계산 - 결석: ${attendance.absence}, 지각: ${attendance.tardiness}, " +
            "조퇴: ${attendance.earlyLeave}, 결과: ${attendance.classExit}, 환산결석: $convertedAbsence")

        return when (convertedAbsence) {
            0 -> 15.0
            1 -> 14.0
            2 -> 13.0
            3 -> 12.0
            4 -> 11.0
            5 -> 10.0
            6 -> 9.0
            7 -> 8.0
            8 -> 7.0
            9 -> 6.0
            10 -> 5.0
            11 -> 4.0
            12 -> 3.0
            13 -> 2.0
            14 -> 1.0
            else -> 0.0 // 15일 이상
        }
    }

    /**
     * 봉사활동점수 계산 (15점 만점)
     *
     * 환산표:
     * - 15시간 이상: 15점
     * - 14시간 ~ 0시간: (총 봉사시간)점
     *
     * 예: 10시간 → 10점, 15시간 → 15점, 20시간 → 15점
     */
    private fun calculateVolunteerScore(scoreInput: ScoreInput): Double {
        val volunteer = scoreInput.volunteerHours ?: 0
        val score = min(volunteer.toDouble(), 15.0)

        logger.info("봉사 계산 - 총 시간: ${volunteer}시간, 점수: $score")

        return score
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
            private val logger = LoggerFactory.getLogger(ScoreInput::class.java)

            fun from(scores: Map<String, Any>): ScoreInput {
                logger.info("ScoreInput.from() 시작")
                logger.info("입력 Map keys: ${scores.keys}")

                val grade3_2 = extractSemesterGrades(scores, "3_2")
                val grade3_1 = extractSemesterGrades(scores, "3_1")
                val grade2_2 = extractSemesterGrades(scores, "2_2")
                val grade2_1 = extractSemesterGrades(scores, "2_1")
                val gedScores = extractGedScores(scores)
                val attendance = extractAttendance(scores)
                val volunteerHours = getIntOrNull(scores, "volunteer")
                val algorithmAward = getBooleanOrNull(scores, "algorithmAward")
                val infoProcessingCert = getBooleanOrNull(scores, "infoProcessingCert")

                logger.info("추출된 학기 성적 - 3_2: $grade3_2, 3_1: $grade3_1, 2_2: $grade2_2, 2_1: $grade2_1")
                logger.info("검정고시 성적: $gedScores")
                logger.info("출결: $attendance, 봉사: $volunteerHours, 알고리즘: $algorithmAward, 정보처리: $infoProcessingCert")

                return ScoreInput(
                    grade3_2 = grade3_2,
                    grade3_1 = grade3_1,
                    grade2_2 = grade2_2,
                    grade2_1 = grade2_1,
                    gedScores = gedScores,
                    attendance = attendance,
                    volunteerHours = volunteerHours,
                    algorithmAward = algorithmAward,
                    infoProcessingCert = infoProcessingCert,
                )
            }

            private fun extractSemesterGrades(
                scores: Map<String, Any>,
                semester: String,
            ): SemesterGrades? {
                logger.info("  extractSemesterGrades for semester $semester")

                val korean = getIntOrNull(scores, "korean_$semester") ?: 0
                val social = getIntOrNull(scores, "social_$semester") ?: 0
                val history = getIntOrNull(scores, "history_$semester") ?: 0
                val math = getIntOrNull(scores, "math_$semester") ?: 0
                val science = getIntOrNull(scores, "science_$semester") ?: 0
                val tech = getIntOrNull(scores, "tech_$semester") ?: 0
                val english = getIntOrNull(scores, "english_$semester") ?: 0

                logger.info("    추출된 값 - 국어: $korean, 사회: $social, 역사: $history, 수학: $math, 과학: $science, 기술: $tech, 영어: $english")

                // 적어도 하나의 과목이라도 있으면 해당 학기 성적으로 인정
                // 없는 과목은 0으로 처리되며, calculateSemesterScore에서 0은 제외됨
                return if (korean > 0 || social > 0 || history > 0 ||
                    math > 0 || science > 0 || tech > 0 || english > 0
                ) {
                    val result = SemesterGrades(korean, social, history, math, science, tech, english)
                    logger.info("    -> SemesterGrades 생성: $result")
                    result
                } else {
                    logger.info("    -> 모든 성적이 0이므로 null 반환")
                    null
                }
            }

            private fun extractGedScores(scores: Map<String, Any>): Map<String, Int>? {
                val gedScores = mutableMapOf<String, Int>()

                // qualification* 키와 ged* 키 모두 지원
                listOf("Korean", "Social", "History", "Math", "Science", "English", "Tech").forEach { subject ->
                    val qualificationKey = "qualification$subject"
                    val gedKey = "ged$subject"

                    // qualification* 키 먼저 확인, 없으면 ged* 키 확인
                    val score = getIntOrNull(scores, qualificationKey) ?: getIntOrNull(scores, gedKey)
                    score?.let { gedScores[subject] = it }
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

            private fun getIntOrNull(
                data: Map<String, Any>,
                key: String,
            ): Int? {
                return when (val value = data[key]) {
                    is Int -> value
                    is Number -> value.toInt()
                    is String -> value.toIntOrNull()
                    else -> null
                }
            }

            private fun getBooleanOrNull(
                data: Map<String, Any>,
                key: String,
            ): Boolean? {
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
        val english: Int,
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
