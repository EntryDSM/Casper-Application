package hs.kr.entrydsm.application.domain.application.event

import hs.kr.entrydsm.application.domain.application.event.dto.CreateApplicationEvent
import hs.kr.entrydsm.application.domain.application.event.spi.ApplicationEventPort
import hs.kr.entrydsm.application.global.kafka.config.KafkaTopics
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class ApplicationProducer(
    private val createApplicationTemplate: KafkaTemplate<String, Any>,
    private val cancelSubmittedApplicationTemplate: KafkaTemplate<String, Any>
) : ApplicationEventPort {
    override fun create(
        receiptCode: Long,
        userId: UUID,
    ) {
        val createApplicationEvent =
            CreateApplicationEvent(
                userId = userId,
                receiptCode = receiptCode,
            )
        createApplicationTemplate.send(
            KafkaTopics.CREATE_APPLICATION,
            createApplicationEvent,
        )
    }

    override fun cancelSubmittedApplication(receiptCode: Long) {
        cancelSubmittedApplicationTemplate.send(
            KafkaTopics.CANCEL_SUBMITTED_APPLICATION,
            receiptCode
        )
    }
}
