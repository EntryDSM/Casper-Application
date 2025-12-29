package hs.kr.entrydsm.application.domain.application.event

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.event.dto.ApplicationScoreData
import hs.kr.entrydsm.application.domain.application.event.dto.SubmitApplicationEvent
import hs.kr.entrydsm.application.domain.application.event.dto.CreateApplicationEvent
import hs.kr.entrydsm.application.domain.application.event.dto.SubmissionData
import hs.kr.entrydsm.application.domain.application.event.spi.ApplicationEventPort
import hs.kr.entrydsm.application.global.kafka.config.KafkaTopics
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class ApplicationProducer(
    private val mapper: ObjectMapper,
    private val createApplicationTemplate: KafkaTemplate<String, Any>,
    private val createApplicationRollbackTemplate: KafkaTemplate<String, Any>,
    private val publishApplicationSubmittedTemplate: KafkaTemplate<String, Any>
) : ApplicationEventPort {

    override fun create(receiptCode: Long, userId: UUID) {
        val createApplicationEvent = CreateApplicationEvent(
            userId = userId,
            receiptCode = receiptCode,
        )
        createApplicationTemplate.send(
            KafkaTopics.CREATE_APPLICATION,
            mapper.writeValueAsString(createApplicationEvent),
        )
    }

    override fun submitApplication(submissionData: SubmissionData) {
        val event = SubmitApplicationEvent(
            receiptCode = submissionData.receiptCode,
            userId = submissionData.userId,
            educationalStatus = submissionData.educationalStatus,
            graduationDate = submissionData.graduationDate,
            schoolCode = submissionData.schoolCode,
            teacherName = submissionData.teacherName,
            schoolPhone = submissionData.schoolPhone,
            scoreData = ApplicationScoreData(
                koreanGrade = submissionData.scoreData.koreanGrade,
                socialGrade = submissionData.scoreData.socialGrade,
                historyGrade = submissionData.scoreData.historyGrade,
                mathGrade = submissionData.scoreData.mathGrade,
                scienceGrade = submissionData.scoreData.scienceGrade,
                englishGrade = submissionData.scoreData.englishGrade,
                techAndHomeGrade = submissionData.scoreData.techAndHomeGrade,
                gedKorean = submissionData.scoreData.gedKorean,
                gedSocial = submissionData.scoreData.gedSocial,
                gedHistory = submissionData.scoreData.gedHistory,
                gedMath = submissionData.scoreData.gedMath,
                gedScience = submissionData.scoreData.gedScience,
                gedEnglish = submissionData.scoreData.gedEnglish,
                absence = submissionData.scoreData.absence,
                tardiness = submissionData.scoreData.tardiness,
                earlyLeave = submissionData.scoreData.earlyLeave,
                classExit = submissionData.scoreData.classExit,
                volunteer = submissionData.scoreData.volunteer,
                algorithmAward = submissionData.scoreData.algorithmAward,
                infoProcessingCert = submissionData.scoreData.infoProcessingCert
            )
        )
        publishApplicationSubmittedTemplate.send(
            KafkaTopics.SUBMIT_APPLICATION,
            mapper.writeValueAsString(event)
        )
    }

    override fun createApplicationScoreRollback(receiptCode: Long) {
        createApplicationRollbackTemplate.send(
            KafkaTopics.CREATE_APPLICATION_SCORE_ROLLBACK,
            receiptCode
        )
    }
}
