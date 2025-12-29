package hs.kr.entrydsm.application.domain.applicationCase.service

import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import hs.kr.entrydsm.application.domain.applicationCase.event.spi.ApplicationCaseEventPort
import hs.kr.entrydsm.application.domain.applicationCase.exception.ApplicationCaseExceptions
import hs.kr.entrydsm.application.domain.applicationCase.factory.ApplicationCaseFactory
import hs.kr.entrydsm.application.domain.applicationCase.model.ApplicationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.GraduationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.QualificationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.vo.ExtraScoreItem
import hs.kr.entrydsm.application.domain.applicationCase.spi.ApplicationCaseQueryApplicationPort
import hs.kr.entrydsm.application.domain.applicationCase.spi.CommandApplicationCasePort
import hs.kr.entrydsm.application.domain.applicationCase.spi.QueryApplicationCasePort
import hs.kr.entrydsm.application.domain.applicationCase.usecase.dto.request.UpdateGraduationCaseRequest
import hs.kr.entrydsm.application.domain.applicationCase.usecase.dto.request.UpdateQualificationCaseRequest
import hs.kr.entrydsm.application.global.annotation.DomainService

@DomainService
class ApplicationCaseService(
    private val applicationCaseQueryApplicationPort: ApplicationCaseQueryApplicationPort,
    private val commandApplicationCasePort: CommandApplicationCasePort,
    private val queryApplicationCasePort: QueryApplicationCasePort,
    private val applicationCaseFactory: ApplicationCaseFactory,
    private val applicationCaseEventPort: ApplicationCaseEventPort,
) {
    fun hasEducationalStatusMismatch(application: Application, applicationCase: ApplicationCase): Boolean {
        application.educationalStatus ?: throw ApplicationCaseExceptions.EducationalStatusUnmatchedException()

        return when (application.educationalStatus) {
            EducationalStatus.GRADUATE, EducationalStatus.PROSPECTIVE_GRADUATE ->
                applicationCase !is GraduationCase
            EducationalStatus.QUALIFICATION_EXAM ->
                applicationCase !is QualificationCase
        }
    }

    fun initializeApplicationCase(receiptCode: Long, educationalStatus: EducationalStatus?) {
        val application = applicationCaseQueryApplicationPort.queryApplicationByReceiptCode(receiptCode)
            ?: throw ApplicationExceptions.ApplicationNotFoundException()

        if (!queryApplicationCasePort.isExistsApplicationCaseByApplication(application)) {
            commandApplicationCasePort.save(
                applicationCaseFactory.createApplicationCase(
                    receiptCode,
                    educationalStatus
                )
            )
        }
    }

    fun updateGraduationCase(application: Application, request: UpdateGraduationCaseRequest) {
        val graduationCase = queryApplicationCasePort.queryApplicationCaseByApplication(application)

        if (graduationCase !is GraduationCase) {
            throw ApplicationCaseExceptions.EducationalStatusUnmatchedException()
        }

        request.run {
            commandApplicationCasePort.save(
                graduationCase.copy(
                    volunteerTime = volunteerTime,
                    absenceDayCount = absenceDayCount,
                    lectureAbsenceCount = lectureAbsenceCount,
                    latenessCount = latenessCount,
                    earlyLeaveCount = earlyLeaveCount,
                    koreanGrade = koreanGrade,
                    socialGrade = socialGrade,
                    historyGrade = historyGrade,
                    mathGrade = mathGrade,
                    scienceGrade = scienceGrade,
                    englishGrade = englishGrade,
                    techAndHomeGrade = techAndHomeGrade,
                    extraScoreItem = ExtraScoreItem(
                        hasCertificate = extraScore.hasCertificate,
                        hasCompetitionPrize = extraScore.hasCompetitionPrize
                    )
                )
            )
        }

        applicationCaseEventPort.updateGraduationCase(graduationCase)
    }

    fun updateGraduationScore(receiptCode: Long, request: UpdateGraduationCaseRequest) {
        val application = applicationCaseQueryApplicationPort.queryApplicationByReceiptCode(receiptCode)
            ?: throw ApplicationExceptions.ApplicationNotFoundException()
        updateGraduationCase(application, request)
    }

    fun updateQualificationScore(application: Application, request: UpdateQualificationCaseRequest) {
        val qualificationCase = queryApplicationCasePort.queryApplicationCaseByApplication(application)

        if (qualificationCase !is QualificationCase) {
            throw ApplicationCaseExceptions.EducationalStatusUnmatchedException()
        }

        commandApplicationCasePort.save(
            qualificationCase.copy(
                koreanGrade = request.koreanGrade,
                scienceGrade = request.scienceGrade,
                socialGrade = request.socialGrade,
                mathGrade = request.mathGrade,
                englishGrade = request.englishGrade,
                historyGrade = request.historyGrade,
                extraScoreItem = ExtraScoreItem(
                    hasCertificate = request.extraScore.hasCertificate,
                    hasCompetitionPrize = request.extraScore.hasCompetitionPrize
                )
            )
        )

        applicationCaseEventPort.updateQualificationCase(qualificationCase)
    }

    fun updateQualificationScore(receiptCode: Long, request: UpdateQualificationCaseRequest) {
        val application = applicationCaseQueryApplicationPort.queryApplicationByReceiptCode(receiptCode)
            ?: throw ApplicationExceptions.ApplicationNotFoundException()
        updateQualificationScore(application, request)
    }
}