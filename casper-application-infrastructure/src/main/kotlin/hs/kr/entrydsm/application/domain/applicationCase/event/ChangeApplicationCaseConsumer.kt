package hs.kr.entrydsm.application.domain.applicationCase.event

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.event.dto.UpdateEducationalStatusEvent
import hs.kr.entrydsm.application.domain.applicationCase.usecase.ChangeApplicationCaseUseCase
import hs.kr.entrydsm.application.global.kafka.config.KafkaTopics
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ChangeApplicationCaseConsumer(
    private val mapper: ObjectMapper,
    private val changeApplicationCaseUseCase: ChangeApplicationCaseUseCase,
) {
    @KafkaListener(
        topics = [KafkaTopics.UPDATE_EDUCATIONAL_STATUS],
        groupId = "change-application-case",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun changeApplicationCase(dto: UpdateEducationalStatusEvent) {
        changeApplicationCaseUseCase.execute(dto.receiptCode)
    }
}
