package hs.kr.entrydsm.application.domain.applicationCase.event

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.applicationCase.model.ApplicationCase
import hs.kr.entrydsm.application.domain.applicationCase.spi.CommandApplicationCasePort
import hs.kr.entrydsm.application.global.kafka.config.KafkaTopics
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class UpdateApplicationCaseRollbackConsumer(
    private val objectMapper: ObjectMapper,
    private val commandApplicationCasePort: CommandApplicationCasePort,
) {
    @KafkaListener(
        topics = [KafkaTopics.UPDATE_APPLICATION_CASE_ROLLBACK],
        groupId = "rollback-application-case",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun changeApplicationCase(message: String) {
        val applicationCase = objectMapper.readValue(message, ApplicationCase::class.java)
        commandApplicationCasePort.save(applicationCase)
    }
}
