package hs.kr.entrydsm.application.domain.application.event.producer

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.event.dto.CreateApplicationEvent
import hs.kr.entrydsm.application.global.kafka.configuration.KafkaTopics
import hs.kr.entrydsm.domain.application.interfaces.ApplicationCreateEventContract
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ApplicationEventProducer(
    private val mapper: ObjectMapper,
    private val createApplicationTemplate: KafkaTemplate<String, Any>
): ApplicationCreateEventContract {

    override fun publishCreateApplication(receiptCode: Long, userId: UUID) {
        val createApplicationEvent = CreateApplicationEvent(receiptCode, userId)
        createApplicationTemplate.send(
            KafkaTopics.CREATE_APPLICATION,
            mapper.writeValueAsString(createApplicationEvent)
        )
    }
}