package hs.kr.entrydsm.application.domain.applicationcase.event.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.global.kafka.configuration.KafkaTopics
import hs.kr.entrydsm.domain.applicationcase.interfaces.ApplicationCaseConsumeEventContract
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * 사용자 삭제 이벤트를 수신하여 연관된 전형을 삭제하는 Consumer 클래스입니다.
 * 
 * 사용자 서비스에서 사용자가 삭제되었을 때 해당 사용자의 전형도 함께 삭제하여
 * 데이터 일관성을 유지하는 역할을 담당합니다.
 * 
 * @property mapper JSON 역직렬화를 위한 ObjectMapper
 * @property applicationCaseConsumeContract 전형 이벤트 처리를 위한 계약 인터페이스
 */
@Component
class DeleteApplicationCaseConsumer(
    private val mapper: ObjectMapper,
    private val applicationCaseConsumeContract: ApplicationCaseConsumeEventContract
) {
    private val logger = LoggerFactory.getLogger(DeleteApplicationCaseConsumer::class.java)

    /**
     * 사용자 삭제 이벤트를 수신하여 연관된 전형을 삭제합니다.
     * 
     * DELETE_USER 토픽에서 접수번호를 수신하고, 해당 접수번호에 해당하는 
     * 전형을 삭제합니다. 처리 과정에서 발생하는 오류는 로그로 기록됩니다.
     * 
     * @param message 사용자 삭제 이벤트 메시지 (접수번호가 JSON 형태로 포함)
     */
    @KafkaListener(
        topics = [KafkaTopics.DELETE_USER],
        groupId = "delete-application-case",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun deleteApplicationCase(message: String) {
        try {
            val receiptCode = mapper.readValue(message, Long::class.java)
            logger.info("사용자 삭제로 인한 전형 삭제 시작: receiptCode=$receiptCode")
            
            applicationCaseConsumeContract.consumeDeleteApplicationCase(receiptCode)
            
            logger.info("전형 삭제 완료: receiptCode=$receiptCode")
        } catch (e: Exception) {
            logger.error("전형 삭제 처리 실패: $message", e)
        }
    }
}
