package hs.kr.entrydsm.application.domain.application.event.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.global.kafka.configuration.KafkaTopics
import hs.kr.entrydsm.domain.application.interfaces.ApplicationConsumeEventContract
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.kafka.annotation.KafkaListener

/**
 * 사용자 삭제 이벤트를 수신하여 연관된 원서를 삭제하는 Consumer 클래스입니다.
 * 
 * 사용자 서비스에서 사용자가 삭제되었을 때 해당 사용자의 원서도 함께 삭제하여
 * 데이터 일관성을 유지하는 역할을 담당합니다.
 * 
 * @property mapper JSON 역직렬화를 위한 ObjectMapper
 * @property applicationConsumeContract 원서 삭제 로직을 처리하는 계약 인터페이스
 */
@Component
class DeleteApplicationConsumer(
    private val mapper: ObjectMapper,
    private val applicationConsumeContract: ApplicationConsumeEventContract
) {
    private val logger = LoggerFactory.getLogger(DeleteApplicationConsumer::class.java)

    /**
     * 사용자 삭제 이벤트를 수신하여 연관된 원서를 삭제합니다.
     * 
     * DELETE_USER 토픽에서 접수번호를 수신하고, 해당 접수번호에 해당하는 
     * 원서를 삭제합니다. 처리 과정에서 발생하는 오류는 로그로 기록됩니다.
     * 
     * @param message 사용자 삭제 이벤트 메시지 (접수번호가 JSON 형태로 포함)
     */
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
