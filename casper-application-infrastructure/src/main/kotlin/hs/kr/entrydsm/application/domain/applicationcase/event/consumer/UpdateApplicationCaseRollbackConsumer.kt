package hs.kr.entrydsm.application.domain.applicationcase.event.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.applicationcase.event.dto.ApplicationCase
import hs.kr.entrydsm.application.global.kafka.configuration.KafkaTopics
import hs.kr.entrydsm.domain.applicationcase.interfaces.ApplicationCaseConsumeEventContract
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * 전형 업데이트 롤백 이벤트를 수신하여 보상 트랜잭션을 수행하는 Consumer 클래스입니다.
 * 
 * 다른 서비스에서 실패가 발생했을 때 전형 데이터를 롤백하여
 * 분산 시스템의 데이터 일관성을 유지하는 역할을 담당합니다.
 * 
 * @property mapper JSON 역직렬화를 위한 ObjectMapper
 * @property applicationCaseConsumeContract 전형 이벤트 처리를 위한 계약 인터페이스
 */
@Component
class UpdateApplicationCaseRollbackConsumer(
    private val mapper: ObjectMapper,
    private val applicationCaseConsumeContract: ApplicationCaseConsumeEventContract
) {
    private val logger = LoggerFactory.getLogger(UpdateApplicationCaseRollbackConsumer::class.java)

    /**
     * 전형 업데이트 롤백 이벤트를 수신하여 보상 트랜잭션을 수행합니다.
     * 
     * UPDATE_APPLICATION_CASE_ROLLBACK 토픽에서 롤백 이벤트를 수신하고,
     * 해당 접수번호의 전형을 이전 상태로 복원합니다.
     * 
     * @param message 전형 업데이트 롤백 이벤트 메시지
     */
    @KafkaListener(
        topics = [KafkaTopics.UPDATE_APPLICATION_CASE_ROLLBACK],
        groupId = "rollback-application-case",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun updateApplicationCaseRollback(message: String) {
        try {
            val applicationCase = mapper.readValue(message, ApplicationCase::class.java)
            logger.info("전형 업데이트 롤백 시작: receiptCode=${applicationCase.receiptCode}")
            
            applicationCaseConsumeContract.consumeUpdateApplicationCaseRollback(applicationCase.receiptCode)
            
            logger.info("전형 업데이트 롤백 완료: receiptCode=${applicationCase.receiptCode}")
        } catch (e: Exception) {
            logger.error("전형 업데이트 롤백 처리 실패: $message", e)
        }
    }
}
