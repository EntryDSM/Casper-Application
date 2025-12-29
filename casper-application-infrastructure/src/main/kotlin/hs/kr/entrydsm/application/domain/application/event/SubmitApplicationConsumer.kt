package hs.kr.entrydsm.application.domain.application.event

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.event.dto.ApplicationSubmittedEvent
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
        val event = objectMapper.readValue(message, ApplicationSubmittedEvent::class.java)

        initializeApplicationCase(event)
        updateGraduationDate(event)
        updateScore(event)
        updateGraduationInformation(event)
    }

    private fun initializeApplicationCase(event: ApplicationSubmittedEvent) {
        applicationCaseService.initializeApplicationCase(
            event.receiptCode,
            event.educationalStatus
        )
    }

    private fun updateGraduationDate(event: ApplicationSubmittedEvent) {
        graduationInfoService.changeGraduationDate(
            event.receiptCode,
            event.graduationDate
        )
    }

    private fun updateScore(event: ApplicationSubmittedEvent) {
        when (event.educationalStatus) {
            EducationalStatus.QUALIFICATION_EXAM -> {
                applicationCaseService.updateQualificationScore(
                    event.receiptCode,
                    UpdateQualificationCaseRequest(
                        koreanGrade = event.scoreData.gedKorean,
                        socialGrade = event.scoreData.gedSocial,
                        mathGrade = event.scoreData.gedMath,
                        scienceGrade = event.scoreData.gedScience,
                        englishGrade = event.scoreData.gedEnglish,
                        historyGrade = event.scoreData.gedHistory,
                        extraScore = ExtraScoreRequest(
                            hasCertificate = event.scoreData.infoProcessingCert,
                            hasCompetitionPrize = event.scoreData.algorithmAward
                        )
                    )
                )
            }
            else -> {
                applicationCaseService.updateGraduationScore(
                    event.receiptCode,
                    UpdateGraduationCaseRequest(
                        volunteerTime = event.scoreData.volunteer,
                        absenceDayCount = event.scoreData.absence,
                        lectureAbsenceCount = event.scoreData.classExit,
                        latenessCount = event.scoreData.tardiness,
                        earlyLeaveCount = event.scoreData.earlyLeave,
                        koreanGrade = event.scoreData.koreanGrade,
                        socialGrade = event.scoreData.socialGrade,
                        historyGrade = event.scoreData.historyGrade,
                        mathGrade = event.scoreData.mathGrade,
                        scienceGrade = event.scoreData.scienceGrade,
                        englishGrade = event.scoreData.englishGrade,
                        techAndHomeGrade = event.scoreData.techAndHomeGrade,
                        extraScore = ExtraScoreRequest(
                            hasCertificate = event.scoreData.infoProcessingCert,
                            hasCompetitionPrize = event.scoreData.algorithmAward
                        )
                    )
                )
            }
        }
    }

    private fun updateGraduationInformation(event: ApplicationSubmittedEvent) {
        graduationInfoService.updateGraduationInformation(
            event.receiptCode,
            UpdateGraduationInformationRequest(
                gradeNumber = "2",
                classNumber = "1",
                studentNumber = "15",
                schoolCode = event.schoolCode,
                teacherName = event.teacherName,
                teacherTel = event.schoolPhone
            )
        )
    }
}