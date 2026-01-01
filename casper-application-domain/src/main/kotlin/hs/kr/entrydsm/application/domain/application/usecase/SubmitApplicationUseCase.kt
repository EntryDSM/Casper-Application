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

        val receiptCode = application.receiptCode
        val educationalStatus = request.applicationInfo.educationalStatus

        // 2. GraduationInfo 초기화 + 업데이트 (동기)
        graduationInfoService.changeGraduationInfo(
            receiptCode = receiptCode,
            graduateDate = request.applicationInfo.graduationDate
        )
        if (educationalStatus != EducationalStatus.QUALIFICATION_EXAM) {
            graduationInfoService.updateGraduationInformation(
                receiptCode = receiptCode,
                request = UpdateGraduationInformationRequest(
                    gradeNumber = request.applicationInfo.studentId.substring(0, 1),
                    classNumber = request.applicationInfo.studentId.substring(1, 2),
                    studentNumber = request.applicationInfo.studentId.substring(2),
                    schoolCode = request.schoolInfo.schoolCode,
                    teacherName = request.schoolInfo.teacherName,
                    teacherTel = request.schoolInfo.schoolPhone
                )
            )
        }

        // 3. ApplicationCase 초기화 + 업데이트 (동기)
        applicationCaseService.initializeApplicationCase(receiptCode, educationalStatus)
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

        // 4. Score 생성 + 계산 (동기)
        scoreService.createScore(receiptCode)
        scoreService.updateScore(receiptCode)

        applicationEventPort.create(receiptCode, userId)

        // 이벤트 처리 로직을 동기적으로 실행하도록 변경
        // cause. 이벤트 순서 문제(Kafka는 비동기적으로 호출되어Score 생성, applicationCase, Score 업데이트 순서가 보장되지 않음.)
//        applicationEventPort.submitApplication(
//            SubmitApplicationMapper.toSubmissionData(request, application, userId)
//        )
    }
}
