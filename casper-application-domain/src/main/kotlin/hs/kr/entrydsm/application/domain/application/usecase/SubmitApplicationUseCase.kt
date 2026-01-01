package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.event.spi.ApplicationEventPort
import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.application.usecase.mapper.SubmitApplicationMapper
import hs.kr.entrydsm.application.domain.application.spi.ApplicationQueryUserPort
import hs.kr.entrydsm.application.domain.application.spi.CommandApplicationPort
import hs.kr.entrydsm.application.domain.application.spi.QueryApplicationPort
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.SubmitApplicationRequest
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import hs.kr.entrydsm.application.domain.applicationCase.service.ApplicationCaseService
import hs.kr.entrydsm.application.domain.applicationCase.usecase.dto.request.ExtraScoreRequest
import hs.kr.entrydsm.application.domain.applicationCase.usecase.dto.request.UpdateGraduationCaseRequest
import hs.kr.entrydsm.application.domain.applicationCase.usecase.dto.request.UpdateQualificationCaseRequest
import hs.kr.entrydsm.application.domain.graduationInfo.service.GraduationInfoService
import hs.kr.entrydsm.application.domain.graduationInfo.model.vo.StudentNumber
import hs.kr.entrydsm.application.domain.graduationInfo.usecase.dto.request.UpdateGraduationInformationRequest
import hs.kr.entrydsm.application.domain.score.service.ScoreService
import hs.kr.entrydsm.application.global.annotation.UseCase
import hs.kr.entrydsm.application.global.security.spi.SecurityPort

@UseCase
class SubmitApplicationUseCase(
    private val securityPort: SecurityPort,
    private val applicationEventPort: ApplicationEventPort,
    private val commandApplicationPort: CommandApplicationPort,
    private val applicationQueryUserPort: ApplicationQueryUserPort,
    private val queryApplicationPort: QueryApplicationPort,
    private val applicationCaseService: ApplicationCaseService,
    private val graduationInfoService: GraduationInfoService,
    private val scoreService: ScoreService
) {
    fun execute(request: SubmitApplicationRequest) {
        val userId = securityPort.getCurrentUserId()
        val user = applicationQueryUserPort.queryUserByUserId(userId)
        if (queryApplicationPort.isExistsApplicationByUserId(userId)) {
            throw ApplicationExceptions.ApplicationExistsException()
        }
        val application = commandApplicationPort.save(
            SubmitApplicationMapper.toApplication(request, user)
        )

        handleSubmissionSideEffects(application.receiptCode, request)

        applicationEventPort.create(application.receiptCode, userId)

        // 이벤트 처리 로직을 동기적으로 실행하도록 변경
        // cause. 이벤트 순서 문제(Kafka는 비동기적으로 호출되어Score 생성, applicationCase, Score 업데이트 순서가 보장되지 않음.)
//        applicationEventPort.submitApplication(
//            SubmitApplicationMapper.toSubmissionData(request, application, userId)
//        )
    }

    private fun handleSubmissionSideEffects(receiptCode: Long, request: SubmitApplicationRequest) {
        val educationalStatus = request.applicationInfo.educationalStatus

        initializeGraduationInfo(receiptCode, request)
        updateGraduationInformation(receiptCode, educationalStatus, request)

        initializeApplicationCase(receiptCode, educationalStatus)
        updateApplicationCase(receiptCode, educationalStatus, request)

        scoreService.createScore(receiptCode)
        scoreService.updateScore(receiptCode)
    }

    private fun initializeGraduationInfo(receiptCode: Long, request: SubmitApplicationRequest) {
        graduationInfoService.changeGraduationInfo(
            receiptCode = receiptCode,
            graduateDate = request.applicationInfo.graduationDate
        )
    }

    private fun updateGraduationInformation(
        receiptCode: Long,
        educationalStatus: EducationalStatus,
        request: SubmitApplicationRequest
    ) {
        if (educationalStatus != EducationalStatus.QUALIFICATION_EXAM) {
            val studentNumber: StudentNumber = StudentNumber.from(request.applicationInfo.studentNumber)
            graduationInfoService.updateGraduationInformation(
                receiptCode = receiptCode,
                request = UpdateGraduationInformationRequest(
                    gradeNumber = studentNumber.gradeNumber,
                    classNumber = studentNumber.classNumber,
                    studentNumber = studentNumber.studentNumber,
                    schoolCode = request.schoolInfo.schoolCode,
                    teacherName = request.schoolInfo.teacherName,
                    teacherTel = request.schoolInfo.schoolPhone
                )
            )
        }
    }

    private fun initializeApplicationCase(receiptCode: Long, educationalStatus: EducationalStatus) {
        applicationCaseService.initializeApplicationCase(receiptCode, educationalStatus)
    }

    private fun updateApplicationCase(
        receiptCode: Long,
        educationalStatus: EducationalStatus,
        request: SubmitApplicationRequest
    ) {
        when (educationalStatus) {
            EducationalStatus.QUALIFICATION_EXAM -> {
                applicationCaseService.updateQualificationScore(
                    receiptCode = receiptCode,
                    request = UpdateQualificationCaseRequest(
                        koreanGrade = request.gradeInfo.gedKorean,
                        socialGrade = request.gradeInfo.gedSocial,
                        mathGrade = request.gradeInfo.gedMath,
                        scienceGrade = request.gradeInfo.gedScience,
                        englishGrade = request.gradeInfo.gedEnglish,
                        historyGrade = request.gradeInfo.gedHistory,
                        extraScore = ExtraScoreRequest(
                            hasCertificate = request.awardAndCertificateInfo.infoProcessingCert,
                            hasCompetitionPrize = request.awardAndCertificateInfo.algorithmAward
                        )
                    )
                )
            }
            else -> {
                applicationCaseService.updateGraduationScore(
                    receiptCode = receiptCode,
                    request = UpdateGraduationCaseRequest(
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
                        extraScore = ExtraScoreRequest(
                            hasCertificate = request.awardAndCertificateInfo.infoProcessingCert,
                            hasCompetitionPrize = request.awardAndCertificateInfo.algorithmAward
                        )
                    )
                )
            }
        }
    }
}
