package hs.kr.entrydsm.application.domain.application.event

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.event.dto.SubmitApplicationEvent
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import hs.kr.entrydsm.application.domain.applicationCase.service.ApplicationCaseService
import hs.kr.entrydsm.application.domain.applicationCase.usecase.dto.request.ExtraScoreRequest
import hs.kr.entrydsm.application.domain.applicationCase.usecase.dto.request.UpdateGraduationCaseRequest
import hs.kr.entrydsm.application.domain.applicationCase.usecase.dto.request.UpdateQualificationCaseRequest
import hs.kr.entrydsm.application.domain.graduationInfo.service.GraduationInfoService
import hs.kr.entrydsm.application.domain.graduationInfo.usecase.dto.request.UpdateGraduationInformationRequest
import hs.kr.entrydsm.application.global.kafka.config.KafkaTopics
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class SubmitApplicationConsumer(
    private val objectMapper: ObjectMapper,
    private val applicationCaseService: ApplicationCaseService,
    private val graduationInfoService: GraduationInfoService
) {

    @KafkaListener(
        topics = [KafkaTopics.SUBMIT_APPLICATION],
        groupId = "application-submitted",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun submitApplication(message: String) {
        val submitApplicationEvent = objectMapper.readValue(message, SubmitApplicationEvent::class.java)

        initializeApplicationCase(submitApplicationEvent)
        initializeGraduationInfo(submitApplicationEvent)
        updateScore(submitApplicationEvent)
        updateGraduationInformation(submitApplicationEvent)
    }

    private fun initializeApplicationCase(submitApplicationEvent: SubmitApplicationEvent) {
        applicationCaseService.initializeApplicationCase(
            submitApplicationEvent.receiptCode,
            submitApplicationEvent.educationalStatus
        )
    }

    private fun initializeGraduationInfo(submitApplicationEvent: SubmitApplicationEvent) {
        graduationInfoService.changeGraduationInfo(
            submitApplicationEvent.receiptCode,
            submitApplicationEvent.graduationDate
        )
    }

    private fun updateGraduationInformation(submitApplicationEvent: SubmitApplicationEvent) {
        if (submitApplicationEvent.educationalStatus == EducationalStatus.QUALIFICATION_EXAM) {
            return
        }
        graduationInfoService.updateGraduationInformation(
            submitApplicationEvent.receiptCode,
            UpdateGraduationInformationRequest(
                gradeNumber = submitApplicationEvent.gradeNumber,
                classNumber = submitApplicationEvent.classNumber,
                studentNumber = submitApplicationEvent.studentNumber,
                schoolCode = submitApplicationEvent.schoolCode,
                teacherName = submitApplicationEvent.teacherName,
                teacherTel = submitApplicationEvent.schoolPhone
            )
        )
    }

    private fun updateScore(submitApplicationEvent: SubmitApplicationEvent) {
        when (submitApplicationEvent.educationalStatus) {
            EducationalStatus.QUALIFICATION_EXAM -> {
                applicationCaseService.updateQualificationScore(
                    submitApplicationEvent.receiptCode,
                    UpdateQualificationCaseRequest(
                        koreanGrade = submitApplicationEvent.scoreData.gedKorean,
                        socialGrade = submitApplicationEvent.scoreData.gedSocial,
                        mathGrade = submitApplicationEvent.scoreData.gedMath,
                        scienceGrade = submitApplicationEvent.scoreData.gedScience,
                        englishGrade = submitApplicationEvent.scoreData.gedEnglish,
                        historyGrade = submitApplicationEvent.scoreData.gedHistory,
                        extraScore = ExtraScoreRequest(
                            hasCertificate = submitApplicationEvent.scoreData.infoProcessingCert,
                            hasCompetitionPrize = submitApplicationEvent.scoreData.algorithmAward
                        )
                    )
                )
            }
            else -> {
                applicationCaseService.updateGraduationScore(
                    submitApplicationEvent.receiptCode,
                    UpdateGraduationCaseRequest(
                        volunteerTime = submitApplicationEvent.scoreData.volunteer,
                        absenceDayCount = submitApplicationEvent.scoreData.absence,
                        lectureAbsenceCount = submitApplicationEvent.scoreData.classExit,
                        latenessCount = submitApplicationEvent.scoreData.tardiness,
                        earlyLeaveCount = submitApplicationEvent.scoreData.earlyLeave,
                        koreanGrade = submitApplicationEvent.scoreData.koreanGrade,
                        socialGrade = submitApplicationEvent.scoreData.socialGrade,
                        historyGrade = submitApplicationEvent.scoreData.historyGrade,
                        mathGrade = submitApplicationEvent.scoreData.mathGrade,
                        scienceGrade = submitApplicationEvent.scoreData.scienceGrade,
                        englishGrade = submitApplicationEvent.scoreData.englishGrade,
                        techAndHomeGrade = submitApplicationEvent.scoreData.techAndHomeGrade,
                        extraScore = ExtraScoreRequest(
                            hasCertificate = submitApplicationEvent.scoreData.infoProcessingCert,
                            hasCompetitionPrize = submitApplicationEvent.scoreData.algorithmAward
                        )
                    )
                )
            }
        }
    }
}