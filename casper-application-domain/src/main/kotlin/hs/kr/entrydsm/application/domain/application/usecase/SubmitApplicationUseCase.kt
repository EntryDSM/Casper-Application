package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.event.dto.ScoreData
import hs.kr.entrydsm.application.domain.application.event.dto.SubmissionData
import hs.kr.entrydsm.application.domain.application.event.spi.ApplicationEventPort
import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.application.spi.ApplicationQueryUserPort
import hs.kr.entrydsm.application.domain.application.spi.CommandApplicationPort
import hs.kr.entrydsm.application.domain.application.spi.QueryApplicationPort
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.SubmissionApplicationWebRequest
import hs.kr.entrydsm.application.domain.user.model.User
import hs.kr.entrydsm.application.global.annotation.UseCase
import hs.kr.entrydsm.application.global.security.spi.SecurityPort
import java.util.UUID

@UseCase
class SubmitApplicationUseCase(
    private val securityPort: SecurityPort,
    private val applicationEventPort: ApplicationEventPort,
    private val commandApplicationPort: CommandApplicationPort,
    private val applicationQueryUserPort: ApplicationQueryUserPort,
    private val queryApplicationPort: QueryApplicationPort
) {

    fun execute(request: SubmissionApplicationWebRequest) {
        val userId = securityPort.getCurrentUserId()
        val user = applicationQueryUserPort.queryUserByUserId(userId)
        if (queryApplicationPort.isExistsApplicationByUserId(userId)) {
            throw ApplicationExceptions.ApplicationExistsException()
        }
        val application = commandApplicationPort.save(createWithUserInfo(request, user))

        applicationEventPort.submitApplication(
            createSubmissionData(application, userId, request)
        )

        applicationEventPort.create(application.receiptCode, userId)
    }

    private fun createWithUserInfo(request: SubmissionApplicationWebRequest, user: User): Application {
        return request.run {
            Application(
                applicantName = user.name,
                applicantTel = user.phoneNumber,
                birthDate = birthDate,
                sex = applicantGender,
                streetAddress = streetAddress,
                postalCode = postalCode,
                detailAddress = detailAddress,
                isDaejeon = isDaejeon,
                parentName = parentName,
                parentTel = parentTel,
                parentRelation = parentRelation,
                educationalStatus = educationalStatus,
                applicationType = applicationType,
                studyPlan = studyPlan,
                selfIntroduce = selfIntroduce,
                userId = user.id
            )
        }
    }

    private fun createSubmissionData(
        application: Application,
        userId: UUID,
        request: SubmissionApplicationWebRequest
    ): SubmissionData {
        return SubmissionData(
            receiptCode = application.receiptCode,
            userId = userId,
            educationalStatus = request.educationalStatus,
            graduationDate = request.graduationDate,
            schoolCode = request.schoolCode,
            teacherName = request.teacherName,
            schoolPhone = request.schoolPhone,
            scoreData = ScoreData(
                koreanGrade = request.koreanGrade,
                socialGrade = request.socialGrade,
                historyGrade = request.historyGrade,
                mathGrade = request.mathGrade,
                scienceGrade = request.scienceGrade,
                englishGrade = request.englishGrade,
                techAndHomeGrade = request.techAndHomeGrade,
                gedKorean = request.gedKorean,
                gedSocial = request.gedSocial,
                gedHistory = request.gedHistory,
                gedMath = request.gedMath,
                gedScience = request.gedScience,
                gedEnglish = request.gedEnglish,
                absence = request.absence,
                tardiness = request.tardiness,
                earlyLeave = request.earlyLeave,
                classExit = request.classExit,
                volunteer = request.volunteer,
                algorithmAward = request.algorithmAward,
                infoProcessingCert = request.infoProcessingCert
            )
        )
    }
}