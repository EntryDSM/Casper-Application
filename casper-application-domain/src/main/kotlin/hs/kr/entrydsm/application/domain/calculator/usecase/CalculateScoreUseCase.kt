package hs.kr.entrydsm.application.domain.calculator.usecase

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import hs.kr.entrydsm.application.domain.applicationCase.model.ApplicationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.GraduationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.QualificationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.vo.ExtraScoreItem
import hs.kr.entrydsm.application.domain.calculator.usecase.dto.request.CalculateScoreRequest
import hs.kr.entrydsm.application.domain.calculator.usecase.dto.response.CalculateScoreResponse
import hs.kr.entrydsm.application.domain.score.model.Score
import hs.kr.entrydsm.application.global.annotation.UseCase
import java.math.BigDecimal

@UseCase
class CalculateScoreUseCase() {

    fun execute(request: CalculateScoreRequest): CalculateScoreResponse {
        val isCommon = request.applicationType == ApplicationType.COMMON
        val applicationCase = createTempApplicationCase(request, isCommon)
        val extraScore = if (isCommon) {
            applicationCase.calculateCompetitionScore()
        } else {
            applicationCase.calculateCompetitionScore().add(applicationCase.calculateCertificateScore())
        }

        val score = Score(receiptCode = 0L)
            .updateScore(applicationCase, isCommon, extraScore)

        return CalculateScoreResponse(
            totalScore = score.totalScore ?: BigDecimal.ZERO
        )
    }

    private fun createTempApplicationCase(
        request: CalculateScoreRequest,
        isCommon: Boolean
    ): ApplicationCase {
        val extraScoreItem = ExtraScoreItem(
            hasCertificate = request.awardAndCertificateInfo.infoProcessingCert,
            hasCompetitionPrize = request.awardAndCertificateInfo.algorithmAward
        )

        return when (request.educationalStatus) {
            EducationalStatus.QUALIFICATION_EXAM -> QualificationCase(
                receiptCode = 0L,
                extraScoreItem = extraScoreItem,
                koreanGrade = request.gradeInfo.gedKorean,
                socialGrade = request.gradeInfo.gedSocial,
                mathGrade = request.gradeInfo.gedMath,
                scienceGrade = request.gradeInfo.gedScience,
                englishGrade = request.gradeInfo.gedEnglish,
                historyGrade = request.gradeInfo.gedHistory,
                isCommon = isCommon
            )
            else -> GraduationCase(
                receiptCode = 0L,
                extraScoreItem = extraScoreItem,
                volunteerTime = request.attendanceInfo.volunteer,
                absenceDayCount = request.attendanceInfo.absence,
                lectureAbsenceCount = request.attendanceInfo.classExit,
                latenessCount = request.attendanceInfo.tardiness,
                earlyLeaveCount = request.attendanceInfo.earlyLeave,
                koreanGrade = request.gradeInfo.koreanGrade,
                socialGrade = request.gradeInfo.socialGrade,
                historyGrade = request.gradeInfo.historyGrade,
                mathGrade = request.gradeInfo.mathGrade,
                scienceGrade = request.gradeInfo.scienceGrade,
                englishGrade = request.gradeInfo.englishGrade,
                techAndHomeGrade = request.gradeInfo.techAndHomeGrade,
                isProspectiveGraduate = request.educationalStatus == EducationalStatus.PROSPECTIVE_GRADUATE
            )
        }
    }
}
