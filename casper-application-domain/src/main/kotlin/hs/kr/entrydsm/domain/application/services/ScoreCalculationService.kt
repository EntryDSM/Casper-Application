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
     * - 일반전형: (평균 - 50) / 50 * 140 (최대 140점)
     * - 특별전형: (평균 - 50) / 50 * 80 (최대 80점)
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
        
        val average = scores.sum().toDouble() / scores.size
        
        return when (application.applicationType) {
            ApplicationType.COMMON -> {
                // 일반전형: (평균 - 50) / 50 * 140
                BigDecimal.valueOf(((average - 50.0) / 50.0) * 140.0)
                    .setScale(2, RoundingMode.HALF_UP)
            }
            ApplicationType.MEISTER, ApplicationType.SOCIAL -> {
                // 특별전형: (평균 - 50) / 50 * 80
                BigDecimal.valueOf(((average - 50.0) / 50.0) * 80.0)
                    .setScale(2, RoundingMode.HALF_UP)
            }
        }
    }

    /**
     * 출석 점수 계산 (15점 만점)
     * 환산 결석 = 결석 + (지각+조퇴+결과)/3
     */
    fun calculateAttendanceScore(application: Application): BigDecimal {
        // 검정고시는 출석점수 만점
        if (application.educationalStatus == EducationalStatus.QUALIFICATION_EXAM) {
            return BigDecimal("15.0")
        }
        
        val absence = application.absence ?: 0
        val tardiness = application.tardiness ?: 0
        val earlyLeave = application.earlyLeave ?: 0
        val classExit = application.classExit ?: 0
        
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
     */
    fun calculateVolunteerScore(application: Application): BigDecimal {
        // 검정고시는 별도 계산
        if (application.educationalStatus == EducationalStatus.QUALIFICATION_EXAM) {
            return calculateGedVolunteerScore(application)
        }
        
        // 일반 학생: 15시간 이상 15점, 그 이하는 시간 = 점수
        val volunteer = application.volunteer ?: 0
        return BigDecimal.valueOf(min(volunteer.toDouble(), 15.0))
            .setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * 검정고시 봉사활동 점수 계산
     * - 모든 전형: (평균 - 40) / 60 * 15 (최대 15점)
     */
    private fun calculateGedVolunteerScore(application: Application): BigDecimal {
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
        
        val average = scores.sum().toDouble() / scores.size
        val volunteerScore = ((average - 40.0) / 60.0) * 15.0
        
        return BigDecimal.valueOf(volunteerScore.coerceIn(0.0, 15.0))
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