package hs.kr.entrydsm.domain.application.services

import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.min

/**
 * 대덕소프트웨어마이스터고 입학전형 점수 계산 서비스
 * 
 * 2026학년도 입학전형 기준에 따라 교과성적, 출석점수, 봉사활동점수, 가산점을 계산합니다.
 */
class ScoreCalculationService {

    /**
     * 전형별 총점을 계산합니다.
     */
    fun calculateTotalScore(application: Application): BigDecimal {
        val subjectScore = calculateSubjectScore(application)
        val attendanceScore = calculateAttendanceScore(application)
        val volunteerScore = calculateVolunteerScore(application)
        val bonusScore = calculateBonusScore(application)

        return subjectScore.add(attendanceScore).add(volunteerScore).add(bonusScore)
    }

    /**
     * 교과 성적을 계산합니다 (전형별로 다름)
     */
    fun calculateSubjectScore(application: Application): BigDecimal {
        return when (application.educationalStatus) {
            EducationalStatus.GRADUATE -> calculateGraduatedSubjectScore(application)
            EducationalStatus.PROSPECTIVE_GRADUATE -> calculateProspectiveGraduatedSubjectScore(application)
            EducationalStatus.QUALIFICATION_EXAM -> calculateGedSubjectScore(application)
        }
    }

