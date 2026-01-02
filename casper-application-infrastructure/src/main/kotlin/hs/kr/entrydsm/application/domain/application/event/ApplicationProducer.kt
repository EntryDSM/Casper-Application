package hs.kr.entrydsm.application.domain.application.event

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.event.dto.CreateApplicationEvent
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

    override fun createApplicationScoreRollback(receiptCode: Long) {
        createApplicationRollbackTemplate.send(
            KafkaTopics.CREATE_APPLICATION_SCORE_ROLLBACK,
            receiptCode
        )
    }
}
