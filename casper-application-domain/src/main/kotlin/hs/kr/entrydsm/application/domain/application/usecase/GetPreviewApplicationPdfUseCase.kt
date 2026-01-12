package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.application.model.types.ApplicationRemark
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import hs.kr.entrydsm.application.domain.application.spi.ApplicationPdfGeneratorPort
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.ApplicationRequest
import hs.kr.entrydsm.application.domain.applicationCase.model.ApplicationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.GraduationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.QualificationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.vo.ExtraScoreItem
import hs.kr.entrydsm.application.domain.graduationInfo.exception.GraduationInfoExceptions
import hs.kr.entrydsm.application.domain.graduationInfo.model.Graduation
import hs.kr.entrydsm.application.domain.graduationInfo.model.GraduationInfo
import hs.kr.entrydsm.application.domain.graduationInfo.model.Qualification
import hs.kr.entrydsm.application.domain.graduationInfo.model.vo.StudentNumber
import hs.kr.entrydsm.application.domain.photo.exception.PhotoExceptions
import hs.kr.entrydsm.application.domain.photo.spi.QueryPhotoPort
import hs.kr.entrydsm.application.domain.score.model.Score
import hs.kr.entrydsm.application.global.annotation.ReadOnlyUseCase
import hs.kr.entrydsm.application.global.security.spi.SecurityPort
import java.util.UUID

@ReadOnlyUseCase
class GetPreviewApplicationPdfUseCase(
    private val securityPort: SecurityPort,
    private val applicationPdfGeneratorPort: ApplicationPdfGeneratorPort,
    private val queryPhotoPort: QueryPhotoPort,
) {

    fun execute(request: ApplicationRequest): ByteArray {
        val userId = securityPort.getCurrentUserId()
        val application = createTempApplication(request, userId)

        validatePrintableApplicant(application)

        val applicationCase = createTempApplicationCase(request, application)
        val calculatedScore = createTempScore(application, applicationCase)
        val graduationInfo = createTempGraduationInfo(request, application)
        val photo = queryPhotoPort.queryPhotoByUserId(userId)
            ?: throw PhotoExceptions.PhotoNotFoundException()

        return applicationPdfGeneratorPort.generate(
            application = application,
            score = calculatedScore,
            photo = photo,
            graduationInfo = graduationInfo,
            applicationCase = applicationCase
        )
    }

    private fun validatePrintableApplicant(application: Application) {
        if (application.isEducationalStatusEmpty())
            throw GraduationInfoExceptions.EducationalStatusUnmatchedException()
    }

    private fun createTempApplication(request: ApplicationRequest, userId: UUID): Application {
        val applicationRemark = when {
            request.applicationInfo.nationalMeritChild -> ApplicationRemark.NATIONAL_MERIT
            request.applicationInfo.specialAdmissionTarget -> ApplicationRemark.PRIVILEGED_ADMISSION
            else -> null
        }

        return Application(
            sex = request.applicantInfo.applicantGender,
            isDaejeon = request.addressInfo.isDaejeon,
            birthDate = request.applicantInfo.birthDate,
            educationalStatus = request.applicationInfo.educationalStatus,
            applicantName = request.applicantInfo.applicantName,
            applicantTel = request.applicantInfo.applicantTel,
            parentName = request.applicantInfo.parentName,
            parentTel = request.applicantInfo.parentTel,
            parentRelation = request.applicantInfo.parentRelation,
            streetAddress = request.addressInfo.streetAddress,
            postalCode = request.addressInfo.postalCode,
            detailAddress = request.addressInfo.detailAddress,
            applicationType = request.applicationInfo.applicationType,
            applicationRemark = applicationRemark,
            studyPlan = request.applicationInfo.studyPlan,
            selfIntroduce = request.applicationInfo.selfIntroduce,
            userId = userId
        )
    }

    private fun createTempApplicationCase(
        request: ApplicationRequest,
        application: Application
    ): ApplicationCase {
        val extraScoreItem = ExtraScoreItem(
            hasCertificate = request.awardAndCertificateInfo.infoProcessingCert,
            hasCompetitionPrize = request.awardAndCertificateInfo.algorithmAward
        )

        return when (request.applicationInfo.educationalStatus) {
            EducationalStatus.QUALIFICATION_EXAM -> QualificationCase(
                receiptCode = application.receiptCode,
                extraScoreItem = extraScoreItem,
                koreanGrade = request.gradeInfo.gedKorean,
                socialGrade = request.gradeInfo.gedSocial,
                mathGrade = request.gradeInfo.gedMath,
                scienceGrade = request.gradeInfo.gedScience,
                englishGrade = request.gradeInfo.gedEnglish,
                historyGrade = request.gradeInfo.gedHistory,
                isCommon = application.isCommon()
            )
            else -> GraduationCase(
                receiptCode = application.receiptCode,
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
                isProspectiveGraduate = request.applicationInfo.educationalStatus ==
                    EducationalStatus.PROSPECTIVE_GRADUATE
            )
        }
    }

    private fun createTempScore(application: Application, applicationCase: ApplicationCase): Score {
        val extraScore = if (application.isCommon()) {
            applicationCase.calculateCompetitionScore()
        } else {
            applicationCase.calculateCompetitionScore().add(applicationCase.calculateCertificateScore())
        }

        return Score(receiptCode = application.receiptCode)
            .updateScore(applicationCase, application.isCommon(), extraScore)
    }

    private fun createTempGraduationInfo(
        request: ApplicationRequest,
        application: Application
    ): GraduationInfo {
        val educationalStatus = request.applicationInfo.educationalStatus
        val graduateDate = request.applicationInfo.graduationDate

        return when (educationalStatus) {
            EducationalStatus.QUALIFICATION_EXAM -> Qualification(
                graduateDate = graduateDate,
                receiptCode = application.receiptCode,
                isProspectiveGraduate = false
            )
            EducationalStatus.GRADUATE,
            EducationalStatus.PROSPECTIVE_GRADUATE -> Graduation(
                graduateDate = graduateDate,
                receiptCode = application.receiptCode,
                isProspectiveGraduate = educationalStatus == EducationalStatus.PROSPECTIVE_GRADUATE,
                studentNumber = StudentNumber.from(request.applicationInfo.studentNumber),
                schoolCode = request.schoolInfo.schoolCode,
                teacherName = request.schoolInfo.teacherName,
                teacherTel = request.schoolInfo.schoolPhone
            )
        }
    }
}