    /**
     * 졸업자 교과 성적 계산 (80점 만점)
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
    private fun calculateGraduatedSubjectScore(application: Application): BigDecimal {
        val semester3_2Avg = calculateSemesterAverage(
            application.korean_3_2, application.social_3_2, application.history_3_2,
            application.math_3_2, application.science_3_2, application.tech_3_2, application.english_3_2
        )
        val semester3_1Avg = calculateSemesterAverage(
            application.korean_3_1, application.social_3_1, application.history_3_1,
            application.math_3_1, application.science_3_1, application.tech_3_1, application.english_3_1
        )
        
        // J₃ = 4 × (S₃₂ / N₃₂) + 4 × (S₃₁ / N₃₁)
        val j3 = semester3_2Avg.multiply(BigDecimal("4.0")).add(semester3_1Avg.multiply(BigDecimal("4.0")))
        
        val semester2_2Avg = calculateSemesterAverage(
            application.korean_2_2, application.social_2_2, application.history_2_2,
            application.math_2_2, application.science_2_2, application.tech_2_2, application.english_2_2
        )
        val semester2_1Avg = calculateSemesterAverage(
            application.korean_2_1, application.social_2_1, application.history_2_1,
            application.math_2_1, application.science_2_1, application.tech_2_1, application.english_2_1
        )
        
        val baseScore = when {
            // 모든 학기 성적이 있는 경우: J = J₃ + J_A + J_B
            semester2_2Avg > BigDecimal.ZERO && semester2_1Avg > BigDecimal.ZERO -> {
                val jA = semester2_2Avg.multiply(BigDecimal("4.0"))
                val jB = semester2_1Avg.multiply(BigDecimal("4.0"))
                j3.add(jA).add(jB)
            }
            // 3학년 + 2-2만 있는 경우: J_B = (J₃ + J_A) × 1/3
            semester2_2Avg > BigDecimal.ZERO && semester2_1Avg == BigDecimal.ZERO -> {
                val jA = semester2_2Avg.multiply(BigDecimal("4.0"))
                val jB = j3.add(jA).multiply(BigDecimal("0.333333"))
                j3.add(jA).add(jB)
            }
            // 3학년 + 2-1만 있는 경우: J_A = (J₃ + J_B) × 1/3
            semester2_2Avg == BigDecimal.ZERO && semester2_1Avg > BigDecimal.ZERO -> {
                val jB = semester2_1Avg.multiply(BigDecimal("4.0"))
                val jA = j3.add(jB).multiply(BigDecimal("0.333333"))
                j3.add(jA).add(jB)
            }
            // 3학년만 있는 경우: J_A = J_B = J₃ / 2
            else -> {
                val jA = j3.divide(BigDecimal("2.0"), 4, RoundingMode.HALF_UP)
                val jB = j3.divide(BigDecimal("2.0"), 4, RoundingMode.HALF_UP)
                j3.add(jA).add(jB)
            }
        }
        
        // 전형별 배수 적용
        return when (application.applicationType) {
            ApplicationType.COMMON -> baseScore.multiply(BigDecimal("1.75")).setScale(2, RoundingMode.HALF_UP)
            ApplicationType.MEISTER, ApplicationType.SOCIAL -> baseScore.setScale(2, RoundingMode.HALF_UP)
        }
    }

    /**
     * 졸업예정자 교과 성적 계산 (80점 만점)
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
    private fun calculateProspectiveGraduatedSubjectScore(application: Application): BigDecimal {
        val semester3_1Avg = calculateSemesterAverage(
            application.korean_3_1, application.social_3_1, application.history_3_1,
            application.math_3_1, application.science_3_1, application.tech_3_1, application.english_3_1
        )
        
        // J₃ = 8 × (S₃₁ / N₃₁)
        val j3 = semester3_1Avg.multiply(BigDecimal("8.0"))
        
        val semester2_2Avg = calculateSemesterAverage(
            application.korean_2_2, application.social_2_2, application.history_2_2,
            application.math_2_2, application.science_2_2, application.tech_2_2, application.english_2_2
        )
        val semester2_1Avg = calculateSemesterAverage(
            application.korean_2_1, application.social_2_1, application.history_2_1,
            application.math_2_1, application.science_2_1, application.tech_2_1, application.english_2_1
        )
        
        val baseScore = when {
            // 모든 학기 성적이 있는 경우: J = J₃ + J_A + J_B
            semester2_2Avg > BigDecimal.ZERO && semester2_1Avg > BigDecimal.ZERO -> {
                val jA = semester2_2Avg.multiply(BigDecimal("4.0"))
                val jB = semester2_1Avg.multiply(BigDecimal("4.0"))
                j3.add(jA).add(jB)
            }
            // 3학년 + 2-2만 있는 경우: J_B = (J₃ + J_A) × 1/3
            semester2_2Avg > BigDecimal.ZERO && semester2_1Avg == BigDecimal.ZERO -> {
                val jA = semester2_2Avg.multiply(BigDecimal("4.0"))
                val jB = j3.add(jA).multiply(BigDecimal("0.333333"))
                j3.add(jA).add(jB)
            }
            // 3학년 + 2-1만 있는 경우: J_A = (J₃ + J_B) × 1/3
            semester2_2Avg == BigDecimal.ZERO && semester2_1Avg > BigDecimal.ZERO -> {
                val jB = semester2_1Avg.multiply(BigDecimal("4.0"))
                val jA = j3.add(jB).multiply(BigDecimal("0.333333"))
                j3.add(jA).add(jB)
            }
            // 3학년만 있는 경우: J_A = J_B = J₃ / 2
            else -> {
                val jA = j3.divide(BigDecimal("2.0"), 4, RoundingMode.HALF_UP)
                val jB = j3.divide(BigDecimal("2.0"), 4, RoundingMode.HALF_UP)
                j3.add(jA).add(jB)
            }
        }
        
        // 전형별 배수 적용
        return when (application.applicationType) {
            ApplicationType.COMMON -> baseScore.multiply(BigDecimal("1.75")).setScale(2, RoundingMode.HALF_UP)
            ApplicationType.MEISTER, ApplicationType.SOCIAL -> baseScore.setScale(2, RoundingMode.HALF_UP)
        }
    }

    /**
     * 검정고시 교과 성적 계산
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
    private fun calculateGedSubjectScore(application: Application): BigDecimal {
        val scores = listOfNotNull(
            application.gedKorean,
            application.gedSocial,
            application.gedHistory,
            application.gedMath,
            application.gedScience,
            application.gedTech,
            application.gedEnglish
        )
        
        if (scores.isEmpty()) return BigDecimal.ZERO
        
        // 1. 각 과목을 환산점으로 변환
        val convertedScores = scores.map { convertGedScoreToPoint(it) }
        
        // 2. 환산점 합계 및 평균 계산
        val totalPoints = convertedScores.sum()
        val subjectCount = convertedScores.size
        val averagePoints = totalPoints.toDouble() / subjectCount
        
        // 3. 전형별 배수 적용
        return when (application.applicationType) {
            ApplicationType.COMMON -> {
                // 일반전형: (T/N) × 34
                BigDecimal.valueOf(averagePoints * 34.0)
                    .setScale(2, RoundingMode.HALF_UP)
            }
            ApplicationType.MEISTER, ApplicationType.SOCIAL -> {
                // 특별전형: (T/N) × 22
                BigDecimal.valueOf(averagePoints * 22.0)
                    .setScale(2, RoundingMode.HALF_UP)
            }
        }
    }

    /**
     * 검정고시 100점 점수를 환산점(1-5)으로 변환
     */
    private fun convertGedScoreToPoint(score: Int): Int {
        return when {
            score >= 98 -> 5
            score >= 94 -> 4
            score >= 90 -> 3
            score >= 86 -> 2
            else -> 1
        }
    }

