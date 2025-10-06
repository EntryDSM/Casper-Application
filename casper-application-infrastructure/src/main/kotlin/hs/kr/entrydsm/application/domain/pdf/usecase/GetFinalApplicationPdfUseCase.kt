package hs.kr.entrydsm.application.domain.pdf.usecase

import hs.kr.entrydsm.application.global.annotation.usecase.ReadOnlyUseCase
import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.interfaces.ApplicationContract
import hs.kr.entrydsm.domain.application.interfaces.ApplicationPdfGeneratorContract
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.security.interfaces.SecurityContract
import hs.kr.entrydsm.domain.status.interfaces.ApplicationQueryStatusContract

@ReadOnlyUseCase
class GetFinalApplicationPdfUseCase(
    private val securityContract: SecurityContract,
    private val applicationContract: ApplicationContract,
    private val applicationPdfGeneratorContract: ApplicationPdfGeneratorContract,
    private val applicationQueryStatusContract: ApplicationQueryStatusContract,
) {
    fun execute(): ByteArray {
        val userId = securityContract.getCurrentUserId()

        val application =
            applicationContract.getApplicationByUserId(userId)
                ?: throw IllegalStateException("원서 정보를 찾을 수 없습니다")

        val status =
            applicationQueryStatusContract.getStatusByReceiptCode(application.receiptCode)
                ?: throw IllegalStateException("상태 정보를 찾을 수 없습니다")

        if (!status.isSubmitted) {
            throw IllegalStateException("제출되지 않은 원서입니다")
        }

        validatePrintableApplication(application)

        // Application 도메인의 점수 계산 기능을 활용
        val updatedApplication = application.calculateAndUpdateScore()
        val scoreDetails = buildScoreDetailsMap(updatedApplication)

        return applicationPdfGeneratorContract.generate(updatedApplication, scoreDetails)
    }

    /**
     * PDF 생성에 필요한 점수 상세 정보를 구성합니다.
     */
    private fun buildScoreDetailsMap(application: Application): Map<String, Any> {
        val scoreDetails = application.getScoreDetails()

        return scoreDetails.mapValues { it.value as Any }
    }

    /**
     * 원서가 PDF 생성 가능한 상태인지 검증합니다.
     */
    private fun validatePrintableApplication(application: Application) {
        // 기본 정보 검증
        if (application.applicantName.isBlank()) {
            throw IllegalStateException("지원자명이 입력되지 않았습니다")
        }

        if (application.applicantTel.isBlank()) {
            throw IllegalStateException("지원자 연락처가 입력되지 않았습니다")
        }

        // 학력별 성적 정보 검증
        when (application.educationalStatus) {
            EducationalStatus.PROSPECTIVE_GRADUATE -> {
                validateProspectiveGraduateScores(application)
            }
            EducationalStatus.GRADUATE -> {
                validateGraduateScores(application)
            }
            EducationalStatus.QUALIFICATION_EXAM -> {
                validateGedScores(application)
            }
        }

        // 출석 및 봉사활동 정보 검증 (검정고시 제외)
        if (application.educationalStatus != EducationalStatus.QUALIFICATION_EXAM) {
            validateAttendanceAndVolunteer(application)
        }
    }

    /**
     * 졸업예정자 성적 정보를 검증합니다.
     */
    private fun validateProspectiveGraduateScores(application: Application) {
        val requiredFields =
            listOf(
                "3학년 1학기 국어" to application.korean_3_1,
                "3학년 1학기 사회" to application.social_3_1,
                "3학년 1학기 역사" to application.history_3_1,
                "3학년 1학기 수학" to application.math_3_1,
                "3학년 1학기 과학" to application.science_3_1,
                "3학년 1학기 기술·가정" to application.tech_3_1,
                "3학년 1학기 영어" to application.english_3_1,
                "2학년 2학기 국어" to application.korean_2_2,
                "2학년 2학기 사회" to application.social_2_2,
                "2학년 2학기 역사" to application.history_2_2,
                "2학년 2학기 수학" to application.math_2_2,
                "2학년 2학기 과학" to application.science_2_2,
                "2학년 2학기 기술·가정" to application.tech_2_2,
                "2학년 2학기 영어" to application.english_2_2,
                "2학년 1학기 국어" to application.korean_2_1,
                "2학년 1학기 사회" to application.social_2_1,
                "2학년 1학기 역사" to application.history_2_1,
                "2학년 1학기 수학" to application.math_2_1,
                "2학년 1학기 과학" to application.science_2_1,
                "2학년 1학기 기술·가정" to application.tech_2_1,
                "2학년 1학기 영어" to application.english_2_1,
            )

        requiredFields.forEach { (fieldName, value) ->
            if (value == null || value < 1 || value > 5) {
                throw IllegalStateException("$fieldName 성적이 올바르게 입력되지 않았습니다 (1-5점)")
            }
        }
    }

    /**
     * 졸업자 성적 정보를 검증합니다.
     */
    private fun validateGraduateScores(application: Application) {
        // 졸업예정자와 동일한 검증 + 3학년 2학기
        validateProspectiveGraduateScores(application)

        val additionalFields =
            listOf(
                "3학년 2학기 국어" to application.korean_3_2,
                "3학년 2학기 사회" to application.social_3_2,
                "3학년 2학기 역사" to application.history_3_2,
                "3학년 2학기 수학" to application.math_3_2,
                "3학년 2학기 과학" to application.science_3_2,
                "3학년 2학기 기술·가정" to application.tech_3_2,
                "3학년 2학기 영어" to application.english_3_2,
            )

        additionalFields.forEach { (fieldName, value) ->
            if (value == null || value < 1 || value > 5) {
                throw IllegalStateException("$fieldName 성적이 올바르게 입력되지 않았습니다 (1-5점)")
            }
        }
    }

    /**
     * 검정고시 성적 정보를 검증합니다.
     */
    private fun validateGedScores(application: Application) {
        val gedFields =
            listOf(
                "검정고시 국어" to application.gedKorean,
                "검정고시 사회" to application.gedSocial,
                "검정고시 역사" to application.gedHistory,
                "검정고시 수학" to application.gedMath,
                "검정고시 과학" to application.gedScience,
                "검정고시 기술·가정" to application.gedTech,
                "검정고시 영어" to application.gedEnglish,
            )

        gedFields.forEach { (fieldName, value) ->
            if (value == null || value < 0 || value > 100) {
                throw IllegalStateException("$fieldName 성적이 올바르게 입력되지 않았습니다 (0-100점)")
            }
        }
    }

    /**
     * 출석 및 봉사활동 정보를 검증합니다.
     */
    private fun validateAttendanceAndVolunteer(application: Application) {
        val attendanceFields =
            listOf(
                "결석일수" to application.absence,
                "지각횟수" to application.tardiness,
                "조퇴횟수" to application.earlyLeave,
                "결과횟수" to application.classExit,
            )

        attendanceFields.forEach { (fieldName, value) ->
            if (value == null || value < 0) {
                throw IllegalStateException("$fieldName 이 올바르게 입력되지 않았습니다 (0 이상)")
            }
        }

        val volunteer = application.volunteer
        if (volunteer == null || volunteer < 0) {
            throw IllegalStateException("봉사활동 시간이 올바르게 입력되지 않았습니다 (0 이상)")
        }
    }
}
