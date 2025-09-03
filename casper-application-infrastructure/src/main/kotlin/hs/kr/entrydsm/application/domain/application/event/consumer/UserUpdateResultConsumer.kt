package hs.kr.entrydsm.application.domain.application.event.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.global.kafka.configuration.KafkaTopics
import hs.kr.entrydsm.application.domain.application.event.dto.UserReceiptCodeUpdateCompletedEvent
import hs.kr.entrydsm.application.domain.application.event.dto.UserReceiptCodeUpdateFailedEvent
import hs.kr.entrydsm.domain.application.interfaces.ApplicationConsumeEventContract
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserUpdateResultConsumer(
    private val mapper: ObjectMapper,
    private val applicationConsumeContract: ApplicationConsumeEventContract,
) {
    private val logger = LoggerFactory.getLogger(UserUpdateResultConsumer::class.java)

    @KafkaListener(
        topics = [KafkaTopics.USER_RECEIPT_CODE_UPDATE_COMPLETED],
        groupId = "user-update-result-consumer",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleUserUpdateCompleted(message: String) {
        try {
            val event = mapper.readValue(message, UserReceiptCodeUpdateCompletedEvent::class.java)
            logger.info("사용자 receiptCode 업데이트 성공: receiptCode=${event.receiptCode}, userId=${event.userId}")
            // Choreography: 추가 처리가 필요하면 여기서
            // 현재는 단순히 로깅만 (원서 생성 완료)
        } catch (e: Exception) {
            logger.error("User 업데이트 성공 이벤트 처리 실패: $message", e)
        }
    }

    @KafkaListener(
        topics = [KafkaTopics.USER_RECEIPT_CODE_UPDATE_FAILED],
        groupId = "user-update-result-consumer",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    fun handleUserUpdateFailed(message: String) {
        try {
            val event = mapper.readValue(message, UserReceiptCodeUpdateFailedEvent::class.java)
            logger.info("사용자 receiptCode 업데이트 실패: receiptCode=${event.receiptCode}, reson=${event.reason}")

            logger.info("보상 트랜잭션 시작: 원서 삭제 receiptCode=${event.receiptCode}")
            applicationConsumeContract.deleteByReceiptCode(event.receiptCode)
            logger.info("보상 트랜잭션 완료: 원서 삭제됨 receiptCode=${event.receiptCode}")

        } catch (e: Exception) {
            logger.error("USER 업데이트 실패 이벤트 처리 실패 : $message", e)

        }
    }
}