    /**
     * 출석 점수 계산 (15점 만점)
     *
     * 환산결석(소수이하 버림) = 결석일수 + (지각횟수 + 조퇴횟수 + 결과횟수) / 3
     *
     * 환산표:
     * - 0일: 15점, 1일: 14점, 2일: 13점, 3일: 12점
     * - 4일: 11점, 5일: 10점, 6일: 9점, 7일: 8점
     * - 8일: 7점, 9일: 6점, 10일: 5점, 11일: 4점
     * - 12일: 3점, 13일: 2점, 14일: 1점, 15일 이상: 0점
     */
    fun calculateAttendanceScore(application: Application): BigDecimal {
        // 검정고시는 출석점수 없음
        if (application.educationalStatus == EducationalStatus.QUALIFICATION_EXAM) {
            return BigDecimal.ZERO
        }
        
        val absence = application.absence ?: 0
        val tardiness = application.tardiness ?: 0
        val earlyLeave = application.earlyLeave ?: 0
        val classExit = application.classExit ?: 0
        
        // 환산결석 = 결석 + (지각 + 조퇴 + 결과) / 3 (소수점 이하 버림)
        val convertedAbsence = (absence + (tardiness + earlyLeave + classExit) / 3.0).toInt()
        
        val score = when (convertedAbsence) {
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
        
        return BigDecimal.valueOf(score)
            .setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * 봉사활동 점수 계산 (15점 만점)
     *
     * 환산표:
     * - 15시간 이상: 15점
     * - 14시간 ~ 0시간: (총 봉사시간)점
     */
    fun calculateVolunteerScore(application: Application): BigDecimal {
        // 검정고시는 봉사점수 없음
        if (application.educationalStatus == EducationalStatus.QUALIFICATION_EXAM) {
            return BigDecimal.ZERO
        }
        
        // 일반 학생: 15시간 이상 15점, 그 이하는 시간 = 점수
        val volunteer = application.volunteer ?: 0
        return BigDecimal.valueOf(min(volunteer.toDouble(), 15.0))
            .setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * 가산점 계산
     * - 알고리즘 경진대회 수상: 3점 (모든 전형)
     * - 정보처리기능사: 6점 (특별전형만)
     */
    fun calculateBonusScore(application: Application): BigDecimal {
        var bonusScore = 0.0
        
        // 알고리즘 경진대회 수상 (모든 전형 3점)
        if (application.algorithmAward == true) {
            bonusScore += 3.0
        }
        
        // 정보처리기능사 (특별전형만 6점)
        if (application.applicationType != ApplicationType.COMMON) {
            if (application.infoProcessingCert == true) {
                bonusScore += 6.0
            }
        }
        
        return BigDecimal.valueOf(bonusScore)
            .setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * 전형별 최대 점수 반환
     */
    fun getMaxScore(applicationType: ApplicationType): BigDecimal {
        return when (applicationType) {
            ApplicationType.COMMON -> BigDecimal("173") // 140 + 15 + 15 + 3
            ApplicationType.MEISTER, ApplicationType.SOCIAL -> BigDecimal("119") // 80 + 15 + 15 + 9
        }
    }

    /**
     * 점수 상세 정보 반환
     */
    fun getScoreDetails(application: Application): Map<String, BigDecimal> {
        return mapOf(
            "교과성적" to calculateSubjectScore(application),
            "출석점수" to calculateAttendanceScore(application),
            "봉사활동점수" to calculateVolunteerScore(application),
            "가산점" to calculateBonusScore(application),
            "총점" to calculateTotalScore(application),
            "최대점수" to getMaxScore(application.applicationType)
        )
    }
    
    /**
     * 학기별 점수 계산
     */
    fun calculateSemesterScores(application: Application): Map<String, BigDecimal> {
        return when (application.educationalStatus) {
            EducationalStatus.GRADUATE -> {
                val semester3_2Avg = calculateSemesterAverage(
                    application.korean_3_2, application.social_3_2, application.history_3_2,
                    application.math_3_2, application.science_3_2, application.tech_3_2, application.english_3_2
                )
                val semester3_1Avg = calculateSemesterAverage(
                    application.korean_3_1, application.social_3_1, application.history_3_1,
                    application.math_3_1, application.science_3_1, application.tech_3_1, application.english_3_1
                )
                val semester2_2Avg = calculateSemesterAverage(
                    application.korean_2_2, application.social_2_2, application.history_2_2,
                    application.math_2_2, application.science_2_2, application.tech_2_2, application.english_2_2
                )
                val semester2_1Avg = calculateSemesterAverage(
                    application.korean_2_1, application.social_2_1, application.history_2_1,
                    application.math_2_1, application.science_2_1, application.tech_2_1, application.english_2_1
                )
                
                mapOf(
                    "3-2" to BigDecimal.valueOf(4.0).multiply(semester3_2Avg).setScale(2, RoundingMode.HALF_UP),
                    "3-1" to BigDecimal.valueOf(4.0).multiply(semester3_1Avg).setScale(2, RoundingMode.HALF_UP),
                    "2-2" to BigDecimal.valueOf(4.0).multiply(semester2_2Avg).setScale(2, RoundingMode.HALF_UP),
                    "2-1" to BigDecimal.valueOf(4.0).multiply(semester2_1Avg).setScale(2, RoundingMode.HALF_UP)
                )
            }
            EducationalStatus.PROSPECTIVE_GRADUATE -> {
                val semester3_1Avg = calculateSemesterAverage(
                    application.korean_3_1, application.social_3_1, application.history_3_1,
                    application.math_3_1, application.science_3_1, application.tech_3_1, application.english_3_1
                )
                val semester2_2Avg = calculateSemesterAverage(
                    application.korean_2_2, application.social_2_2, application.history_2_2,
                    application.math_2_2, application.science_2_2, application.tech_2_2, application.english_2_2
                )
                val semester2_1Avg = calculateSemesterAverage(
                    application.korean_2_1, application.social_2_1, application.history_2_1,
                    application.math_2_1, application.science_2_1, application.tech_2_1, application.english_2_1
                )
                
                mapOf(
                    "3-2" to BigDecimal.ZERO,
                    "3-1" to BigDecimal.valueOf(8.0).multiply(semester3_1Avg).setScale(2, RoundingMode.HALF_UP),
                    "2-2" to BigDecimal.valueOf(4.0).multiply(semester2_2Avg).setScale(2, RoundingMode.HALF_UP),
                    "2-1" to BigDecimal.valueOf(4.0).multiply(semester2_1Avg).setScale(2, RoundingMode.HALF_UP)
                )
            }
            EducationalStatus.QUALIFICATION_EXAM -> {
                // 검정고시 교과 점수 (이미 전형별 배수 적용됨)
                val totalScore = calculateGedSubjectScore(application)
                
                mapOf(
                    "3-2" to BigDecimal.ZERO,
                    "3-1" to totalScore, // 3-1에 표시
                    "2-2" to BigDecimal.ZERO,
                    "2-1" to BigDecimal.ZERO
                )
            }
        }
    }
    
    /**
     * 학기 평균 계산 (성취도 1~5 기준)
     */
    private fun calculateSemesterAverage(
        korean: Int?, social: Int?, history: Int?,
        math: Int?, science: Int?, tech: Int?, english: Int?
    ): BigDecimal {
        // 0보다 큰 유효한 성적만 필터링
        val scores = listOfNotNull(korean, social, history, math, science, tech, english)
            .filter { it > 0 }
        
        if (scores.isEmpty()) return BigDecimal.ZERO
        
        val sum = scores.sum()
        return BigDecimal.valueOf(sum.toDouble() / scores.size)
            .setScale(4, RoundingMode.HALF_UP)
    }
}
