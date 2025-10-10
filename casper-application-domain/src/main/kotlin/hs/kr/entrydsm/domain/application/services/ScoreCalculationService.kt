package hs.kr.entrydsm.domain.application.services

import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import java.math.BigDecimal
import java.math.RoundingMode

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
        val baseScore = when (application.educationalStatus) {
            EducationalStatus.GRADUATE -> calculateGraduatedSubjectScore(application)
            EducationalStatus.PROSPECTIVE_GRADUATE -> calculateProspectiveGraduatedSubjectScore(application)
            EducationalStatus.QUALIFICATION_EXAM -> calculateGedSubjectScore(application)
        }

        // 전형별 환산 비율 적용
        return when (application.applicationType) {
            ApplicationType.COMMON -> baseScore.multiply(BigDecimal("1.75")) // 140점 (80점 × 175%)
            ApplicationType.MEISTER, ApplicationType.SOCIAL -> baseScore // 80점 (80점 × 100%)
        }
    }

    /**
     * 졸업자 교과 성적 계산 (최근 4개 학기)
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
        
        return semester3_2Score.add(semester3_1Score).add(semester2_2Score).add(semester2_1Score)
            .setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * 졸업예정자 교과 성적 계산 (최근 3개 학기)
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
        
        return semester3_1Score.add(semester2_2Score).add(semester2_1Score)
            .setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * 검정고시 교과 성적 계산
     */
    private fun calculateGedSubjectScore(application: Application): BigDecimal {
        val average = calculateSemesterAverage(
            application.gedKorean, application.gedSocial, application.gedHistory,
            application.gedMath, application.gedScience, application.gedTech, application.gedEnglish
        )
        
        return BigDecimal.valueOf(16.0).multiply(average)
            .setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * 출석 점수 계산 (15점 만점)
     */
    fun calculateAttendanceScore(application: Application): BigDecimal {
        val absence = application.absence ?: 0
        val tardiness = application.tardiness ?: 0
        val earlyLeave = application.earlyLeave ?: 0
        val classExit = application.classExit ?: 0
        
        val convertedAbsence = absence + (tardiness / 3.0) + (earlyLeave / 3.0) + (classExit / 3.0)
        val score = 15.0 - convertedAbsence
        
        return BigDecimal.valueOf(score.coerceIn(0.0, 15.0))
            .setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * 봉사활동 점수 계산 (15점 만점)
     */
    fun calculateVolunteerScore(application: Application): BigDecimal {
        val volunteer = application.volunteer ?: 0
        return BigDecimal.valueOf(volunteer.toDouble().coerceIn(0.0, 15.0))
            .setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * 가산점 계산
     */
    fun calculateBonusScore(application: Application): BigDecimal {
        val algorithmScore = if (application.algorithmAward == true) 3.0 else 0.0
        val certScore = if (application.infoProcessingCert == true) {
            when (application.applicationType) {
                ApplicationType.COMMON -> 0.0
                else -> 6.0
            }
        } else {
            0.0
        }
        
        return BigDecimal.valueOf(algorithmScore + certScore)
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
                val average = calculateSemesterAverage(
                    application.gedKorean, application.gedSocial, application.gedHistory,
                    application.gedMath, application.gedScience, application.gedTech, application.gedEnglish
                )
                val totalScore = BigDecimal.valueOf(16.0).multiply(average).setScale(2, RoundingMode.HALF_UP)
                
                mapOf(
                    "3-2" to totalScore,
                    "3-1" to BigDecimal.ZERO,
                    "2-2" to BigDecimal.ZERO,
                    "2-1" to BigDecimal.ZERO
                )
            }
        }
    }
    
    /**
     * 학기 평균 계산
     */
    private fun calculateSemesterAverage(
        korean: Int?, social: Int?, history: Int?,
        math: Int?, science: Int?, tech: Int?, english: Int?
    ): BigDecimal {
        val scores = listOfNotNull(korean, social, history, math, science, tech, english)
        if (scores.isEmpty()) return BigDecimal.ZERO
        
        val sum = scores.sum()
        return BigDecimal.valueOf(sum.toDouble() / 7.0)
            .setScale(4, RoundingMode.HALF_UP)
    }
}