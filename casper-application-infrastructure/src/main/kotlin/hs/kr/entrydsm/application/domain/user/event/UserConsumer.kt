package hs.kr.entrydsm.application.domain.user.event

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.usecase.DeleteApplicationUseCase
import hs.kr.entrydsm.application.domain.saga.service.ApplicationSagaService
import hs.kr.entrydsm.application.global.kafka.config.KafkaTopics
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class UserConsumer(
    private val mapper: ObjectMapper,
    private val deleteApplicationUseCase: DeleteApplicationUseCase,
    private val applicationSagaService: ApplicationSagaService,
) {

    @KafkaListener(
        topics = [KafkaTopics.USER_RECEIPT_CODE_UPDATE_COMPLETED],
        groupId = "user-receipt-code-update-completed",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun userReceiptCodeUpdateCompleted(message: String) {
        val receiptCode = mapper.readValue(message, Long::class.java)
        applicationSagaService.markUserUpdated(receiptCode)
    }

    @KafkaListener(
        topics = [KafkaTopics.USER_RECEIPT_CODE_UPDATE_FAILED],
        groupId = "user-receipt-code-update-failed",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun userReceiptCodeUpdateFailed(message: String) {
        val receiptCode = mapper.readValue(message, Long::class.java)
        applicationSagaService.markFailed(receiptCode)
        deleteApplicationUseCase.execute(receiptCode)
    }
}
