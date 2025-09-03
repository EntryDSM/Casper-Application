package hs.kr.entrydsm.application.domain.application.event.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.global.kafka.configuration.KafkaTopics
import hs.kr.entrydsm.domain.application.interfaces.ApplicationConsumeEventContract
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.kafka.annotation.KafkaListener

@Component
class DeleteApplicationConsumer(
    private val mapper: ObjectMapper,
    private val applicationConsumeContract: ApplicationConsumeEventContract
) {
    private val logger = LoggerFactory.getLogger(DeleteApplicationConsumer::class.java)

    @KafkaListener(
        topics = [KafkaTopics.DELETE_USER],
        groupId = "delete-application-consumer",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun deleteApplication(message: String) {
        try{
            val receiptCode = mapper.readValue(message, Long::class.java)
            logger.info("사용자 삭제로 인한 원서 삭제: receiptCode=$receiptCode")
            applicationConsumeContract.deleteByReceiptCode(receiptCode)
            logger.info("원서 삭제 완료: receiptCode=$receiptCode")
        } catch (e: Exception) {
            logger.error("원서 삭제 처리 실패: $message", e)
        }
    }
}