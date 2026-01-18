package hs.kr.entrydsm.application.domain.status.event

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.usecase.DeleteApplicationUseCase
import hs.kr.entrydsm.application.domain.saga.service.ApplicationSagaService
import hs.kr.entrydsm.application.global.kafka.config.KafkaTopics
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class StatusConsumer(
    private val mapper: ObjectMapper,
    private val deleteApplicationUseCase: DeleteApplicationUseCase,
    private val applicationSagaService: ApplicationSagaService,
) {

    @KafkaListener(
        topics = [KafkaTopics.APPLICATION_STATUS_CREATE_COMPLETED],
        groupId = "application-status-create-completed",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun statusCreateCompleted(message: String) {
        val receiptCode = mapper.readValue(message, Long::class.java)
        applicationSagaService.markStatusCreated(receiptCode)
    }

    @KafkaListener(
        topics = [KafkaTopics.APPLICATION_STATUS_CREATE_FAILED],
        groupId = "application-status-create-failed",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun statusCreateFailed(message: String) {
        val receiptCode = mapper.readValue(message, Long::class.java)
        applicationSagaService.markFailed(receiptCode)
        deleteApplicationUseCase.execute(receiptCode)
    }
}
