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
     * 소수점 4자리에서 반올림하여 소수점 3자리까지 표시
     */
    fun calculateTotalScore(application: Application): BigDecimal {
        val subjectScore = calculateSubjectScore(application)
        val attendanceScore = calculateAttendanceScore(application)
        val volunteerScore = calculateVolunteerScore(application)
        val bonusScore = calculateBonusScore(application)

        return subjectScore.add(attendanceScore).add(volunteerScore).add(bonusScore)
            .setScale(3, RoundingMode.HALF_UP)
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
     * 졸업자 교과 성적 계산 (최근 4개 학기)
     * - 3학년 2학기: 25% (20점)
     * - 3학년 1학기: 25% (20점)
     * - 2학년 2학기: 25% (20점)
     * - 2학년 1학기: 25% (20점)
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

        val semester2_2Avg = calculateSemesterAverage(
            application.korean_2_2, application.social_2_2, application.history_2_2,
            application.math_2_2, application.science_2_2, application.tech_2_2, application.english_2_2
        )
        val semester2_1Avg = calculateSemesterAverage(
            application.korean_2_1, application.social_2_1, application.history_2_1,
            application.math_2_1, application.science_2_1, application.tech_2_1, application.english_2_1
        )

        val semester3_2Score = BigDecimal.valueOf(4.0).multiply(semester3_2Avg)
        val semester3_1Score = BigDecimal.valueOf(4.0).multiply(semester3_1Avg)
        val semester2_2Score = BigDecimal.valueOf(4.0).multiply(semester2_2Avg)
        val semester2_1Score = BigDecimal.valueOf(4.0).multiply(semester2_1Avg)

        val baseScore = semester3_2Score.add(semester3_1Score).add(semester2_2Score).add(semester2_1Score)
        
        // 전형별 배수 적용
        return when (application.applicationType) {
            ApplicationType.COMMON -> baseScore.multiply(BigDecimal("1.75")) // 140점 만점
            ApplicationType.MEISTER, ApplicationType.SOCIAL -> baseScore // 80점 만점
        }
    }

    /**
     * 졸업예정자 교과 성적 계산 (최근 3개 학기)
     * - 3학년 1학기: 50% (40점)
     * - 2학년 2학기: 25% (20점)
     * - 2학년 1학기: 25% (20점)
     */
    private fun calculateProspectiveGraduatedSubjectScore(application: Application): BigDecimal {
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

        val semester3_1Score = BigDecimal.valueOf(8.0).multiply(semester3_1Avg)
        val semester2_2Score = BigDecimal.valueOf(4.0).multiply(semester2_2Avg)
        val semester2_1Score = BigDecimal.valueOf(4.0).multiply(semester2_1Avg)

        val baseScore = semester3_1Score.add(semester2_2Score).add(semester2_1Score)
        
        // 전형별 배수 적용
        return when (application.applicationType) {
            ApplicationType.COMMON -> baseScore.multiply(BigDecimal("1.75")) // 140점 만점
            ApplicationType.MEISTER, ApplicationType.SOCIAL -> baseScore // 80점 만점
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
        

        val totalPoints = convertedScores.sum()
        val subjectCount = convertedScores.size
        val averagePoints = BigDecimal.valueOf(totalPoints.toDouble() / subjectCount)
            .setScale(3, RoundingMode.HALF_UP)
        
        // 3. 전형별 배수 적용
        return when (application.applicationType) {
            ApplicationType.COMMON -> {
                // 일반전형: (T/N) × 34
                averagePoints.multiply(BigDecimal.valueOf(34.0))
                    .setScale(3, RoundingMode.HALF_UP)
            }
            ApplicationType.MEISTER, ApplicationType.SOCIAL -> {
                // 특별전형: (T/N) × 22
                averagePoints.multiply(BigDecimal.valueOf(22.0))
                    .setScale(3, RoundingMode.HALF_UP)
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
     * 환산 결석 = 결석 + (지각+조퇴+결과)/3
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
        val convertedAbsence = absence + (tardiness + earlyLeave + classExit) / 3.0

        val score = when {
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
