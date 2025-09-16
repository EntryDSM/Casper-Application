package hs.kr.entrydsm.application.domain.application.event.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.global.kafka.configuration.KafkaTopics
import hs.kr.entrydsm.domain.application.interfaces.ApplicationConsumeEventContract
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * 원서 롤백 이벤트를 수신하여 보상 트랜잭션을 수행하는 Consumer 클래스입니다.
 * 
 * 다른 서비스에서 실패가 발생했을 때 원서를 삭제하여
 * 분산 시스템의 데이터 일관성을 유지하는 역할을 담당합니다.
 * 
 * @property mapper JSON 역직렬화를 위한 ObjectMapper
 * @property applicationConsumeContract 원서 이벤트 처리를 위한 계약 인터페이스
 */
@Component
class ApplicationRollbackConsumer(
    private val mapper: ObjectMapper,
    private val applicationConsumeContract: ApplicationConsumeEventContract
) {
    private val logger = LoggerFactory.getLogger(ApplicationRollbackConsumer::class.java)

    /**
     * 상태 생성 롤백 이벤트를 수신하여 원서를 삭제합니다.
     * 
     * CREATE_APPLICATION_STATUS_ROLLBACK 토픽에서 롤백 이벤트를 수신하고,
     * 해당 접수번호의 원서를 삭제합니다.
     * 
     * @param message 상태 생성 롤백 이벤트 메시지 (접수번호가 JSON 형태로 포함)
     */
    @KafkaListener(
        topics = [KafkaTopics.CREATE_APPLICATION_STATUS_ROLLBACK],
        groupId = "create-application-status-rollback",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun createStatusRollback(message: String) {
        try {
            val receiptCode = mapper.readValue(message, Long::class.java)
            logger.info("원서 상태 생성 롤백 시작: receiptCode=$receiptCode")
            
            applicationConsumeContract.deleteByReceiptCode(receiptCode)
            
            logger.info("원서 상태 생성 롤백 완료: receiptCode=$receiptCode")
        } catch (e: Exception) {
            logger.error("원서 상태 생성 롤백 처리 실패: $message", e)
        }
    }

    /**
     * 성적 생성 롤백 이벤트를 수신하여 원서를 삭제합니다.
     * 
     * CREATE_APPLICATION_SCORE_ROLLBACK 토픽에서 롤백 이벤트를 수신하고,
     * 해당 접수번호의 원서를 삭제합니다.
     * 
     * @param message 성적 생성 롤백 이벤트 메시지 (접수번호가 JSON 형태로 포함)
     */
    @KafkaListener(
        topics = [KafkaTopics.CREATE_APPLICATION_SCORE_ROLLBACK],
        groupId = "create-score-rollback",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun createScoreRollback(message: String) {
        try {
            val receiptCode = mapper.readValue(message, Long::class.java)
            logger.info("원서 성적 생성 롤백 시작: receiptCode=$receiptCode")
            
            applicationConsumeContract.deleteByReceiptCode(receiptCode)
            
            logger.info("원서 성적 생성 롤백 완료: receiptCode=$receiptCode")
        } catch (e: Exception) {
            logger.error("원서 성적 생성 롤백 처리 실패: $message", e)
        }
    }
}
