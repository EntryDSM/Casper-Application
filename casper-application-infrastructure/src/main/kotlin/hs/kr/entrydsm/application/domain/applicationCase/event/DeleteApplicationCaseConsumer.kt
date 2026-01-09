package hs.kr.entrydsm.application.domain.applicationCase.event

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.application.spi.QueryApplicationPort
import hs.kr.entrydsm.application.domain.applicationCase.exception.ApplicationCaseExceptions
import hs.kr.entrydsm.application.domain.applicationCase.spi.CommandApplicationCasePort
import hs.kr.entrydsm.application.domain.applicationCase.spi.QueryApplicationCasePort
import hs.kr.entrydsm.application.global.kafka.config.KafkaTopics
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class DeleteApplicationCaseConsumer(
    private val mapper: ObjectMapper,
    private val commandApplicationCasePort: CommandApplicationCasePort,
    private val queryApplicationPort: QueryApplicationPort,
    private val queryApplicationCasePort: QueryApplicationCasePort,
) {
    @KafkaListener(
        topics = [KafkaTopics.DELETE_USER],
        groupId = "delete-application-case-consumer",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun deleteApplication(message: String) {
        val receiptCode = mapper.readValue(message, Long::class.java)

        val application =
            queryApplicationPort.queryApplicationByReceiptCode(receiptCode)
                ?: throw ApplicationExceptions.ApplicationNotFoundException()

        val applicationCase =
            queryApplicationCasePort.queryApplicationCaseByApplication(application)
                ?: throw ApplicationCaseExceptions.ApplicationCaseNotFoundException()

        commandApplicationCasePort.delete(applicationCase)
    }
}
